---
applyTo: '**/presentation/**'
---

# Presentation Layer 開發規範

## 架構職責
負責處理 HTTP 請求、數據驗證、格式轉換

## 代碼實作規範

### Controller 實作模式
```java
@RestController
@RequestMapping("/api/v1/games")
@Validated
public class GameController {
    
    private final GameService gameService;
    
    public GameController(GameService gameService) {
        this.gameService = gameService;
    }
    
    @PostMapping
    public ResponseEntity<GameResponseDto> createGame(
            @Valid @RequestBody CreateGameRequestDto request) {
        var game = gameService.createGame(request.toCommand());
        return ResponseEntity.ok(GameResponseDto.from(game));
    }
}
```

## 層級職責
- HTTP 請求處理
- 數據格式轉換 (DTO ↔ Domain)
- 輸入驗證
- 異常處理
- API 文檔生成

## 設計原則
1. Controller 只負責請求路由和格式轉換
2. 所有業務邏輯委託給 Service 層
3. 使用 DTO 進行數據傳輸
4. 統一異常處理機制
5. API 版本控制策略

## 命名規範
- Controller 類別：`{Entity}Controller`
- DTO 類別：`{Entity}RequestDto` / `{Entity}ResponseDto`
- Validator 類別：`{Entity}Validator`

## 驗證規範
```java
public class CreateGameRequestDto {
    
    @NotNull(message = "Player count cannot be null")
    @Min(value = 2, message = "Minimum 2 players required")
    @Max(value = 10, message = "Maximum 10 players allowed")
    private Integer playerCount;
    
    @Valid
    @NotNull(message = "Blinds configuration required")
    private BlindsDto blinds;
}
```

## API 文檔標準
- 使用 SpringDoc OpenAPI 3
- 完整的請求/響應範例
- 錯誤代碼文檔
- 業務場景說明