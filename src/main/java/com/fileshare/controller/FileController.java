package com.fileshare.controller;

import com.fileshare.entity.File;
import com.fileshare.security.CustomUserDetails;
import com.fileshare.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
@RequestMapping("/api/files")
public class FileController {

    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
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
    public File uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "isPublic", defaultValue = "false") boolean isPublic,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("请选择要上传的文件");
            }
            log.debug("开始上传文件: {}, isPublic: {}, uploaderId: {}", 
                file.getOriginalFilename(), isPublic, userDetails.getId());
            return fileService.uploadFile(file, userDetails.getId(), isPublic);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        try {
            byte[] fileData = fileService.downloadFile(id, userDetails.getId());
            File fileInfo = fileService.getFileInfo(id);
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"" + URLEncoder.encode(fileInfo.getOriginalFileName(), StandardCharsets.UTF_8) + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(fileData.length)
                .body(new ByteArrayResource(fileData));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "文件下载失败: " + e.getMessage());
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
    public void shareFile(
            @PathVariable Long id,
            @RequestBody List<Long> userIds,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        fileService.shareFile(id, userIds, userDetails.getId());
    }
} 