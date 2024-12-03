package com.fileshare.controller;

public class PermissionUpdateRequest {
    private Long id;
    private Boolean canUpload;
    private Boolean canDownload;
    private Boolean canShare;
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Boolean getCanUpload() { return canUpload; }
    public Boolean getCanDownload() { return canDownload; }
    public Boolean getCanShare() { return canShare; }
    public void setCanUpload(Boolean canUpload) { this.canUpload = canUpload; }
    public void setCanDownload(Boolean canDownload) { this.canDownload = canDownload; }
    public void setCanShare(Boolean canShare) { this.canShare = canShare; }
} 