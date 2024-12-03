package com.fileshare.service.impl;

import com.fileshare.entity.User;
import com.fileshare.mapper.UserMapper;
import com.fileshare.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

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
        userMapper.updateStatus(id, false);
    }

    @Override
    public void enableUser(Long id) {
        userMapper.updateStatus(id, true);
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
} 