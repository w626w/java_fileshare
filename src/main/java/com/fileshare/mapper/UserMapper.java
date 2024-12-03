package com.fileshare.mapper;

import com.fileshare.entity.User;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface UserMapper {
    
    @Select("SELECT * FROM users WHERE username = #{username}")
    @Results({
        @Result(property = "canUpload", column = "can_upload"),
        @Result(property = "canDownload", column = "can_download"),
        @Result(property = "canShare", column = "can_share")
    })
    User findByUsername(String username);
    
    @Select("SELECT id, username, email, is_admin, is_enabled, role, " +
            "create_time, update_time, can_upload, can_download, can_share " +
            "FROM users")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "username", column = "username"),
        @Result(property = "email", column = "email"),
        @Result(property = "isAdmin", column = "is_admin"),
        @Result(property = "isEnabled", column = "is_enabled"),
        @Result(property = "role", column = "role"),
        @Result(property = "createTime", column = "create_time"),
        @Result(property = "updateTime", column = "update_time"),
        @Result(property = "canUpload", column = "can_upload"),
        @Result(property = "canDownload", column = "can_download"),
        @Result(property = "canShare", column = "can_share")
    })
    List<User> findAll();
    
    @Insert("INSERT INTO users (username, password, email, is_admin, is_enabled, role, create_time, update_time) " +
            "VALUES (#{username}, #{password}, #{email}, #{isAdmin}, #{isEnabled}, #{role}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(User user);
    
    void update(User user);
    
    @Delete("DELETE FROM users WHERE id = #{id}")
    void delete(Long id);
    
    @Update("UPDATE users SET is_enabled = #{status}, update_time = NOW(), " +
            "role = CASE " +
            "   WHEN #{status} = false THEN 'ROLE_DISABLED' " +
            "   WHEN is_admin = true THEN 'ROLE_ADMIN' " +
            "   ELSE 'ROLE_USER' " +
            "END " +
            "WHERE id = #{id}")
    void updateStatus(@Param("id") Long id, @Param("status") boolean status);
    
    @Select("SELECT COUNT(*) FROM users")
    int count();
    
    @Select("SELECT COUNT(*) FROM users WHERE is_enabled = #{enabled}")
    int countByEnabled(boolean enabled);
    
    @Select("SELECT COUNT(*) FROM users WHERE DATE(create_time) = CURDATE()")
    int countTodayNewUsers();
    
    @Update("UPDATE users SET role = #{role} WHERE id = #{id}")
    void updateRole(@Param("id") Long id, @Param("role") String role);
    
    @Select("SELECT * FROM users WHERE id = #{id}")
    User findById(Long id);
    
    @Update("UPDATE users SET can_upload = #{canUpload}, " +
            "can_download = #{canDownload}, " +
            "can_share = #{canShare} " +
            "WHERE id = #{id}")
    void updatePermissions(@Param("id") Long id, 
                         @Param("canUpload") boolean canUpload,
                         @Param("canDownload") boolean canDownload,
                         @Param("canShare") boolean canShare);
    
    @Update("UPDATE users SET can_upload = #{enabled} WHERE id = #{userId}")
    void updateUploadPermission(@Param("userId") Long userId, @Param("enabled") boolean enabled);
    
    @Update("UPDATE users SET can_download = #{enabled} WHERE id = #{userId}")
    void updateDownloadPermission(@Param("userId") Long userId, @Param("enabled") boolean enabled);
    
    @Update("UPDATE users SET can_share = #{enabled} WHERE id = #{userId}")
    void updateSharePermission(@Param("userId") Long userId, @Param("enabled") boolean enabled);
} 