package com.fileshare.service;

import com.fileshare.entity.User;
import com.fileshare.entity.PermissionUpdateRequest;

import java.util.List;
import java.util.Map;

public interface UserService {
    User findByUsername(String username);
    User createUser(User user);
    void updateUser(User user);
    void deleteUser(Long id);
    void disableUser(Long id);
    void enableUser(Long id);
    List<User> getAllUsers();
    Map<String, Object> getSystemStats();
    void setAdminRole(Long userId);
    void updatePermission(Long userId, String permission, boolean enabled);
    User findById(Long id);
    void updatePermissions(Long id, PermissionUpdateRequest request);
}
