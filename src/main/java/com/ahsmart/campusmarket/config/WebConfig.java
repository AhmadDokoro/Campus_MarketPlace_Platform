package com.ahsmart.campusmarket.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve images and library assets that are still in templates/ (we moved CSS/JS to /static/)
        registry.addResourceHandler("/img/**").addResourceLocations("classpath:/templates/img/");
        registry.addResourceHandler("/lib/**").addResourceLocations("classpath:/templates/lib/");
    }
}
