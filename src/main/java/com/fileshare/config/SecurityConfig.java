package com.fileshare.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.fileshare.security.CustomUserDetailsService;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;


@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers("/auth/login", "/auth/register", "/error").permitAll()
                .antMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                .antMatchers("/admin/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.POST, "/api/files/upload").access("@userPermissionEvaluator.canUpload(authentication)")
                .antMatchers(HttpMethod.GET, "/api/files/download/**").access("@userPermissionEvaluator.canDownload(authentication)")
                .antMatchers(HttpMethod.POST, "/api/files/*/share").access("@userPermissionEvaluator.canShare(authentication)")
                .anyRequest().authenticated()
                .and()
            .exceptionHandling()
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    String message = "";
                    if (request.getRequestURI().contains("/upload")) {
                        message = "{\"error\": \"您没有上传权限\"}";
                    } else if (request.getRequestURI().contains("/download")) {
                        message = "{\"error\": \"您没有下载权限\"}";
                    } else if (request.getRequestURI().contains("/share")) {
                        message = "{\"error\": \"您没有分享权限\"}";
                    } else {
                        message = "{\"error\": \"权限不足\"}";
                    }
                    response.getWriter().write(message);
                })
                .and()
            .formLogin()
                .loginPage("/auth/login")
                .defaultSuccessUrl("/")
                .permitAll()
                .and()
            .logout()
                .logoutUrl("/auth/logout")
                .logoutSuccessUrl("/auth/login")
                .permitAll()
                .and()
            .csrf().disable();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService)
            .passwordEncoder(passwordEncoder());
    }
} 