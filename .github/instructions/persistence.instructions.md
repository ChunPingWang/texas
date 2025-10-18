---
applyTo: '**/persistence/**'
---

# Persistence Layer 開發規範

## 架構職責
負責數據存取、實體映射、查詢實現

## 代碼實作規範

### Entity 實作模式
```java
@Entity
@Table(name = "games")
public class GameEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "game_state", nullable = false)
    private GameState gameState;
    
    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL)
    private List<PlayerEntity> players = new ArrayList<>();
    
    // constructors, getters, setters
}
```

### Repository 實作模式
```java
@Repository
public interface GameRepository extends JpaRepository<GameEntity, Long> {
    
    @Query("SELECT g FROM GameEntity g WHERE g.gameState = :state")
    List<GameEntity> findByGameState(@Param("state") GameState state);
    
    @Query("SELECT g FROM GameEntity g JOIN FETCH g.players WHERE g.id = :gameId")
    Optional<GameEntity> findByIdWithPlayers(@Param("gameId") Long gameId);
}
```

## 層級職責
- 數據持久化
- 查詢實現
- 實體關係管理
- 數據庫事務

## 設計原則
1. Entity 只負責數據映射，不包含業務邏輯
2. Repository 介面定義清晰的數據訪問契約
3. 適當使用 Fetch 策略避免 N+1 問題
4. 數據庫約束與 Java 驗證並行
5. 查詢效能優化考量

## 命名規範
- Entity 類別：`{Domain}Entity`
- Repository 介面：`{Domain}Repository`
- Mapper 類別：`{Domain}Mapper`

## 實體設計規範
```java
@Entity
@Table(name = "players", 
       indexes = {
           @Index(name = "idx_player_game_id", columnList = "game_id"),
           @Index(name = "idx_player_seat_number", columnList = "seat_number")
       })
public class PlayerEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "player_name", nullable = false, length = 50)
    private String playerName;
    
    @Column(name = "seat_number", nullable = false)
    private Integer seatNumber;
    
    @Column(name = "chip_count", nullable = false)
    private BigDecimal chipCount;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private GameEntity game;
}
```

## 查詢效能規範
- 使用 @Query 進行複雜查詢
- JOIN FETCH 避免懶載入問題
- 適當建立資料庫索引
- 分頁查詢大數據集
- 避免 SELECT N+1 問題

## 事務管理
```java
@Repository
@Transactional(readOnly = true)
public class GameRepositoryImpl {
    
    @Transactional
    public Game save(Game game) {
        // 寫入操作使用寫事務
    }
    
    public Optional<Game> findById(Long id) {
        // 讀取操作使用只讀事務
    }
}
```