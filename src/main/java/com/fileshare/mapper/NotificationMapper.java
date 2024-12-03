package com.fileshare.mapper;

import com.fileshare.entity.Notification;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface NotificationMapper {
    
    @Insert("INSERT INTO notifications (user_id, message, type, is_read, create_time) " +
            "VALUES (#{userId}, #{message}, #{type}, #{isRead}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Notification notification);

    @Select("SELECT * FROM notifications WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Notification> findByUserId(Long userId);

    @Delete("DELETE FROM notifications WHERE id = #{id}")
    void delete(Long id);

    @Update("UPDATE notifications SET is_read = true WHERE id = #{id} AND user_id = #{userId}")
    void markAsRead(Long id, Long userId);

    @Select("SELECT COUNT(*) FROM notifications WHERE user_id = #{userId} AND is_read = false")
    int countUnreadByUserId(Long userId);
} 