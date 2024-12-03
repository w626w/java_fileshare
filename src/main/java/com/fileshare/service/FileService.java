package com.fileshare.service;

import com.fileshare.entity.File;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface FileService {
    File uploadFile(MultipartFile file, Long uploaderId, Boolean isPublic);
    byte[] downloadFile(Long fileId, Long userId);
    void deleteFile(Long fileId, Long userId);
    void updateFile(File file, Long userId);
    List<File> getPublicFiles();
    List<File> getUserFiles(Long userId);
    List<File> searchFiles(String keyword, Long userId);
    void shareFileToUser(Long fileId, Long fromUserId, Long toUserId);
    File getFileInfo(Long fileId);
    void shareFile(Long fileId, List<Long> userIds, Long userId);
} 