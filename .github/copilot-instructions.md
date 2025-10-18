# Texas Hold'em 專案開發指引

## 專案概述
本專案採用 Java 17 + Spring Boot 開發 Texas Hold'em 撲克系統，遵循 BDD 行為驅動開發方法論，實現高品質、可維護的企業級應用。

## 技術棧規範

### 核心技術
- **JDK**: OpenJDK 17 (LTS)
- **建構工具**: Gradle 8.x
- **框架**: Spring Boot 3.x
- **測試框架**: JUnit 5 + Mockito
- **BDD 框架**: Cucumber for Java 7.x
- **數據庫**: H2 (測試) / PostgreSQL (生產)
- **ORM**: Spring Data JPA + Hibernate

### 開發工具
- **IDE**: IntelliJ IDEA 2023.x+
- **版本控制**: Git + GitHub
- **CI/CD**: GitHub Actions
- **代碼品質**: SonarQube + SpotBugs + Checkstyle
- **API 文檔**: SpringDoc OpenAPI 3

## 專案架構

### 三層式架構設計
```
com/holdem/
├── presentation/     # 展示層 (Controllers, DTOs, Validators)
├── business/         # 業務層 (Services, Domain Logic)
├── persistence/      # 持久層 (Repositories, Entities)
├── infrastructure/   # 基礎設施層 (Config, Security, Exception)
└── shared/          # 共用組件 (Constants, Enums, Utils)
```

### 各層職責
- **Presentation**: HTTP 請求處理、數據驗證、格式轉換、API 文檔生成
- **Business**: 業務規則實現、領域模型管理、事務管理、用例編排
- **Persistence**: 數據持久化、查詢實現、實體關係管理、數據庫事務
- **Infrastructure**: 系統配置、安全控制、全域異常處理、外部系統整合
- **Shared**: 跨層共用常數、枚舉、工具類、通用驗證規則

## SOLID 設計原則

### 關鍵實踐
1. **單一職責原則 (SRP)**: 每個類別只負責一個職責
2. **開放封閉原則 (OCP)**: 對擴展開放，對修改封閉
3. **里氏替換原則 (LSP)**: 子類型必須能夠替換其基類型
4. **介面隔離原則 (ISP)**: 客戶端不應依賴它不使用的介面
5. **依賴反轉原則 (DIP)**: 依賴抽象而非具體實現

## BDD 測試方法論

### 測試結構
```
src/test/
├── java/com/holdem/
│   ├── bdd/           # BDD 測試
│   │   ├── steps/     # Step Definitions
│   │   ├── runner/    # Test Runner
│   │   └── config/    # Test Configuration
│   ├── integration/   # 整合測試
│   └── unit/         # 單元測試
└── resources/
    ├── features/      # Cucumber Feature 文件
    └── test-data/     # 測試數據
```

### BDD 測試流程
1. **編寫 Feature 文件**: 描述業務行為場景
2. **實現 Step Definitions**: 將 Gherkin 步驟轉換為 Java 代碼
3. **領域模型設計**: 設計核心業務物件
4. **分層實現**: 由內而外實現各層功能
5. **驗證測試**: 確保所有場景通過

## 代碼品質標準

### 命名規範
```java
// 類別命名：PascalCase
public class GameService {}
public class HandEvaluator {}

// 方法命名：camelCase，動詞開頭
public void createGame() {}
public boolean isValidHand() {}
public Game findGameById() {}

// 變數命名：camelCase
private int playerCount;
private List<Card> communityCards;

// 常數命名：UPPER_SNAKE_CASE
public static final int MAX_PLAYERS = 10;
public static final String INVALID_GAME_STATE = "Invalid game state";

// 套件命名：小寫，點號分隔
package com.holdem.business.service;
package com.holdem.persistence.entity;
```

### 品質指標
- **測試覆蓋率**: 單元測試 ≥ 85%, 整合測試 ≥ 70%
- **代碼重複率**: ≤ 3%
- **圈複雜度**: 方法 ≤ 10, 類別 ≤ 50
- **技術債務比率**: ≤ 5%
- **代碼異味**: Critical = 0, Major ≤ 5

### BDD 測試品質
- **場景覆蓋率**: 所有業務場景 100% 覆蓋
- **步驟重用率**: ≥ 60%
- **測試執行時間**: BDD 測試套件 ≤ 10 分鐘
- **測試穩定性**: 成功率 ≥ 95%

## 建構與部署

### Gradle 設定重點
```gradle
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    // Spring Boot 核心
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    
    // BDD 測試
    testImplementation 'io.cucumber:cucumber-java:7.18.0'
    testImplementation 'io.cucumber:cucumber-spring:7.18.0'
    testImplementation 'io.cucumber:cucumber-junit-platform-engine:7.18.0'
}
```

### CI/CD 檢查點
- [ ] 所有測試通過 (單元測試 + BDD 測試)
- [ ] 代碼覆蓋率達標
- [ ] 靜態代碼分析通過
- [ ] 安全掃描無高危問題
- [ ] 效能測試通過
- [ ] 文檔更新

## 開發工作流程

### 功能開發流程
1. **建立 Feature Branch**: `git checkout -b feature/{feature-name}`
2. **編寫 BDD 場景**: 先寫 `.feature` 文件描述業務行為
3. **實現 Step Definitions**: 編寫步驟定義連接 Gherkin 與 Java
4. **領域模型設計**: 設計核心業務物件和規則
5. **三層架構實現**: 由業務層開始，向外實現各層
6. **單元測試編寫**: 確保代碼品質和覆蓋率
7. **整合測試驗證**: 端到端驗證功能完整性
8. **代碼審查**: Pull Request Review
9. **合併主分支**: 通過所有檢查後合併

### 測試驅動開發循環
```
BDD Scenario (Red) → Step Definition (Red) → Domain Logic (Green) → Refactor → Repeat
```

## 安全與效能標準

### 安全要求
- **輸入驗證**: 所有外部輸入 100% 驗證
- **SQL 注入防護**: 使用參數化查詢
- **XSS 防護**: 輸出編碼
- **認證授權**: JWT Token 機制

### 效能指標
- **API 響應時間**: P95 ≤ 200ms
- **記憶體使用率**: ≤ 70%
- **並發處理能力**: ≥ 100 concurrent games
- **資料庫查詢時間**: ≤ 50ms

## 文檔與註解標準

### JavaDoc 規範
```java
/**
 * 德州撲克遊戲服務類別
 * 負責遊戲的創建、管理和狀態控制
 * 
 * @author Development Team
 * @version 1.0
 * @since 2025-10-18
 */
@Service
@Transactional
public class GameService {
    
    /**
     * 創建新的德州撲克遊戲
     * 
     * @param command 創建遊戲的命令物件，包含玩家數量、盲注等資訊
     * @return 創建成功的遊戲物件
     * @throws InvalidGameConfigurationException 當遊戲配置無效時拋出
     */
    public Game createGame(@NonNull CreateGameCommand command) {
        // 實現邏輯
    }
}
```

## 異常處理策略

### 分層異常處理
- **Business Layer**: 拋出業務異常，清晰表達業務規則違反
- **Infrastructure Layer**: 統一異常處理，轉換為 HTTP 響應
- **Presentation Layer**: 格式化錯誤訊息，提供用戶友好的回應

### 自定義異常體系
```java
// 基礎業務異常
public class GameException extends RuntimeException {
    private final String errorCode;
    // ... 實現細節
}

// 具體業務異常
public class InvalidGameConfigurationException extends GameException {
    public InvalidGameConfigurationException(String message) {
        super("INVALID_GAME_CONFIGURATION", message);
    }
}
```

---

遵循以上規範和指引，確保團隊能以一致的方式開發高品質、可維護的 Texas Hold'em 撲克系統，並符合現代軟體開發最佳實踐。