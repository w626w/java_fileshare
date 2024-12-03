package com.fileshare.mapper;

import com.fileshare.entity.OperationLog;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface LogMapper {
    
    @Insert("INSERT INTO operation_logs (user_id, username, operation, details, ip_address, operation_time) " +
            "VALUES (#{userId}, #{username}, #{operation}, #{details}, #{ipAddress}, #{operationTime})")
    void insert(OperationLog log);

    @Select("SELECT * FROM operation_logs ORDER BY operation_time DESC")
    List<OperationLog> findAll();

    @Select("SELECT * FROM operation_logs WHERE username LIKE CONCAT('%', #{keyword}, '%') " +
            "OR operation LIKE CONCAT('%', #{keyword}, '%') " +
            "OR details LIKE CONCAT('%', #{keyword}, '%') " +
            "ORDER BY operation_time DESC")
    List<OperationLog> searchLogs(String keyword);
} 