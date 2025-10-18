package com.holdem.business.domain;

import com.holdem.shared.enums.GameState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 發牌系統相關的單元測試
 * 
 * @author Development Team
 * @version 1.0
 * @since 2025-10-18
 */
@DisplayName("發牌系統單元測試")
class DealingSystemTest {
    
    private Game game;
    private final BigDecimal smallBlind = new BigDecimal("10");
    private final BigDecimal bigBlind = new BigDecimal("20");
    
    @BeforeEach
    void setUp() {
        game = new Game(smallBlind, bigBlind);
        
        // 加入4位玩家
        for (int i = 1; i <= 4; i++) {
            Player player = new Player("Player" + i, new BigDecimal("1000"));
            game.addPlayer(player);
        }
        
        game.initialize();
        game.startNewHand(); // 到達 PRE_FLOP 狀態，已發放底牌
    }
    
    @Test
    @DisplayName("底牌發放測試")
    void shouldDealHoleCardsCorrectly() {
        // Given - 遊戲已開始，玩家應該收到底牌
        assertEquals(GameState.PRE_FLOP, game.getState());
        
        // When & Then - 驗證每位玩家都收到2張底牌
        game.getActivePlayers().forEach(player -> {
            assertNotNull(player.getHoleCards(), "Player should have hole cards");
            assertEquals(2, player.getHoleCards().size(), "Player should have exactly 2 hole cards");
        });
        
        // 驗證牌組剩餘張數
        int expectedRemainingCards = 52 - (game.getActivePlayers().size() * 2);
        assertEquals(expectedRemainingCards, game.getDeck().getRemainingCards(), 
                    "Correct number of cards should remain in deck");
    }
    
    @Test
    @DisplayName("翻牌發放測試")
    void shouldDealFlopCorrectly() {
        // Given - 遊戲在 PRE_FLOP 狀態
        assertEquals(GameState.PRE_FLOP, game.getState());
        
        // When - 發放翻牌
        game.dealFlop();
        
        // Then - 驗證翻牌狀態和公共牌
        assertEquals(GameState.FLOP, game.getState());
        assertEquals(3, game.getCommunityCards().size(), "Should have 3 community cards after flop");
        
        // 驗證銷牌和發牌後的牌組數量
        int initialCards = 52;
        int holeCards = game.getActivePlayers().size() * 2;
        int burnCards = 1; // 翻牌前銷牌
        int flopCards = 3;
        int expectedRemaining = initialCards - holeCards - burnCards - flopCards;
        
        assertEquals(expectedRemaining, game.getDeck().getRemainingCards(), 
                    "Correct number of cards should remain after flop");
    }
    
    @Test
    @DisplayName("轉牌發放測試")
    void shouldDealTurnCorrectly() {
        // Given - 遊戲在翻牌狀態
        game.dealFlop();
        assertEquals(GameState.FLOP, game.getState());
        assertEquals(3, game.getCommunityCards().size());
        
        // When - 發放轉牌
        game.dealTurn();
        
        // Then - 驗證轉牌狀態和公共牌
        assertEquals(GameState.TURN, game.getState());
        assertEquals(4, game.getCommunityCards().size(), "Should have 4 community cards after turn");
    }
    
    @Test
    @DisplayName("河牌發放測試")
    void shouldDealRiverCorrectly() {
        // Given - 遊戲在轉牌狀態
        game.dealFlop();
        game.dealTurn();
        assertEquals(GameState.TURN, game.getState());
        assertEquals(4, game.getCommunityCards().size());
        
        // When - 發放河牌
        game.dealRiver();
        
        // Then - 驗證河牌狀態和公共牌
        assertEquals(GameState.RIVER, game.getState());
        assertEquals(5, game.getCommunityCards().size(), "Should have 5 community cards after river");
    }
    
    @Test
    @DisplayName("發牌狀態轉換測試")
    void shouldTransitionStateCorrectlyThroughDealingPhases() {
        // 驗證完整的發牌狀態轉換
        assertEquals(GameState.PRE_FLOP, game.getState());
        
        game.dealFlop();
        assertEquals(GameState.FLOP, game.getState());
        
        game.dealTurn();
        assertEquals(GameState.TURN, game.getState());
        
        game.dealRiver();
        assertEquals(GameState.RIVER, game.getState());
    }
    
    @Test
    @DisplayName("非法狀態發牌測試")
    void shouldThrowExceptionForInvalidStateTransitions() {
        // 嘗試在錯誤狀態下發牌
        assertThrows(IllegalStateException.class, () -> {
            game.dealTurn(); // 在 PRE_FLOP 狀態下直接發轉牌
        });
        
        game.dealFlop();
        assertThrows(IllegalStateException.class, () -> {
            game.dealRiver(); // 在 FLOP 狀態下直接發河牌
        });
    }
    
    @Test
    @DisplayName("牌不足錯誤處理測試")
    void shouldHandleInsufficientCardsError() {
        // Given - 消耗大部分牌
        while (game.getDeck().getRemainingCards() > 3) {
            game.getDeck().dealCard();
        }
        
        // When & Then - 嘗試發翻牌時應該拋出錯誤
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            game.dealFlop();
        });
        
        assertTrue(exception.getMessage().contains("牌組不足"), 
                  "Should throw insufficient cards error");
    }
    
    @Test
    @DisplayName("發牌錯誤處理測試")
    void shouldHandleDealingErrors() {
        // When & Then - 發牌錯誤應該拋出異常
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            game.handleDealingError("測試錯誤");
        });
        
        assertTrue(exception.getMessage().contains("發牌錯誤"), 
                  "Should throw dealing error exception");
        assertTrue(exception.getMessage().contains("測試錯誤"), 
                  "Should include original error message");
    }
}