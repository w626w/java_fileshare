package com.fileshare.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class File {
    private Long id;
    private String fileName;
    private String originalFileName;
    private String filePath;
    private Long fileSize;
    private String fileType;
    private Boolean isPublic;
    private Long uploaderId;
    private String uploaderName;
    private LocalDateTime uploadTime;
    private LocalDateTime updateTime;

    public String getUploaderName() {
        return uploaderName;
    }

    public void setUploaderName(String uploaderName) {
        this.uploaderName = uploaderName;
    }
} 