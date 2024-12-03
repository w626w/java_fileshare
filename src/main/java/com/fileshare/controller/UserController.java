package com.fileshare.controller;

import com.fileshare.entity.User;
import com.fileshare.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/list")
    public List<User> getUserList() {
        return userService.getAllUsers();
    }

    @PostMapping("/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<?> updatePermissions(@RequestBody PermissionUpdateRequest request) {
        try {
            User user = userService.findById(request.getId());
            if (user == null) {
                return ResponseEntity.badRequest().body("用户不存在");
            }

            if (request.getCanUpload() != null) {
                user.setCanUpload(request.getCanUpload());
            }
            if (request.getCanDownload() != null) {
                user.setCanDownload(request.getCanDownload());
            }
            if (request.getCanShare() != null) {
                user.setCanShare(request.getCanShare());
            }

            userService.updateUser(user);
            return ResponseEntity.ok("权限更新成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("权限更新失败: " + e.getMessage());
        }
    }
} 