package com.demo.springboot.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC Configuration for HTTP request/response logging
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Autowired
    private HttpLoggingInterceptor httpLoggingInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(httpLoggingInterceptor)
                .addPathPatterns("/api/**")  // Only log API endpoints
                .excludePathPatterns("/actuator/**", "/h2-console/**"); // Exclude management endpoints
    }
}
