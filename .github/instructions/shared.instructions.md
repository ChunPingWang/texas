---
applyTo: '**/shared/**'
---

# Shared Layer 開發規範

## 架構職責
提供跨層共用的常數、枚舉、工具類和通用組件

## 代碼實作規範

### 常數定義
```java
public final class GameConstants {
    
    // 遊戲規則常數
    public static final int MIN_PLAYERS = 2;
    public static final int MAX_PLAYERS = 10;
    public static final int CARDS_PER_PLAYER = 2;
    public static final int COMMUNITY_CARDS_COUNT = 5;
    
    // 撲克牌常數
    public static final int DECK_SIZE = 52;
    public static final int SUITS_COUNT = 4;
    public static final int RANKS_COUNT = 13;
    
    // API 相關常數
    public static final String API_VERSION_V1 = "/api/v1";
    public static final String GAMES_ENDPOINT = "/games";
    
    private GameConstants() {
        throw new AssertionError("Utility class should not be instantiated");
    }
}
```

### 枚舉定義
```java
public enum GameState {
    WAITING_FOR_PLAYERS("等待玩家加入"),
    INITIALIZED("遊戲初始化完成"),
    PRE_FLOP("翻牌前"),
    FLOP("翻牌"),
    TURN("轉牌"),
    RIVER("河牌"),
    SHOWDOWN("攤牌"),
    FINISHED("遊戲結束");
    
    private final String description;
    
    GameState(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isActive() {
        return this != WAITING_FOR_PLAYERS && this != FINISHED;
    }
}

public enum HandRank {
    HIGH_CARD(1, "高牌"),
    PAIR(2, "一對"),
    TWO_PAIR(3, "兩對"),
    THREE_OF_A_KIND(4, "三條"),
    STRAIGHT(5, "順子"),
    FLUSH(6, "同花"),
    FULL_HOUSE(7, "葫蘆"),
    FOUR_OF_A_KIND(8, "四條"),
    STRAIGHT_FLUSH(9, "同花順"),
    ROYAL_FLUSH(10, "皇家同花順");
    
    private final int strength;
    private final String chineseName;
    
    HandRank(int strength, String chineseName) {
        this.strength = strength;
        this.chineseName = chineseName;
    }
    
    public int getStrength() {
        return strength;
    }
    
    public String getChineseName() {
        return chineseName;
    }
}
```

### 工具類實作
```java
@UtilityClass
public class CardUtils {
    
    /**
     * 驗證牌組是否為標準52張牌
     */
    public static boolean isStandardDeck(List<Card> cards) {
        return cards != null && 
               cards.size() == GameConstants.DECK_SIZE && 
               cards.stream().distinct().count() == GameConstants.DECK_SIZE;
    }
    
    /**
     * 計算手牌強度
     */
    public static int calculateHandStrength(List<Card> cards) {
        if (cards == null || cards.size() != 7) {
            throw new IllegalArgumentException("Hand evaluation requires exactly 7 cards");
        }
        
        // 實作手牌評估邏輯
        return evaluateBestHand(cards).getStrength();
    }
    
    /**
     * 格式化牌組顯示
     */
    public static String formatCards(List<Card> cards) {
        return cards.stream()
                   .map(Card::toString)
                   .collect(Collectors.joining(", "));
    }
}

@UtilityClass
public class ValidationUtils {
    
    /**
     * 驗證玩家數量是否有效
     */
    public static boolean isValidPlayerCount(int playerCount) {
        return playerCount >= GameConstants.MIN_PLAYERS && 
               playerCount <= GameConstants.MAX_PLAYERS;
    }
    
    /**
     * 驗證盲注設定
     */
    public static boolean isValidBlinds(BigDecimal smallBlind, BigDecimal bigBlind) {
        return smallBlind != null && 
               bigBlind != null && 
               smallBlind.compareTo(BigDecimal.ZERO) > 0 && 
               bigBlind.compareTo(smallBlind.multiply(BigDecimal.valueOf(2))) == 0;
    }
    
    /**
     * 驗證座位號碼
     */
    public static boolean isValidSeatNumber(int seatNumber, int totalPlayers) {
        return seatNumber >= 1 && seatNumber <= totalPlayers;
    }
}
```

## 層級職責
- 跨層共用常數定義
- 系統枚舉和狀態定義
- 通用工具方法
- 共用數據結構
- 驗證工具和規則

## 設計原則
1. 無狀態工具類設計
2. 線程安全考量
3. 高內聚低耦合
4. 單一職責原則
5. 明確的 API 契約

## 命名規範
- 常數類別：`{Domain}Constants`
- 枚舉類別：`{Domain}{Type}` (如：GameState, HandRank)
- 工具類別：`{Domain}Utils`

## 異常定義
```java
public class HoldemException extends RuntimeException {
    private final String errorCode;
    
    public HoldemException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public HoldemException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}

public class InvalidGameStateException extends HoldemException {
    public InvalidGameStateException(String message) {
        super("INVALID_GAME_STATE", message);
    }
}

public class InvalidPlayerActionException extends HoldemException {
    public InvalidPlayerActionException(String message) {
        super("INVALID_PLAYER_ACTION", message);
    }
}
```

## 通用數據傳輸物件
```java
@Value
@Builder
public class ApiResponse<T> {
    boolean success;
    String message;
    T data;
    Instant timestamp;
    
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
            .success(true)
            .data(data)
            .timestamp(Instant.now())
            .build();
    }
    
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
            .success(false)
            .message(message)
            .timestamp(Instant.now())
            .build();
    }
}

@Value
@Builder
public class PageResult<T> {
    List<T> content;
    int pageNumber;
    int pageSize;
    long totalElements;
    int totalPages;
    boolean hasNext;
    boolean hasPrevious;
}
```

## 驗證註解
```java
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidPlayerCountValidator.class)
public @interface ValidPlayerCount {
    String message() default "Player count must be between 2 and 10";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

public class ValidPlayerCountValidator implements ConstraintValidator<ValidPlayerCount, Integer> {
    
    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        return value != null && ValidationUtils.isValidPlayerCount(value);
    }
}
```