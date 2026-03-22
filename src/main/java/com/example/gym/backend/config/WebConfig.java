package com.example.gym.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origins:http://localhost:3000,http://127.0.0.1:3000}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // When allowCredentials is true, we must use allowedOriginPatterns instead of allowedOrigins
        // This is because the Access-Control-Allow-Origin header cannot be "*" when credentials are allowed
        registry.addMapping("/**")
                .allowedOriginPatterns(getAllowedOriginPatterns())
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .exposedHeaders("Authorization", "Content-Type")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * Convert comma-separated origins to Spring's allowedOriginPatterns format
     * Supports patterns like "http://*.example.com" or exact URLs
     */
    private String[] getAllowedOriginPatterns() {
        String[] origins = allowedOrigins.split(",");
        String[] patterns = new String[origins.length];
        for (int i = 0; i < origins.length; i++) {
            String origin = origins[i].trim();
            // Convert exact origins to patterns by escaping dots
            // Spring's allowedOriginPatterns uses Ant-style path matching
            if (origin.contains("*")) {
                // Already a pattern - use as is
                patterns[i] = origin;
            } else {
                // For exact matches, escape dots and add pattern suffix
                // http://localhost:3000 -> http://localhost:3000
                patterns[i] = origin.replace(".", "\\.");
            }
        }
        return patterns;
    }
}