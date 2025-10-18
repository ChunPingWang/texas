package com.holdem.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

/**
 * Web 配置類
 * 配置 CORS、序列化等 Web 相關設定
 * 
 * @author Development Team
 * @version 1.0
 * @since 2025-10-18
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    /**
     * 配置 CORS 跨域請求
     * @return CORS 配置源
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 允許的來源
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        
        // 允許的 HTTP 方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        
        // 允許的請求頭
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // 允許發送認證資訊
        configuration.setAllowCredentials(true);
        
        // 預檢請求的快取時間（秒）
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}