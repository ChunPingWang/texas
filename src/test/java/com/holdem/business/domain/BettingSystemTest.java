package com.holdem.business.domain;

import com.holdem.shared.enums.GameState;
import com.holdem.shared.enums.PlayerStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 下注系統單元測試
 * 測試各種下注行動和規則驗證
 */
@DisplayName("下注系統測試")
class BettingSystemTest {

    private Game game;
    private BigDecimal smallBlind = new BigDecimal("10");
    private BigDecimal bigBlind = new BigDecimal("20");

    @BeforeEach
    void setUp() {
        game = new Game(smallBlind, bigBlind);
    }

    @Nested
    @DisplayName("玩家行動能力檢查")
    class PlayerActionValidationTests {

        @Test
        @DisplayName("檢查玩家棄牌能力")
        void shouldValidatePlayerCanFold() {
            // Given - 設置遊戲和玩家
            setupGameWithPlayers(3);
            game.initialize();
            game.startNewHand();
            
            Player currentPlayer = game.getCurrentPlayer();
            
            // When & Then
            assertTrue(game.canPlayerFold(currentPlayer), "Current player should be able to fold");
            assertFalse(game.canPlayerFold(null), "Null player should not be able to fold");
            
            // 測試非當前玩家
            Player otherPlayer = game.getPlayers().stream()
                .filter(p -> p != currentPlayer)
                .findFirst()
                .orElse(null);
            assertFalse(game.canPlayerFold(otherPlayer), "Non-current player should not be able to fold");
        }

        @Test
        @DisplayName("檢查玩家跟注能力")
        void shouldValidatePlayerCanCall() {
            // Given
            setupGameWithPlayers(3);
            game.initialize();
            game.startNewHand();
            
            Player currentPlayer = game.getCurrentPlayer();
            
            // When & Then
            assertTrue(game.canPlayerCall(currentPlayer), "Current player should be able to call");
            
            // 測試籌碼為0的玩家
            currentPlayer.setChipCount(BigDecimal.ZERO);
            assertFalse(game.canPlayerCall(currentPlayer), "Player with no chips should not be able to call");
        }

        @Test
        @DisplayName("檢查玩家加注能力")
        void shouldValidatePlayerCanRaise() {
            // Given
            setupGameWithPlayers(3);
            game.initialize();
            game.startNewHand();
            
            Player currentPlayer = game.getCurrentPlayer();
            
            // When & Then
            assertTrue(game.canPlayerRaise(currentPlayer), "Current player should be able to raise");
            
            // 測試籌碼不足的玩家
            currentPlayer.setChipCount(new BigDecimal("10")); // 少於最小加注
            assertFalse(game.canPlayerRaise(currentPlayer), "Player with insufficient chips should not be able to raise");
        }

        @Test
        @DisplayName("檢查玩家全押能力")
        void shouldValidatePlayerCanAllIn() {
            // Given
            setupGameWithPlayers(3);
            game.initialize();
            game.startNewHand();
            
            Player currentPlayer = game.getCurrentPlayer();
            
            // When & Then
            assertTrue(game.canPlayerAllIn(currentPlayer), "Current player should be able to go all-in");
            
            // 測試已全押的玩家
            currentPlayer.setStatus(PlayerStatus.ALL_IN);
            assertFalse(game.canPlayerAllIn(currentPlayer), "Already all-in player should not be able to go all-in again");
        }
    }

    @Nested
    @DisplayName("棄牌行動測試")
    class FoldActionTests {

        @Test
        @DisplayName("成功執行棄牌")
        void shouldExecuteFoldSuccessfully() {
            // Given
            setupGameWithPlayers(3);
            game.initialize();
            game.startNewHand();
            
            Player currentPlayer = game.getCurrentPlayer();
            Player originalCurrentPlayer = currentPlayer; // 保存原始當前玩家的引用
            
            // When
            boolean result = game.playerFold(currentPlayer);
            
            // Then
            assertTrue(result, "Fold action should succeed");
            assertEquals(PlayerStatus.FOLDED, originalCurrentPlayer.getStatus(), "Player should be folded");
            assertNotEquals(originalCurrentPlayer, game.getCurrentPlayer(), "Should move to next player");
            assertFalse(game.isPlayerInGame(originalCurrentPlayer), "Folded player should not be in game");
        }

        @Test
        @DisplayName("棄牌後不影響底池")
        void shouldNotAffectPotAfterFold() {
            // Given
            setupGameWithPlayers(3);
            game.initialize();
            game.startNewHand();
            
            BigDecimal originalPot = game.getCurrentPot();
            Player currentPlayer = game.getCurrentPlayer();
            
            // When
            game.playerFold(currentPlayer);
            
            // Then
            assertEquals(originalPot, game.getCurrentPot(), "Pot should not change after fold");
        }
    }

    @Nested
    @DisplayName("跟注行動測試")
    class CallActionTests {

        @Test
        @DisplayName("成功執行跟注")
        void shouldExecuteCallSuccessfully() {
            // Given
            setupGameWithPlayers(3);
            game.initialize();
            game.startNewHand();
            
            Player currentPlayer = game.getCurrentPlayer();
            BigDecimal originalChips = currentPlayer.getChipCount();
            BigDecimal originalPot = game.getCurrentPot();
            BigDecimal callAmount = game.getCallAmount(currentPlayer);
            
            // When
            boolean result = game.playerCall(currentPlayer);
            
            // Then
            assertTrue(result, "Call action should succeed");
            assertEquals(originalChips.subtract(callAmount), currentPlayer.getChipCount(), 
                        "Player chips should decrease by call amount");
            assertEquals(originalPot.add(callAmount), game.getCurrentPot(), 
                        "Pot should increase by call amount");
            assertEquals(PlayerStatus.ACTIVE, currentPlayer.getStatus(), 
                        "Player should remain active after call");
        }

        @Test
        @DisplayName("跟注金額計算正確")
        void shouldCalculateCallAmountCorrectly() {
            // Given
            setupGameWithPlayers(3);
            game.initialize();
            game.startNewHand();
            
            Player currentPlayer = game.getCurrentPlayer();
            
            // When - 當前下注是大盲注 20，玩家還沒下注
            BigDecimal callAmount = game.getCallAmount(currentPlayer);
            
            // Then
            assertEquals(bigBlind, callAmount, "Call amount should equal big blind for first player");
        }

        @Test
        @DisplayName("籌碼不足時自動全押")
        void shouldAllInWhenInsufficientChipsForCall() {
            // Given
            setupGameWithPlayers(3);
            game.initialize();
            game.startNewHand();
            
            Player currentPlayer = game.getCurrentPlayer();
            currentPlayer.setChipCount(new BigDecimal("10")); // 少於大盲注
            
            // When
            boolean result = game.playerCall(currentPlayer);
            
            // Then
            assertTrue(result, "Call should succeed");
            assertEquals(PlayerStatus.ALL_IN, currentPlayer.getStatus(), "Player should be all-in");
            assertEquals(BigDecimal.ZERO, currentPlayer.getChipCount(), "Player should have no chips left");
        }
    }

    @Nested
    @DisplayName("加注行動測試")
    class RaiseActionTests {

        @Test
        @DisplayName("成功執行加注")
        void shouldExecuteRaiseSuccessfully() {
            // Given
            setupGameWithPlayers(3);
            game.initialize();
            game.startNewHand();
            
            Player currentPlayer = game.getCurrentPlayer();
            BigDecimal raiseAmount = new BigDecimal("50");
            BigDecimal originalChips = currentPlayer.getChipCount();
            BigDecimal originalPot = game.getCurrentPot();
            
            // When
            boolean result = game.playerRaise(currentPlayer, raiseAmount);
            
            // Then
            assertTrue(result, "Raise action should succeed");
            assertEquals(raiseAmount, game.getCurrentBet(), "Current bet should be updated to raise amount");
            assertTrue(originalPot.compareTo(game.getCurrentPot()) < 0, "Pot should increase");
            assertTrue(originalChips.compareTo(currentPlayer.getChipCount()) > 0, "Player chips should decrease");
        }

        @Test
        @DisplayName("加注金額太小應該拒絕")
        void shouldRejectRaiseThatTooSmall() {
            // Given
            setupGameWithPlayers(3);
            game.initialize();
            game.startNewHand();
            
            Player currentPlayer = game.getCurrentPlayer();
            // 最小加注增量是 bigBlind = 20，當前下注是 20，所以最小加注到的金額是 20 + 20 = 40
            BigDecimal tooSmallRaise = new BigDecimal("30"); // 小於最小加注到的金額 40
            
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                game.playerRaise(currentPlayer, tooSmallRaise);
            }, "Should reject raise amount that's too small");
        }

        @Test
        @DisplayName("最小加注金額計算正確")
        void shouldCalculateMinimumRaiseCorrectly() {
            // Given
            setupGameWithPlayers(3);
            game.initialize();
            game.startNewHand();
            
            // When
            BigDecimal minRaise = game.getMinimumRaise();
            
            // Then
            assertEquals(bigBlind, minRaise, 
                        "Minimum raise increment should be big blind");
        }
    }

    @Nested
    @DisplayName("全押行動測試")
    class AllInActionTests {

        @Test
        @DisplayName("成功執行全押")
        void shouldExecuteAllInSuccessfully() {
            // Given
            setupGameWithPlayers(3);
            game.initialize();
            game.startNewHand();
            
            Player currentPlayer = game.getCurrentPlayer();
            BigDecimal originalChips = currentPlayer.getChipCount();
            BigDecimal originalPot = game.getCurrentPot();
            
            // When
            boolean result = game.playerAllIn(currentPlayer);
            
            // Then
            assertTrue(result, "All-in action should succeed");
            assertEquals(PlayerStatus.ALL_IN, currentPlayer.getStatus(), "Player should be all-in");
            assertEquals(BigDecimal.ZERO, currentPlayer.getChipCount(), "Player should have no chips left");
            assertEquals(originalPot.add(originalChips), game.getCurrentPot(), 
                        "Pot should increase by all player's chips");
        }

        @Test
        @DisplayName("全押金額大於當前下注時更新當前下注")
        void shouldUpdateCurrentBetWhenAllInExceedsCurrentBet() {
            // Given
            setupGameWithPlayers(3);
            game.initialize();
            game.startNewHand();
            
            Player currentPlayer = game.getCurrentPlayer();
            currentPlayer.setChipCount(new BigDecimal("100")); // 大於當前下注
            BigDecimal originalCurrentBet = game.getCurrentBet();
            
            // When
            game.playerAllIn(currentPlayer);
            
            // Then
            assertTrue(game.getCurrentBet().compareTo(originalCurrentBet) > 0, 
                      "Current bet should increase when all-in exceeds current bet");
        }
    }

    @Nested
    @DisplayName("下注行動測試")
    class BetActionTests {

        @Test
        @DisplayName("拒絕無效下注金額")
        void shouldRejectInvalidBetAmount() {
            // Given
            setupGameWithPlayers(3);
            game.initialize();
            game.startNewHand();
            
            Player currentPlayer = game.getCurrentPlayer();
            
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                game.playerBet(currentPlayer, BigDecimal.ZERO);
            }, "Should reject zero bet amount");
            
            assertThrows(IllegalArgumentException.class, () -> {
                game.playerBet(currentPlayer, new BigDecimal("-10"));
            }, "Should reject negative bet amount");
        }

        @Test
        @DisplayName("籌碼不足時拋出異常")
        void shouldThrowExceptionWhenInsufficientChips() {
            // Given
            setupGameWithPlayers(3);
            game.initialize();
            game.startNewHand();
            
            Player currentPlayer = game.getCurrentPlayer();
            currentPlayer.setChipCount(new BigDecimal("10"));
            
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                game.playerBet(currentPlayer, new BigDecimal("100"));
            }, "Should throw exception when bet exceeds chips");
        }
    }

    @Nested
    @DisplayName("下注輪次管理測試")
    class BettingRoundManagementTests {

        @Test
        @DisplayName("檢查下注輪次完成條件")
        void shouldDetectBettingRoundCompletion() {
            // Given
            setupGameWithPlayers(3);
            game.initialize();
            game.startNewHand();
            
            // 讓所有玩家都跟注
            while (!game.isBettingRoundComplete()) {
                Player currentPlayer = game.getCurrentPlayer();
                if (currentPlayer != null && game.canPlayerCall(currentPlayer)) {
                    game.playerCall(currentPlayer);
                } else {
                    break;
                }
            }
            
            // Then
            assertTrue(game.isBettingRoundComplete(), "Betting round should be complete");
        }

        @Test
        @DisplayName("只剩一位玩家時下注輪次結束")
        void shouldEndBettingRoundWithOnePlayer() {
            // Given
            setupGameWithPlayers(3);
            game.initialize();
            game.startNewHand();
            
            // 讓兩個玩家棄牌
            Player player1 = game.getCurrentPlayer();
            game.playerFold(player1);
            
            Player player2 = game.getCurrentPlayer();
            game.playerFold(player2);
            
            // Then
            assertTrue(game.isBettingRoundComplete(), "Betting round should end with only one active player");
        }
    }

    @Nested
    @DisplayName("Player 類下注方法測試")
    class PlayerBettingMethodTests {

        private Player player;

        @BeforeEach
        void setUpPlayer() {
            player = new Player("TestPlayer", new BigDecimal("1000"));
            player.setStatus(PlayerStatus.ACTIVE); // 設定為活躍狀態以進行下注測試
        }

        @Test
        @DisplayName("canAfford 方法測試")
        void shouldTestCanAffordMethod() {
            assertTrue(player.canAfford(new BigDecimal("500")), "Player should afford 500");
            assertTrue(player.canAfford(new BigDecimal("1000")), "Player should afford all chips");
            assertFalse(player.canAfford(new BigDecimal("1001")), "Player should not afford more than chips");
            
            assertThrows(IllegalArgumentException.class, () -> {
                player.canAfford(new BigDecimal("-10"));
            }, "Should reject negative amount");
        }

        @Test
        @DisplayName("fold 方法測試")
        void shouldTestFoldMethod() {
            player.fold();
            assertEquals(PlayerStatus.FOLDED, player.getStatus(), "Player should be folded");
        }

        @Test
        @DisplayName("call 方法測試")
        void shouldTestCallMethod() {
            BigDecimal callAmount = new BigDecimal("100");
            BigDecimal originalChips = player.getChipCount();
            
            BigDecimal paidAmount = player.call(callAmount);
            
            assertEquals(callAmount, paidAmount, "Should return paid amount");
            assertEquals(originalChips.subtract(callAmount), player.getChipCount(), 
                        "Chips should decrease by call amount");
            assertEquals(callAmount, player.getCurrentBet(), "Current bet should be updated");
        }

        @Test
        @DisplayName("bet 方法測試")
        void shouldTestBetMethod() {
            BigDecimal betAmount = new BigDecimal("200");
            BigDecimal originalChips = player.getChipCount();
            
            BigDecimal paidAmount = player.bet(betAmount);
            
            assertEquals(betAmount, paidAmount, "Should return bet amount");
            assertEquals(originalChips.subtract(betAmount), player.getChipCount(), 
                        "Chips should decrease by bet amount");
            assertEquals(betAmount, player.getCurrentBet(), "Current bet should be updated");
        }

        @Test
        @DisplayName("raise 方法測試")
        void shouldTestRaiseMethod() {
            // 先設置一些當前下注
            player.setCurrentBet(new BigDecimal("50"));
            BigDecimal raiseToAmount = new BigDecimal("150");
            BigDecimal additionalAmount = raiseToAmount.subtract(player.getCurrentBet());
            BigDecimal originalChips = player.getChipCount();
            
            BigDecimal paidAmount = player.raise(raiseToAmount);
            
            assertEquals(additionalAmount, paidAmount, "Should return additional amount paid");
            assertEquals(originalChips.subtract(additionalAmount), player.getChipCount(), 
                        "Chips should decrease by additional amount");
            assertEquals(raiseToAmount, player.getCurrentBet(), "Current bet should be updated to raise amount");
        }

        @Test
        @DisplayName("allIn 方法測試")
        void shouldTestAllInMethod() {
            BigDecimal originalChips = player.getChipCount();
            
            BigDecimal allInAmount = player.allIn();
            
            assertEquals(originalChips, allInAmount, "Should return all chips");
            assertEquals(BigDecimal.ZERO, player.getChipCount(), "Should have no chips left");
            assertEquals(PlayerStatus.ALL_IN, player.getStatus(), "Should be all-in");
        }

        @Test
        @DisplayName("canPerformAction 方法測試")
        void shouldTestCanPerformActionMethod() {
            assertTrue(player.canPerformAction("fold"), "Active player should be able to fold");
            assertTrue(player.canPerformAction("call"), "Active player should be able to call");
            assertTrue(player.canPerformAction("raise"), "Active player should be able to raise");
            assertTrue(player.canPerformAction("allin"), "Active player should be able to go all-in");
            
            player.setStatus(PlayerStatus.FOLDED);
            assertFalse(player.canPerformAction("call"), "Folded player should not be able to call");
        }
    }

    // 輔助方法
    private void setupGameWithPlayers(int playerCount) {
        for (int i = 1; i <= playerCount; i++) {
            Player player = new Player("Player" + i, new BigDecimal("1000"));
            game.addPlayer(player);
        }
    }

    private Player getNextActivePlayer(Player currentPlayer) {
        int currentIndex = game.getPlayers().indexOf(currentPlayer);
        int nextIndex = (currentIndex + 1) % game.getPlayers().size();
        return game.getPlayers().get(nextIndex);
    }
}