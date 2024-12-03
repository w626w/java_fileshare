package com.fileshare.controller;

import com.fileshare.entity.User;
import com.fileshare.entity.OperationLog;
import com.fileshare.service.UserService;
import com.fileshare.service.LogService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping({"/admin", "/api/admin"})
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final LogService logService;

    public AdminController(UserService userService, LogService logService) {
        this.userService = userService;
        this.logService = logService;
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "admin/dashboard";
    }

    @GetMapping("/users")
    @ResponseBody
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PostMapping("/users")
    @ResponseBody
    public ResponseEntity<String> createUser(@RequestBody User user) {
        userService.createUser(user);
        return ResponseEntity.ok("用户创建成功");
    }

    @PutMapping("/users/{id}/disable")
    public ResponseEntity<String> disableUser(@PathVariable Long id) {
        userService.disableUser(id);
        return ResponseEntity.ok("用户已禁用");
    }

    @PutMapping("/users/{id}/enable")
    public ResponseEntity<String> enableUser(@PathVariable Long id) {
        userService.enableUser(id);
        return ResponseEntity.ok("用户已启用");
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("用户已删除");
    }

    @GetMapping("/logs")
    public ResponseEntity<List<OperationLog>> getAllLogs() {
        return ResponseEntity.ok(logService.getAllLogs());
    }

    @GetMapping("/logs/search")
    public ResponseEntity<List<OperationLog>> searchLogs(@RequestParam String keyword) {
        return ResponseEntity.ok(logService.searchLogs(keyword));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getSystemStats() {
        Map<String, Object> stats = userService.getSystemStats();
        return ResponseEntity.ok(stats);
    }

    @RequestMapping("/api/users")
    @ResponseBody
    public ResponseEntity<List<User>> getUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
} 