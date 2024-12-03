package com.fileshare.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, Model model, HttpServletRequest request) {
        log.error("请求处理出错: {} - {}", request.getRequestURI(), e.getMessage(), e);
        
        model.addAttribute("error", e.getMessage());
        model.addAttribute("timestamp", new Date());
        model.addAttribute("path", request.getRequestURI());
        model.addAttribute("trace", e.getStackTrace());
        
        return "error";
    }
} 