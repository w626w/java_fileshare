package com.fileshare.controller;

import com.fileshare.entity.File;
import com.fileshare.entity.User;
import com.fileshare.security.CustomUserDetails;
import com.fileshare.service.FileService;
import com.fileshare.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/files")
public class FileController {

    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    private final FileService fileService;
    private final UserService userService;

    public FileController(FileService fileService, UserService userService) {
        this.fileService = fileService;
        this.userService = userService;
    }

    @GetMapping("/public")
    @ResponseBody
    public List<File> getPublicFiles() {
        log.debug("Getting public files");
        List<File> files = fileService.getPublicFiles();
        log.debug("Found {} public files", files.size());
        files.forEach(file -> log.debug("File: {}", file));
        return files;
    }

    @GetMapping("/my")
    @ResponseBody
    public List<File> getMyFiles(@AuthenticationPrincipal CustomUserDetails userDetails) {
        log.debug("Getting files for user: {}", userDetails.getId());
        List<File> files = fileService.getUserFiles(userDetails.getId());
        log.debug("Found {} files", files.size());
        files.forEach(file -> log.debug("File: {}", file));
        return files;
    }

    @PostMapping("/upload")
    @ResponseBody
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "isPublic", defaultValue = "true") Boolean isPublic,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            log.debug("Received upload request from user: {}", userDetails.getUsername());
            // 检查用户是否有上传权限
            User user = userService.findByUsername(userDetails.getUsername());
            if (!user.getCanUpload()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("error", "您没有上传权限"));
            }
            
            File uploadedFile = fileService.uploadFile(file, userDetails.getId(), isPublic);
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                    "success", true, 
                    "message", "文件上传成功",
                    "file", uploadedFile
                ));
        } catch (Exception e) {
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("error", "上传失败: " + e.getMessage()));
        }
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<?> downloadFile(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            // 检查用户是否有下载权限
            User user = userService.findByUsername(userDetails.getUsername());
            if (!user.getCanDownload()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("error", "您没有下载权限"));
            }

            byte[] fileData = fileService.downloadFile(id, userDetails.getId());
            File fileInfo = fileService.getFileInfo(id);
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"" + URLEncoder.encode(fileInfo.getOriginalFileName(), StandardCharsets.UTF_8) + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(fileData.length)
                .body(new ByteArrayResource(fileData));
        } catch (Exception e) {
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("error", "下载失败: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public void deleteFile(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        fileService.deleteFile(id, userDetails.getId());
    }

    @GetMapping("/search")
    @ResponseBody
    public List<File> searchFiles(
            @RequestParam String keyword,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return fileService.searchFiles(keyword, userDetails.getId());
    }

    @PostMapping("/{id}/share")
    @ResponseBody
    public ResponseEntity<?> shareFile(
            @PathVariable Long id,
            @RequestBody List<Long> userIds,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            // 检查用户是否有分享权限
            User user = userService.findByUsername(userDetails.getUsername());
            if (!user.getCanShare()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("error", "您没有分享权限"));
            }

            fileService.shareFile(id, userIds, userDetails.getId());
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("message", "文件分享成功"));
        } catch (Exception e) {
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("error", "分享失败: " + e.getMessage()));
        }
    }
} 