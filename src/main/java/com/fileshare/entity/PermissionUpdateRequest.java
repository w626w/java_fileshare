package com.fileshare.entity;

import java.util.Map;

public class PermissionUpdateRequest {
    private Long id;
    private Map<String, Boolean> permissions;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Map<String, Boolean> getPermissions() { return permissions; }
    public void setPermissions(Map<String, Boolean> permissions) { this.permissions = permissions; }
} 