package com.fileshare.controller;

import com.fileshare.entity.User;
import com.fileshare.entity.OperationLog;
import com.fileshare.service.UserService;
import com.fileshare.service.LogService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

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
        try {
            log.debug("Creating user: {}", user.getUsername());
            user.setRole("ROLE_USER"); // 默认为普通用户
            user.setIsAdmin(false);
            user.setIsEnabled(true);
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
            userService.createUser(user);
            return ResponseEntity.ok("用户创建成功");
        } catch (Exception e) {
            log.error("Failed to create user", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/users/{id}/disable")
    @ResponseBody
    public ResponseEntity<String> disableUser(@PathVariable Long id) {
        try {
            log.debug("Disabling user: {}", id);
            userService.disableUser(id);
            return ResponseEntity.ok("用户已禁用");
        } catch (Exception e) {
            log.error("Failed to disable user", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/users/{id}/enable")
    @ResponseBody
    public ResponseEntity<String> enableUser(@PathVariable Long id) {
        try {
            log.debug("Enabling user: {}", id);
            userService.enableUser(id);
            return ResponseEntity.ok("用户已启用");
        } catch (Exception e) {
            log.error("Failed to enable user", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
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

    @PutMapping("/users/{id}/permissions")
    public ResponseEntity<?> updatePermissions(
            @PathVariable Long id,
            @RequestBody PermissionUpdateRequest request) {
        try {
            User user = userService.findById(id);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "用户不存在"
                ));
            }

            log.debug("Updating permissions for user {}: upload={}, download={}, share={}",
                    user.getUsername(),
                    request.getCanUpload(),
                    request.getCanDownload(),
                    request.getCanShare());

            if (request.getCanUpload() != null) {
                user.setCanUpload(request.getCanUpload());
            }
            if (request.getCanDownload() != null) {
                user.setCanDownload(request.getCanDownload());
            }
            if (request.getCanShare() != null) {
                user.setCanShare(request.getCanShare());
            }

            log.debug("Saving user with permissions: upload={}, download={}, share={}",
                    user.getCanUpload(),
                    user.getCanDownload(),
                    user.getCanShare());

            userService.updateUser(user);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to update permissions", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "更新权限失败: " + e.getMessage()
            ));
        }
    }
}