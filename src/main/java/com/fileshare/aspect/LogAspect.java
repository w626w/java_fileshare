package com.fileshare.aspect;

import com.fileshare.entity.OperationLog;
import com.fileshare.security.CustomUserDetails;
import com.fileshare.service.LogService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Aspect
@Component
public class LogAspect {

    @Autowired
    private LogService logService;

    @Around("execution(* com.fileshare.controller.FileController.*(..))")
    public Object logOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        
        OperationLog log = new OperationLog();
        log.setUserId(userDetails.getId());
        log.setUsername(userDetails.getUsername());
        log.setOperation(getOperationName(methodName));
        log.setDetails(getOperationDetails(joinPoint));
        log.setIpAddress(getClientIp(request));
        log.setOperationTime(LocalDateTime.now());

        Object result = joinPoint.proceed();
        logService.saveLog(log);
        return result;
    }

    private String getOperationName(String methodName) {
        switch (methodName) {
            case "uploadFile": return "上传文件";
            case "downloadFile": return "下载文件";
            case "deleteFile": return "删除文件";
            case "updateFile": return "更新文件";
            case "shareFile": return "分享文件";
            default: return methodName;
        }
    }

    private String getOperationDetails(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        StringBuilder details = new StringBuilder();
        for (Object arg : args) {
            if (arg != null && !(arg instanceof Authentication)) {
                details.append(arg.toString()).append(", ");
            }
        }
        return details.length() > 0 ? details.substring(0, details.length() - 2) : "";
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
} 