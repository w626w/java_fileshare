package com.fileshare.mapper;

import com.fileshare.entity.User;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface UserMapper {
    
    @Select("SELECT * FROM users WHERE username = #{username}")
    User findByUsername(String username);
    
    @Select("SELECT * FROM users")
    List<User> findAll();
    
    @Insert("INSERT INTO users (username, password, email, is_admin, is_enabled, create_time, update_time) " +
            "VALUES (#{username}, #{password}, #{email}, #{isAdmin}, #{isEnabled}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(User user);
    
    @Update("UPDATE users SET username = #{username}, email = #{email}, " +
            "is_enabled = #{isEnabled}, update_time = NOW() WHERE id = #{id}")
    void update(User user);
    
    @Delete("DELETE FROM users WHERE id = #{id}")
    void delete(Long id);
    
    @Update("UPDATE users SET is_enabled = #{status}, update_time = NOW() WHERE id = #{id}")
    void updateStatus(@Param("id") Long id, @Param("status") boolean status);
    
    @Select("SELECT COUNT(*) FROM users")
    int count();
    
    @Select("SELECT COUNT(*) FROM users WHERE is_enabled = #{enabled}")
    int countByEnabled(boolean enabled);
    
    @Select("SELECT COUNT(*) FROM users WHERE DATE(create_time) = CURDATE()")
    int countTodayNewUsers();
    
    @Update("UPDATE users SET role = #{role} WHERE id = #{id}")
    void updateRole(@Param("id") Long id, @Param("role") String role);
} 