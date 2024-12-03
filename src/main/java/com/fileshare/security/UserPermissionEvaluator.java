package com.fileshare.security;

import com.fileshare.entity.User;
import com.fileshare.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class UserPermissionEvaluator {
    
    private static final Logger log = LoggerFactory.getLogger(UserPermissionEvaluator.class);
    
    @Autowired
    private UserService userService;
    
    public boolean canUpload(Authentication auth) {
        try {
            log.debug("Evaluating upload permission for request");
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            User user = userService.findByUsername(userDetails.getUsername());
            if (user == null) {
                log.warn("User not found");
                return false;
            }
            log.debug("Checking upload permission for user {}: {}", user.getUsername(), user.getCanUpload());
            boolean result = user.getCanUpload();
            log.debug("Upload permission result for user {}: {}", user.getUsername(), result);
            return result;
        } catch (Exception e) {
            log.error("Error checking upload permission", e);
            return false;
        }
    }
    
    public boolean canDownload(Authentication auth) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            User user = userService.findByUsername(userDetails.getUsername());
            if (user == null) {
                log.warn("User not found");
                return false;
            }
            log.debug("Checking download permission for user {}: {}", user.getUsername(), user.getCanDownload());
            return user.getCanDownload();
        } catch (Exception e) {
            log.error("Error checking download permission", e);
            return false;
        }
    }
    
    public boolean canShare(Authentication auth) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            User user = userService.findByUsername(userDetails.getUsername());
            if (user == null) {
                log.warn("User not found");
                return false;
            }
            log.debug("Checking share permission for user {}: {}", user.getUsername(), user.getCanShare());
            return user.getCanShare();
        } catch (Exception e) {
            log.error("Error checking share permission", e);
            return false;
        }
    }
} 