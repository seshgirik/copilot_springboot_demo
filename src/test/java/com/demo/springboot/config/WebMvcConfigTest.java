package com.demo.springboot.config;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Unit tests for WebMvcConfig
 * Tests Spring MVC configuration and interceptor setup
 */
@WebMvcTest
@ContextConfiguration(classes = {WebMvcConfig.class, FilterConfig.class, HttpLoggingInterceptor.class, HttpBodyLoggingFilter.class})
@DisplayName("WebMvcConfig Unit Tests")
class WebMvcConfigTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("Should load WebMvcConfig bean successfully")
    void shouldLoadWebMvcConfigBeanSuccessfully() {
        // Act & Assert
        assertThat(applicationContext.getBean(WebMvcConfig.class)).isNotNull();
        assertThat(applicationContext.getBean(WebMvcConfig.class)).isInstanceOf(WebMvcConfigurer.class);
    }

    @Test
    @DisplayName("Should have HttpLoggingInterceptor bean configured")
    void shouldHaveHttpLoggingInterceptorBeanConfigured() {
        // Act & Assert
        assertThat(applicationContext.containsBean("httpLoggingInterceptor")).isTrue();
        assertThat(applicationContext.getBean(HttpLoggingInterceptor.class)).isNotNull();
    }

    @Test
    @DisplayName("Should have FilterConfig bean configured")
    void shouldHaveFilterConfigBeanConfigured() {
        // Act & Assert
        assertThat(applicationContext.containsBean("filterConfig")).isTrue();
        assertThat(applicationContext.getBean(FilterConfig.class)).isNotNull();
    }

    @Test
    @DisplayName("Should have HttpBodyLoggingFilter bean configured")
    void shouldHaveHttpBodyLoggingFilterBeanConfigured() {
        // Act & Assert
        assertThat(applicationContext.containsBean("httpBodyLoggingFilter")).isTrue();
        assertThat(applicationContext.getBean(HttpBodyLoggingFilter.class)).isNotNull();
    }
}
