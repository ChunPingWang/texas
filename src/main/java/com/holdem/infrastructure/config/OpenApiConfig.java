package com.holdem.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * OpenAPI 配置類
 * 配置 Swagger 文檔資訊和 API 標籤
 * 
 * @author Development Team
 * @version 1.0
 * @since 2025-10-18
 */
@Configuration
public class OpenApiConfig {
    
    /**
     * OpenAPI 配置
     * @return OpenAPI 配置實例
     */
    @Bean
    public OpenAPI holdemOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("德州撲克 API")
                        .description("德州撲克遊戲系統 RESTful API 文檔\n\n" +
                                   "## 功能特色\n" +
                                   "- 支援 2-10 人德州撲克遊戲\n" +
                                   "- 完整的下注系統（跟注、加注、全押、棄牌）\n" +
                                   "- 自動盲注管理\n" +
                                   "- 牌型評估和比較\n" +
                                   "- BDD 測試驅動開發\n\n" +
                                   "## 技術架構\n" +
                                   "- Spring Boot 3.x + Java 17\n" +
                                   "- 三層式架構設計\n" +
                                   "- SOLID 原則實現\n" +
                                   "- RESTful API 設計")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("開發團隊")
                                .email("dev@holdem.com")
                                .url("https://github.com/ChunPingWang/texas"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(Arrays.asList(
                        new Server()
                                .url("http://localhost:8080")
                                .description("本地開發環境"),
                        new Server()
                                .url("https://api.holdem.com")
                                .description("生產環境")))
                .tags(Arrays.asList(
                        new Tag()
                                .name("Game Management")
                                .description("遊戲管理相關 API - 創建遊戲、獲取狀態、開始新局"),
                        new Tag()
                                .name("Player Actions")
                                .description("玩家動作相關 API - 下注、跟注、加注、棄牌"),
                        new Tag()
                                .name("Game Information")
                                .description("遊戲資訊查詢 API - 玩家列表、遊戲狀態檢查")));
    }
}