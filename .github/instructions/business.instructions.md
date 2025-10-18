---
applyTo: '**/business/**'
---

# Business Layer 開發規範

## 架構職責
包含核心業務邏輯、領域模型、用例實現

## 代碼實作規範

### Service 實作模式
```java
@Service
@Transactional
public class GameService {
    
    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    
    public GameService(GameRepository gameRepository, 
                      PlayerRepository playerRepository) {
        this.gameRepository = gameRepository;
        this.playerRepository = playerRepository;
    }
    
    public Game createGame(CreateGameCommand command) {
        validateGameCreation(command);
        var game = Game.create(command);
        return gameRepository.save(game);
    }
    
    private void validateGameCreation(CreateGameCommand command) {
        if (command.getPlayerCount() < 2 || command.getPlayerCount() > 10) {
            throw new InvalidGameConfigurationException("Player count must be between 2 and 10");
        }
    }
}
```

## 層級職責
- 業務規則實現
- 領域模型管理
- 事務管理
- 業務驗證
- 用例編排

## SOLID 原則實踐

### 單一職責原則 (SRP)
```java
// ✅ 遵循 SRP - 每個類別只有一個職責
public class GameService {
    public Game createGame(CreateGameCommand command) { /* ... */ }
}

public class GameValidator {
    public void validate(Game game) { /* ... */ }
}

public class NotificationService {
    public void sendGameCreatedNotification(Game game) { /* ... */ }
}
```

### 開放封閉原則 (OCP)
```java
// 抽象基類
public abstract class HandEvaluator {
    public abstract HandRank evaluate(List<Card> cards);
    public abstract int compare(Hand hand1, Hand hand2);
}

// 具體實現 - 對擴展開放，對修改封閉
public class TexasHoldemHandEvaluator extends HandEvaluator {
    @Override
    public HandRank evaluate(List<Card> cards) {
        // Texas Hold'em 特定的牌型評估邏輯
    }
}
```

### 依賴反轉原則 (DIP)
```java
@Service
public class GameService {
    
    private final GameRepository gameRepository;
    private final PaymentProcessor paymentProcessor;
    private final NotificationSender notificationSender;
    
    // 依賴注入抽象而非具體實現
    public GameService(GameRepository gameRepository,
                      PaymentProcessor paymentProcessor,
                      NotificationSender notificationSender) {
        this.gameRepository = gameRepository;
        this.paymentProcessor = paymentProcessor;
        this.notificationSender = notificationSender;
    }
}
```

## 設計原則
1. 富領域模型設計
2. 依賴注入和介面導向
3. 事務邊界清晰定義
4. 業務異常處理機制
5. 命令查詢責任分離 (CQRS)

## 命名規範
- Service 類別：`{Entity}Service`
- Domain 類別：`{Entity}` (如：Game, Player)
- Command 類別：`{Action}{Entity}Command`
- UseCase 類別：`{Action}{Entity}UseCase`

## 異常處理規範
```java
// 自定義業務異常
public class GameException extends RuntimeException {
    public GameException(String message) {
        super(message);
    }
    
    public GameException(String message, Throwable cause) {
        super(message, cause);
    }
}

public class InvalidGameConfigurationException extends GameException {
    public InvalidGameConfigurationException(String message) {
        super(message);
    }
}
```