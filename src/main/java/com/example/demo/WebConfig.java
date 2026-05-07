package com.example.demo;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// CORS болон AuthFilter тохиргоо
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthFilter authFilter;

    public WebConfig(AuthFilter authFilter) {
        this.authFilter = authFilter;
    }

    // Frontend-аас (localhost:5173) хандах боломжтой болгох CORS тохиргоо
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }

    // AuthFilter-ийг зөвхөн /api/users/* замд ажиллуулахаар бүртгэнэ
    @Bean
    public FilterRegistrationBean<AuthFilter> authFilterRegistration() {
        FilterRegistrationBean<AuthFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(authFilter);
        registration.addUrlPatterns("/api/users/*");
        registration.setOrder(1);
        return registration;
    }
}
