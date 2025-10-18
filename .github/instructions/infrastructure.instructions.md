---
applyTo: '**/infrastructure/**'
---

# Infrastructure Layer 開發規範

## 架構職責
提供基礎設施服務、配置管理、安全控制

## 代碼實作規範

### 配置管理
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/v1/games/**").authenticated()
                .anyRequest().permitAll()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.decoder(jwtDecoder()))
            );
        return http.build();
    }
}
```

### 全域異常處理
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(InvalidGameConfigurationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidGameConfiguration(
            InvalidGameConfigurationException e) {
        var errorResponse = ErrorResponse.builder()
            .code("INVALID_GAME_CONFIGURATION")
            .message(e.getMessage())
            .timestamp(Instant.now())
            .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(
            EntityNotFoundException e) {
        var errorResponse = ErrorResponse.builder()
            .code("ENTITY_NOT_FOUND")
            .message(e.getMessage())
            .timestamp(Instant.now())
            .build();
        
        return ResponseEntity.notFound().build();
    }
}
```

## 層級職責
- 系統配置管理
- 安全認證授權
- 跨切面關注點 (AOP)
- 外部系統整合
- 監控和日誌

## 配置規範
### Application Properties
```yaml
# application.yml
spring:
  application:
    name: texas-holdem
  
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:local}
  
  datasource:
    url: ${DATABASE_URL:jdbc:h2:mem:testdb}
    username: ${DATABASE_USERNAME:sa}
    password: ${DATABASE_PASSWORD:}
  
  jpa:
    hibernate:
      ddl-auto: ${JPA_DDL_AUTO:validate}
    show-sql: ${JPA_SHOW_SQL:false}
    properties:
      hibernate:
        format_sql: true
        
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${JWT_ISSUER_URI:}

logging:
  level:
    com.holdem: ${LOG_LEVEL:INFO}
    org.springframework.security: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
```

## 安全規範
### JWT 認證配置
```java
@Configuration
public class JwtConfig {
    
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;
    
    @Bean
    public JwtDecoder jwtDecoder() {
        return JwtDecoders.fromIssuerLocation(issuerUri);
    }
    
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        var converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            // 自定義權限轉換邏輯
            return AuthorityUtils.createAuthorityList("ROLE_USER");
        });
        return converter;
    }
}
```

## 設計原則
1. 配置外部化，支援多環境
2. 安全優先，預設拒絕策略
3. 統一異常處理機制
4. 完整的監控和日誌記錄
5. 外部依賴隔離與容錯

## 命名規範
- Config 類別：`{Feature}Config`
- Exception Handler：`Global{Feature}ExceptionHandler`
- Security 相關：`{Feature}Security{Type}`

## 監控和日誌
```java
@Component
@Slf4j
public class GameAuditService {
    
    @EventListener
    @Async
    public void handleGameCreated(GameCreatedEvent event) {
        log.info("Game created: gameId={}, playerCount={}, createdBy={}", 
                event.getGameId(), 
                event.getPlayerCount(), 
                event.getCreatedBy());
        
        // 發送到監控系統
        meterRegistry.counter("game.created").increment();
    }
}
```

## 外部整合規範
```java
@Component
public class PaymentServiceClient {
    
    private final WebClient webClient;
    private final CircuitBreaker circuitBreaker;
    
    public PaymentServiceClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
            .baseUrl("${payment.service.base-url}")
            .build();
        this.circuitBreaker = CircuitBreaker.ofDefaults("payment-service");
    }
    
    public Mono<PaymentResult> processPayment(PaymentRequest request) {
        return circuitBreaker.executeSupplier(() ->
            webClient.post()
                .uri("/payments")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(PaymentResult.class)
        );
    }
}
```