package com.fileshare.mapper;

import com.fileshare.entity.File;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface FileMapper {
    void insert(File file);
    File findById(Long id);
    List<File> findByUploaderId(Long uploaderId);
    List<File> findPublicFiles();
    List<File> searchFiles(String keyword);
    void update(File file);
    void delete(Long id);
    void shareFile(@Param("fileId") Long fileId, @Param("userId") Long userId);
} 