package com.fileshare.aspect;

import com.fileshare.entity.OperationLog;
import com.fileshare.security.CustomUserDetails;
import com.fileshare.service.LogService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Arrays;

@Aspect
@Component
public class LogAspect {

    private final LogService logService;
    private final HttpServletRequest request;
    private static final Logger log = LoggerFactory.getLogger(LogAspect.class);

    public LogAspect(LogService logService, HttpServletRequest request) {
        this.logService = logService;
        this.request = request;
    }

    @Around("@annotation(org.springframework.web.bind.annotation.RequestMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.GetMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.DeleteMapping)")
    public Object logOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        log.debug("Starting to log operation: {}", joinPoint.getSignature().getName());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            
            OperationLog operationLog = new OperationLog();
            operationLog.setUserId(userDetails.getId());
            operationLog.setUsername(userDetails.getUsername());
            operationLog.setOperation(joinPoint.getSignature().getName());
            operationLog.setDetails(Arrays.toString(joinPoint.getArgs()));
            operationLog.setIpAddress(getClientIp());
            operationLog.setOperationTime(LocalDateTime.now());
            
            try {
                Object result = joinPoint.proceed();
                logService.saveLog(operationLog);
                return result;
            } catch (Exception e) {
                operationLog.setDetails(operationLog.getDetails() + " [Error: " + e.getMessage() + "]");
                logService.saveLog(operationLog);
                throw e;
            }
        }
        return joinPoint.proceed();
    }

    private String getClientIp() {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
} 