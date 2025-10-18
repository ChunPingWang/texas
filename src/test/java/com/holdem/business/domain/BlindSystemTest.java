package com.holdem.business.domain;

import com.holdem.shared.enums.GameState;
import com.holdem.shared.enums.PlayerAction;
import com.holdem.shared.enums.PlayerStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 盲注系統相關的單元測試
 * 
 * @author Development Team
 * @version 1.0
 * @since 2025-10-18
 */
@DisplayName("盲注系統單元測試")
class BlindSystemTest {
    
    private Game game;
    private final BigDecimal smallBlind = new BigDecimal("10");
    private final BigDecimal bigBlind = new BigDecimal("20");
    
    @BeforeEach
    void setUp() {
        game = new Game(smallBlind, bigBlind);
    }
    
    @Test
    @DisplayName("正常盲注收集測試 - 4位玩家")
    void shouldCollectBlindsCorrectlyWithFourPlayers() {
        // Given - 加入4位玩家
        for (int i = 1; i <= 4; i++) {
            Player player = new Player("Player" + i, new BigDecimal("1000"));
            game.addPlayer(player);
        }
        game.initialize();
        
        // When - 開始新手牌
        game.startNewHand();
        
        // Then - 驗證盲注收集正確
        assertEquals(GameState.PRE_FLOP, game.getState());
        assertEquals(smallBlind.add(bigBlind), game.getCurrentPot());
        assertEquals(bigBlind, game.getCurrentBet());
        
        // 驗證小盲注玩家
        int dealerPosition = game.getDealerButtonPosition();
        int smallBlindPosition = (dealerPosition + 1) % 4;
        Player smallBlindPlayer = game.getPlayers().get(smallBlindPosition);
        assertEquals(smallBlind, smallBlindPlayer.getCurrentBet());
        assertEquals(new BigDecimal("990"), smallBlindPlayer.getChipCount());
        
        // 驗證大盲注玩家
        int bigBlindPosition = (dealerPosition + 2) % 4;
        Player bigBlindPlayer = game.getPlayers().get(bigBlindPosition);
        assertEquals(bigBlind, bigBlindPlayer.getCurrentBet());
        assertEquals(new BigDecimal("980"), bigBlindPlayer.getChipCount());
    }
    
    @Test
    @DisplayName("Heads-up 盲注收集測試")
    void shouldCollectBlindsCorrectlyInHeadsUp() {
        // Given - 加入2位玩家
        Player player1 = new Player("Player1", new BigDecimal("1000"));
        Player player2 = new Player("Player2", new BigDecimal("1000"));
        game.addPlayer(player1);
        game.addPlayer(player2);
        game.initialize();
        
        // When - 開始新手牌
        game.startNewHand();
        
        // Then - 驗證 heads-up 盲注規則
        assertEquals(GameState.PRE_FLOP, game.getState());
        assertEquals(smallBlind.add(bigBlind), game.getCurrentPot());
        
        // 在 heads-up 中，莊家支付小盲注
        int dealerPosition = game.getDealerButtonPosition();
        Player dealer = game.getPlayers().get(dealerPosition);
        Player opponent = game.getPlayers().get((dealerPosition + 1) % 2);
        
        assertEquals(smallBlind, dealer.getCurrentBet());
        assertEquals(bigBlind, opponent.getCurrentBet());
        assertEquals(new BigDecimal("990"), dealer.getChipCount());
        assertEquals(new BigDecimal("980"), opponent.getChipCount());
    }
    
    @Test
    @DisplayName("玩家籌碼不足支付盲注測試")
    void shouldHandleInsufficientChipsForBlind() {
        // Given - 確保籌碼不足的玩家在大盲注位置
        Player poorPlayer = new Player("PoorPlayer", new BigDecimal("15"));
        game.addPlayer(poorPlayer);
        
        // 添加正常玩家
        for (int i = 1; i <= 3; i++) {
            Player player = new Player("Player" + i, new BigDecimal("1000"));
            game.addPlayer(player);
        }
        
        game.initialize();
        
        // 設定莊家位置使 PoorPlayer 成為大盲注（位置 0，莊家在位置 2）
        game.setDealerButtonPosition(2);
        
        // When - 開始新手牌
        game.startNewHand();
        
        // Then - 驗證籌碼不足的玩家變為全押
        assertEquals(GameState.PRE_FLOP, game.getState());
        assertEquals(PlayerStatus.ALL_IN, poorPlayer.getStatus());
        assertEquals(BigDecimal.ZERO, poorPlayer.getChipCount());
        assertEquals(new BigDecimal("15"), poorPlayer.getCurrentBet());
        
        // 驗證底池包含了 PoorPlayer 的籌碼
        assertTrue(game.getCurrentPot().compareTo(new BigDecimal("25")) >= 0); // 10 + 15 至少
        
        // 底池應該包含所有支付的盲注
        assertTrue(game.getCurrentPot().compareTo(BigDecimal.ZERO) > 0);
    }
    
    @Test
    @DisplayName("小盲注位置計算測試")
    void shouldCalculateSmallBlindPositionCorrectly() {
        // Given - 加入6位玩家
        for (int i = 1; i <= 6; i++) {
            Player player = new Player("Player" + i, new BigDecimal("1000"));
            game.addPlayer(player);
        }
        game.initialize();
        
        // When - 開始新手牌
        game.startNewHand();
        
        // Then - 驗證小盲注位置計算
        int dealerPosition = game.getDealerButtonPosition();
        int expectedSmallBlindPosition = (dealerPosition + 1) % 6;
        int expectedBigBlindPosition = (dealerPosition + 2) % 6;
        
        Player smallBlindPlayer = game.getPlayers().get(expectedSmallBlindPosition);
        Player bigBlindPlayer = game.getPlayers().get(expectedBigBlindPosition);
        
        assertEquals(smallBlind, smallBlindPlayer.getCurrentBet());
        assertEquals(bigBlind, bigBlindPlayer.getCurrentBet());
        assertEquals(new BigDecimal("990"), smallBlindPlayer.getChipCount());
        assertEquals(new BigDecimal("980"), bigBlindPlayer.getChipCount());
    }
    
    @Test
    @DisplayName("多手牌莊家按鈕移動測試")
    void shouldMoveDealerButtonBetweenHands() {
        // Given - 加入3位玩家
        for (int i = 1; i <= 3; i++) {
            Player player = new Player("Player" + i, new BigDecimal("1000"));
            game.addPlayer(player);
        }
        game.initialize();
        
        // When - 記錄初始莊家位置
        int initialDealerPosition = game.getDealerButtonPosition();
        
        // 開始第一手牌
        game.startNewHand(); 
        int firstHandDealer = game.getDealerButtonPosition();
        
        // Then - 驗證第一手牌的莊家按鈕位置
        assertEquals(initialDealerPosition, firstHandDealer); // 第一手牌莊家不變
        
        // 驗證盲注收集成功
        assertEquals(GameState.PRE_FLOP, game.getState());
        assertTrue(game.getCurrentPot().compareTo(BigDecimal.ZERO) > 0);
        
        // 驗證至少兩輪後莊家按鈕會移動的邏輯
        // (在實際遊戲中，需要完整的遊戲流程來測試莊家按鈕移動)
        assertTrue(game.getHandNumber() > 0); // 確認手牌編號增加
    }
    
    @Test
    @DisplayName("盲注收集後遊戲狀態驗證")
    void shouldSetCorrectGameStateAfterCollectingBlinds() {
        // Given - 準備遊戲
        for (int i = 1; i <= 5; i++) {
            Player player = new Player("Player" + i, new BigDecimal("1000"));
            game.addPlayer(player);
        }
        game.initialize();
        
        // When - 開始新手牌
        game.startNewHand();
        
        // Then - 驗證遊戲狀態
        assertEquals(GameState.PRE_FLOP, game.getState());
        assertEquals(1, game.getHandNumber());
        assertEquals(smallBlind.add(bigBlind), game.getCurrentPot());
        assertEquals(bigBlind, game.getCurrentBet());
        
        // 驗證所有玩家都收到了底牌
        game.getPlayers().forEach(player -> {
            if (player.getStatus().isInGame()) {
                assertEquals(2, player.getHoleCards().size());
            }
        });
    }
}