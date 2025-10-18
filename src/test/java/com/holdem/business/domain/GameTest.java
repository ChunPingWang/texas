package com.holdem.business.domain;

import com.holdem.shared.constants.GameConstants;
import com.holdem.shared.enums.GameState;
import com.holdem.shared.enums.PlayerStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Game 領域實體的單元測試
 * 
 * @author Development Team
 * @version 1.0
 * @since 2025-10-18
 */
@DisplayName("Game 領域實體測試")
class GameTest {
    
    private Game game;
    private final BigDecimal smallBlind = new BigDecimal("10");
    private final BigDecimal bigBlind = new BigDecimal("20");
    
    @BeforeEach
    void setUp() {
        game = new Game(smallBlind, bigBlind);
    }
    
    @Test
    @DisplayName("應能成功建立遊戲實例")
    void shouldCreateGameSuccessfully() {
        // Then
        assertNotNull(game.getId());
        assertEquals(smallBlind, game.getSmallBlind());
        assertEquals(bigBlind, game.getBigBlind());
        assertEquals(GameState.WAITING_FOR_PLAYERS, game.getState());
        assertTrue(game.getPlayers().isEmpty());
        assertNotNull(game.getCreatedAt());
    }
    
    @Test
    @DisplayName("建立遊戲時盲注設定必須有效")
    void shouldValidateBlindsOnCreation() {
        // Given & When & Then
        assertThrows(NullPointerException.class, () -> {
            new Game(null, bigBlind);
        });
        
        assertThrows(NullPointerException.class, () -> {
            new Game(smallBlind, null);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            new Game(BigDecimal.ZERO, bigBlind);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            new Game(smallBlind, new BigDecimal("15")); // 大盲注必須是小盲注的兩倍
        });
    }
    
    @Test
    @DisplayName("應能成功加入玩家")
    void shouldAddPlayersSuccessfully() {
        // Given
        Player player1 = new Player("Player1", new BigDecimal("1000"));
        Player player2 = new Player("Player2", new BigDecimal("1000"));
        
        // When
        game.addPlayer(player1);
        game.addPlayer(player2);
        
        // Then
        assertEquals(2, game.getPlayers().size());
        assertEquals(1, player1.getSeatNumber());
        assertEquals(2, player2.getSeatNumber());
        assertEquals(PlayerStatus.ACTIVE, player1.getStatus());
        assertEquals(PlayerStatus.ACTIVE, player2.getStatus());
    }
    
    @Test
    @DisplayName("不應允許加入重複的玩家")
    void shouldNotAllowDuplicatePlayers() {
        // Given
        Player player = new Player("TestPlayer", new BigDecimal("1000"));
        game.addPlayer(player);
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            game.addPlayer(player);
        });
    }
    
    @Test
    @DisplayName("不應允許超過最大玩家數")
    void shouldNotAllowTooManyPlayers() {
        // Given - 加入最大數量的玩家
        for (int i = 1; i <= GameConstants.MAX_PLAYERS; i++) {
            Player player = new Player("Player" + i, new BigDecimal("1000"));
            game.addPlayer(player);
        }
        
        // When & Then
        Player extraPlayer = new Player("ExtraPlayer", new BigDecimal("1000"));
        assertThrows(IllegalStateException.class, () -> {
            game.addPlayer(extraPlayer);
        });
    }
    
    @Test
    @DisplayName("遊戲開始前不應允許加入玩家")
    void shouldNotAllowAddingPlayersAfterInitialization() {
        // Given
        Player player1 = new Player("Player1", new BigDecimal("1000"));
        Player player2 = new Player("Player2", new BigDecimal("1000"));
        game.addPlayer(player1);
        game.addPlayer(player2);
        game.initialize();
        
        // When & Then
        Player player3 = new Player("Player3", new BigDecimal("1000"));
        assertThrows(IllegalStateException.class, () -> {
            game.addPlayer(player3);
        });
    }
    
    @Test
    @DisplayName("應能成功初始化遊戲")
    void shouldInitializeGameSuccessfully() {
        // Given
        Player player1 = new Player("Player1", new BigDecimal("1000"));
        Player player2 = new Player("Player2", new BigDecimal("1000"));
        game.addPlayer(player1);
        game.addPlayer(player2);
        
        // When
        game.initialize();
        
        // Then
        assertEquals(GameState.INITIALIZED, game.getState());
        assertTrue(game.getDealerButtonPosition() >= 0);
        assertTrue(game.getDealerButtonPosition() < game.getPlayers().size());
        assertTrue(game.canStart());
    }
    
    @Test
    @DisplayName("玩家數量不足時不應允許初始化")
    void shouldNotInitializeWithInsufficientPlayers() {
        // Given
        Player player = new Player("Player1", new BigDecimal("1000"));
        game.addPlayer(player);
        
        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            game.initialize();
        });
    }
    
    @Test
    @DisplayName("應能檢查遊戲是否可以開始")
    void shouldCheckIfGameCanStart() {
        // Given - 少於最小玩家數
        Player player = new Player("Player1", new BigDecimal("1000"));
        game.addPlayer(player);
        
        // Then
        assertFalse(game.canStart());
        
        // When - 達到最小玩家數
        Player player2 = new Player("Player2", new BigDecimal("1000"));
        game.addPlayer(player2);
        
        // Then
        assertTrue(game.canStart());
    }
    
    @Test
    @DisplayName("應能正確識別活躍玩家")
    void shouldIdentifyActivePlayers() {
        // Given
        Player player1 = new Player("Player1", new BigDecimal("1000"));
        Player player2 = new Player("Player2", new BigDecimal("1000"));
        Player player3 = new Player("Player3", new BigDecimal("1000"));
        
        game.addPlayer(player1);
        game.addPlayer(player2);
        game.addPlayer(player3);
        
        // When - 一個玩家棄牌
        player2.performAction(com.holdem.shared.enums.PlayerAction.FOLD, BigDecimal.ZERO);
        
        // Then
        assertEquals(2, game.getActivePlayers().size());
        assertTrue(game.getActivePlayers().contains(player1));
        assertFalse(game.getActivePlayers().contains(player2));
        assertTrue(game.getActivePlayers().contains(player3));
    }
}