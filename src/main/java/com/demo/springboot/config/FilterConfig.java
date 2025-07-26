package com.demo.springboot.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Filter Configuration for HTTP body logging
 */
@Configuration
public class FilterConfig {
    
    @Autowired
    private HttpBodyLoggingFilter httpBodyLoggingFilter;
    
    @Bean
    public FilterRegistrationBean<HttpBodyLoggingFilter> loggingFilter() {
        FilterRegistrationBean<HttpBodyLoggingFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(httpBodyLoggingFilter);
        registrationBean.addUrlPatterns("/api/*");
        registrationBean.setOrder(1);
        return registrationBean;
    }
}
