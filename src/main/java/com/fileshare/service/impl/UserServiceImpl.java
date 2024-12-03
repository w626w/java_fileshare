package com.fileshare.service.impl;

import com.fileshare.entity.User;
import com.fileshare.entity.PermissionUpdateRequest;
import com.fileshare.mapper.UserMapper;
import com.fileshare.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private UserMapper userMapper;

    private PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    @Override
    public User createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setIsEnabled(true);
        userMapper.insert(user);
        return user;
    }

    @Override
    public void updateUser(User user) {
        userMapper.update(user);
    }

    @Override
    public void deleteUser(Long id) {
        userMapper.delete(id);
    }

    @Override
    public void disableUser(Long id) {
        log.debug("Disabling user with id: {}", id);
        userMapper.updateStatus(id, false);
        log.debug("User disabled successfully");
    }

    @Override
    public void enableUser(Long id) {
        log.debug("Enabling user with id: {}", id);
        userMapper.updateStatus(id, true);
        log.debug("User enabled successfully");
    }

    @Override
    public List<User> getAllUsers() {
        return userMapper.findAll();
    }

    @Override
    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userMapper.count());
        stats.put("activeUsers", userMapper.countByEnabled(true));
        stats.put("newUsers", userMapper.countTodayNewUsers());
        return stats;
    }

    @Override
    public void setAdminRole(Long userId) {
        userMapper.updateRole(userId, "ROLE_ADMIN");
    }

    @Override
    public void updatePermission(Long userId, String permission, boolean enabled) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        switch (permission) {
            case "upload":
                userMapper.updateUploadPermission(userId, enabled);
                break;
            case "download":
                userMapper.updateDownloadPermission(userId, enabled);
                break;
            case "share":
                userMapper.updateSharePermission(userId, enabled);
                break;
            default:
                throw new RuntimeException("未知的权限类型");
        }
    }

    @Override
    public User findById(Long id) {
        return userMapper.findById(id);
    }

    @Override
    public void updatePermissions(Long id, PermissionUpdateRequest request) {
        Map<String, Boolean> permissions = request.getPermissions();
        userMapper.updatePermissions(id, 
            permissions.get("canUpload"),
            permissions.get("canDownload"), 
            permissions.get("canShare"));
    }
} 