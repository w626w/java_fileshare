package com.fileshare.service.impl;

import com.fileshare.entity.File;
import com.fileshare.entity.User;
import com.fileshare.mapper.FileMapper;
import com.fileshare.mapper.UserMapper;
import com.fileshare.service.FileService;
import com.fileshare.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    private static final Logger log = LoggerFactory.getLogger(FileServiceImpl.class);

    @Value("${file.upload.path}")
    private String uploadPath;

    @Autowired
    private FileMapper fileMapper;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserMapper userMapper;

    @Override
    public File uploadFile(MultipartFile multipartFile, Long uploaderId, Boolean isPublic) {
        try {
            // 确保上传目录存在
            Path directory = Paths.get(uploadPath.trim());
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }

            // 生成唯一文件名
            String originalFileName = multipartFile.getOriginalFilename();
            String fileType = multipartFile.getContentType();
            if (fileType != null && fileType.length() > 50) {
                fileType = fileType.substring(0, 50);
            }
            String uniqueFileName = UUID.randomUUID().toString() + "_" + originalFileName;
            
            // 保存文件
            Path filePath = directory.resolve(uniqueFileName);
            Files.copy(multipartFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 创建文件记录
            File file = new File();
            file.setFileName(uniqueFileName);
            file.setOriginalFileName(originalFileName);
            file.setFilePath(filePath.toString());
            file.setFileSize(multipartFile.getSize());
            file.setFileType(fileType);
            file.setIsPublic(isPublic);
            file.setUploaderId(uploaderId);
            file.setUploadTime(LocalDateTime.now());
            file.setUpdateTime(LocalDateTime.now());

            // 保存到数据库
            fileMapper.insert(file);

            if (isPublic) {
                // 如果是公开文件，通知所有用户
                List<User> allUsers = userMapper.findAll();
                for (User user : allUsers) {
                    if (!user.getId().equals(uploaderId)) {
                        notificationService.sendNotification(
                            user.getId(),
                            String.format("新的公开文件已上传: %s", file.getOriginalFileName()),
                            "NEW_PUBLIC_FILE"
                        );
                    }
                }
            }

            return file;
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public byte[] downloadFile(Long fileId, Long userId) {
        File file = fileMapper.findById(fileId);
        if (file == null) {
            throw new RuntimeException("文件不存在");
        }

        if (!file.getIsPublic() && !file.getUploaderId().equals(userId)) {
            throw new RuntimeException("没有权限下载此文件");
        }

        try {
            // 使用正确的路径分隔符并处理路径
            String normalizedPath = file.getFilePath().replace('\\', '/');
            Path filePath = Paths.get(normalizedPath).normalize();
            
            // 检查文件是否存在
            if (!Files.exists(filePath)) {
                log.error("文件不存在于服务器: {}", filePath);
                throw new RuntimeException("文件不存在于服务器");
            }
            
            // 检查文件是否可读
            if (!Files.isReadable(filePath)) {
                log.error("文件不可读: {}", filePath);
                throw new RuntimeException("文件不可读");
            }
            
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            log.error("文件下载失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件下载失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteFile(Long fileId, Long userId) {
        File file = fileMapper.findById(fileId);
        if (file == null) {
            throw new RuntimeException("文件不存在");
        }

        if (!file.getUploaderId().equals(userId)) {
            throw new RuntimeException("没有权限删除文件");
        }

        try {
            Path filePath = Paths.get(file.getFilePath());
            Files.deleteIfExists(filePath);
            fileMapper.delete(fileId);
        } catch (IOException e) {
            throw new RuntimeException("文件删除失败", e);
        }
    }

    @Override
    public void updateFile(File file, Long userId) {
        File existingFile = fileMapper.findById(file.getId());
        if (existingFile == null) {
            throw new RuntimeException("文件不存在");
        }

        if (!existingFile.getUploaderId().equals(userId)) {
            throw new RuntimeException("没有权限修改此文件");
        }

        file.setUpdateTime(LocalDateTime.now());
        fileMapper.update(file);

        if (existingFile.getIsPublic()) {
            // 如果是公开文件，通知所有用户文件已更新
            List<User> allUsers = userMapper.findAll();
            for (User user : allUsers) {
                if (!user.getId().equals(userId)) {
                    notificationService.sendNotification(
                        user.getId(),
                        String.format("公开文件已更新: %s", file.getOriginalFileName()),
                        "FILE_UPDATE"
                    );
                }
            }
        }
    }

    @Override
    public List<File> getPublicFiles() {
        return fileMapper.findPublicFiles();
    }

    @Override
    public List<File> getUserFiles(Long userId) {
        return fileMapper.findByUploaderId(userId);
    }

    @Override
    public List<File> searchFiles(String keyword, Long userId) {
        return fileMapper.searchFiles(keyword);
    }

    @Override
    public void shareFileToUser(Long fileId, Long fromUserId, Long toUserId) {
        File file = fileMapper.findById(fileId);
        if (file == null) {
            throw new RuntimeException("文件不存在");
        }

        if (!file.getUploaderId().equals(fromUserId)) {
            throw new RuntimeException("没有权限分享此文件");
        }

        fileMapper.shareFile(fileId, toUserId);
        
        // 添加通知
        notificationService.sendNotification(
            toUserId,
            String.format("用户 %d 向您分享了文件: %s", fromUserId, file.getOriginalFileName()),
            "FILE_SHARE"
        );
    }

    @Override
    public File getFileInfo(Long fileId) {
        return fileMapper.findById(fileId);
    }

    @Override
    public void shareFile(Long fileId, List<Long> userIds, Long userId) {
        for (Long toUserId : userIds) {
            shareFileToUser(fileId, userId, toUserId);
        }
    }
} 