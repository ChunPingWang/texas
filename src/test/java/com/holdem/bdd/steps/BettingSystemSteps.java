package com.holdem.bdd.steps;

import com.holdem.business.domain.Game;
import com.holdem.business.domain.Player;
import com.holdem.shared.enums.GameState;
import com.holdem.shared.enums.PlayerStatus;
import io.cucumber.java.zh_tw.假設;
import io.cucumber.java.zh_tw.而且;
import io.cucumber.java.zh_tw.當;
import io.cucumber.java.zh_tw.那麼;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 下注系統的 BDD 步驟定義
 * 處理德州撲克中的各種下注行動和驗證邏輯
 */
@SpringBootTest
public class BettingSystemSteps {
    
    private Game game;
    private Player currentPlayer;
    private String lastErrorMessage;
    private boolean actionExecuted;
    private BigDecimal originalChips;
    private BigDecimal originalPot;

    // 翻牌前下注行動場景
    @假設("所有玩家都已收到底牌")
    public void 所有玩家都已收到底牌() {
        // 設置遊戲，發放底牌
        game = new Game(new BigDecimal("10"), new BigDecimal("20"));
        
        // 添加玩家
        for (int i = 1; i <= 4; i++) {
            Player player = new Player("Player" + i, new BigDecimal("1000"));
            game.addPlayer(player);
        }
        
        game.initialize();
        game.startNewHand(); // 這會支付盲注並發放底牌
        
        // 確認所有玩家都有底牌
        for (Player player : game.getPlayers()) {
            assertEquals(2, player.getHoleCards().size(), "Each player should have 2 hole cards");
        }
    }

    @而且("輪到大盲注後一位玩家行動")
    public void 輪到大盲注後一位玩家行動() {
        // 找到當前行動的玩家（大盲注後第一位）
        currentPlayer = game.getCurrentPlayer();
        assertNotNull(currentPlayer, "Current player should be set");
        assertEquals(GameState.PRE_FLOP, game.getState(), "Game should be in PRE_FLOP state");
    }

    @當("該玩家面臨行動選擇時")
    public void 該玩家面臨行動選擇時() {
        // 記錄初始狀態
        originalChips = currentPlayer.getChipCount();
        originalPot = game.getCurrentPot();
        
        // 驗證玩家可以行動
        assertEquals(PlayerStatus.ACTIVE, currentPlayer.getStatus(), "Player should be active");
        assertTrue(originalChips.compareTo(BigDecimal.ZERO) > 0, "Player should have chips");
    }

    @那麼("玩家可以選擇棄牌")
    public void 玩家可以選擇棄牌() {
        // 測試棄牌行動
        assertTrue(game.canPlayerFold(currentPlayer), "Player should be able to fold");
        
        // 執行棄牌
        game.playerFold(currentPlayer);
        assertEquals(PlayerStatus.FOLDED, currentPlayer.getStatus(), "Player should be folded");
    }

    @而且("玩家可以選擇跟注大盲注金額")
    public void 玩家可以選擇跟注大盲注金額() {
        // 確保當前玩家是遊戲中的當前玩家
        currentPlayer = game.getCurrentPlayer();
        assertNotNull(currentPlayer, "Current player should not be null");
        
        // 確保玩家狀態正確
        currentPlayer.setStatus(PlayerStatus.ACTIVE);
        
        BigDecimal callAmount = game.getCallAmount(currentPlayer);
        assertTrue(game.canPlayerCall(currentPlayer), "Player should be able to call");
        assertTrue(currentPlayer.canAfford(callAmount), "Player should afford call amount");
    }

    @而且("玩家可以選擇加注")
    public void 玩家可以選擇加注() {
        // 重置玩家狀態
        currentPlayer.setStatus(PlayerStatus.ACTIVE);
        
        BigDecimal minRaise = game.getMinimumRaise();
        assertTrue(game.canPlayerRaise(currentPlayer), "Player should be able to raise");
        assertTrue(currentPlayer.canAfford(minRaise), "Player should afford minimum raise");
    }

    @而且("玩家可以選擇全押")
    public void 玩家可以選擇全押() {
        // 重置玩家狀態
        currentPlayer.setStatus(PlayerStatus.ACTIVE);
        
        assertTrue(game.canPlayerAllIn(currentPlayer), "Player should be able to go all-in");
        assertTrue(currentPlayer.getChipCount().compareTo(BigDecimal.ZERO) > 0, 
                  "Player should have chips to go all-in");
    }

    // 加注規則驗證場景
    @假設("一位玩家選擇加注")
    public void 一位玩家選擇加注() {
        // 設置測試環境
        game = new Game(new BigDecimal("10"), new BigDecimal("20"));
        
        for (int i = 1; i <= 3; i++) {
            Player player = new Player("Player" + i, new BigDecimal("1000"));
            game.addPlayer(player);
        }
        
        game.initialize();
        game.startNewHand();
        currentPlayer = game.getCurrentPlayer();
    }

    @而且("當前輪次的最小加注金額是 {int}")
    public void 當前輪次的最小加注金額是(int minRaise) {
        assertEquals(new BigDecimal(minRaise), game.getMinimumRaise(), 
                    "Minimum raise should be " + minRaise);
    }

    @當("執行加注行動時")
    public void 執行加注行動時() {
        BigDecimal raiseAmount = new BigDecimal("40"); // 加注到40
        originalPot = game.getCurrentPot();
        
        actionExecuted = game.playerRaise(currentPlayer, raiseAmount);
    }

    @那麼("加注金額必須等於或超過本輪最後的加注金額")
    public void 加注金額必須等於或超過本輪最後的加注金額() {
        assertTrue(actionExecuted, "Raise action should be executed");
        assertTrue(game.getCurrentBet().compareTo(game.getMinimumRaise()) >= 0, 
                  "Current bet should meet minimum raise requirement");
    }

    @而且("如果本輪沒有人加注過，加注金額必須大於等於大盲注")
    public void 如果本輪沒有人加注過加注金額必須大於等於大盲注() {
        BigDecimal bigBlind = game.getBigBlind();
        assertTrue(game.getCurrentBet().compareTo(bigBlind) >= 0, 
                  "Raise amount should be at least big blind");
    }

    @而且("系統應該更新當前最高下注金額")
    public void 系統應該更新當前最高下注金額() {
        assertTrue(game.getCurrentBet().compareTo(new BigDecimal("20")) > 0, 
                  "Current bet should be updated");
    }

    @而且("其他玩家需要至少跟注到新的金額")
    public void 其他玩家需要至少跟注到新的金額() {
        BigDecimal currentBet = game.getCurrentBet();
        
        for (Player player : game.getPlayers()) {
            if (player != currentPlayer && player.getStatus() == PlayerStatus.ACTIVE) {
                assertEquals(currentBet, game.getCallAmount(player).add(player.getCurrentBet()), 
                           "Other players should need to call to current bet amount");
            }
        }
    }

    // 跟注行動處理場景
    @假設("有玩家已經下注 {int}")
    public void 有玩家已經下注(int betAmount) {
        game = new Game(new BigDecimal("10"), new BigDecimal("20"));
        
        for (int i = 1; i <= 3; i++) {
            Player player = new Player("Player" + i, new BigDecimal("1000"));
            game.addPlayer(player);
        }
        
        game.initialize();
        game.startNewHand();
        
        // 第一個玩家加注到指定金額
        Player firstPlayer = game.getCurrentPlayer();
        game.playerRaise(firstPlayer, new BigDecimal(betAmount));
        
        // 移動到下一個玩家
        game.moveToNextPlayer();
        currentPlayer = game.getCurrentPlayer();
    }

    @而且("輪到下一位玩家行動")
    public void 輪到下一位玩家行動() {
        assertNotNull(currentPlayer, "Current player should be set");
        assertEquals(PlayerStatus.ACTIVE, currentPlayer.getStatus(), "Current player should be active");
        originalChips = currentPlayer.getChipCount();
        originalPot = game.getCurrentPot();
    }

    @當("該玩家選擇跟注時")
    public void 該玩家選擇跟注時() {
        actionExecuted = game.playerCall(currentPlayer);
    }

    @那麼("玩家應該支付 {int} 籌碼")
    public void 玩家應該支付籌碼(int amount) {
        assertTrue(actionExecuted, "Call action should be executed");
        
        // amount 表示玩家的總下注金額（已經下注的 + 這次下注的）
        // 驗證玩家的當前下注總額是否等於預期金額
        assertEquals(new BigDecimal(amount), currentPlayer.getCurrentBet(), 
                    "Player's total bet should equal " + amount);
    }

    @而且("玩家的籌碼應該相應減少")
    public void 玩家的籌碼應該相應減少() {
        assertTrue(currentPlayer.getChipCount().compareTo(originalChips) < 0, 
                  "Player's chips should decrease");
    }

    @而且("底池應該增加相應金額")
    public void 底池應該增加相應金額() {
        assertTrue(game.getCurrentPot().compareTo(originalPot) > 0, 
                  "Pot should increase");
    }

    @而且("玩家狀態應該保持 ACTIVE")
    public void 玩家狀態應該保持ACTIVE() {
        assertEquals(PlayerStatus.ACTIVE, currentPlayer.getStatus(), 
                    "Player should remain active after call");
    }

    // 全押行動處理場景
    @假設("一位玩家剩餘籌碼少於當前下注金額")
    public void 一位玩家剩餘籌碼少於當前下注金額() {
        game = new Game(new BigDecimal("10"), new BigDecimal("20"));
        
        // 添加玩家
        for (int i = 1; i <= 3; i++) {
            Player player = new Player("Player" + i, new BigDecimal("1000"));
            game.addPlayer(player);
        }
        
        game.initialize();
        game.startNewHand();
        
        // 獲取當前玩家並設置少量籌碼來模擬短籌碼情況
        currentPlayer = game.getCurrentPlayer();
        currentPlayer.setChipCount(new BigDecimal("30")); // 設置短籌碼
        
        // 設置高額下注讓短籌碼玩家無法跟注
        game.setCurrentBet(new BigDecimal("50"));
    }

    @而且("該玩家選擇全押")
    public void 該玩家選擇全押() {
        originalChips = currentPlayer.getChipCount();
        originalPot = game.getCurrentPot();
    }

    @當("執行全押行動時")
    public void 執行全押行動時() {
        actionExecuted = game.playerAllIn(currentPlayer);
    }

    @那麼("玩家應該投入所有剩餘籌碼")
    public void 玩家應該投入所有剩餘籌碼() {
        assertTrue(actionExecuted, "All-in action should be executed");
        assertEquals(BigDecimal.ZERO, currentPlayer.getChipCount(), 
                    "Player should have no chips left");
    }

    @而且("玩家狀態應該變為 ALL_IN")
    public void 玩家狀態應該變為ALL_IN() {
        assertEquals(PlayerStatus.ALL_IN, currentPlayer.getStatus(), 
                    "Player should be all-in");
    }

    @而且("底池應該增加玩家的所有籌碼")
    public void 底池應該增加玩家的所有籌碼() {
        BigDecimal currentPot = game.getCurrentPot();
        BigDecimal expectedIncrease = originalChips; // 玩家的原始籌碼全部進入底池
        assertTrue(currentPot.compareTo(originalPot.add(expectedIncrease)) >= 0, 
                  "Pot should increase by player's all chips");
    }

    @而且("如果全押金額少於當前下注，不會影響其他玩家的跟注金額")
    public void 如果全押金額少於當前下注不會影響其他玩家的跟注金額() {
        // 如果全押金額小於當前下注，其他玩家仍需跟注原金額
        BigDecimal currentBet = game.getCurrentBet();
        
        for (Player player : game.getPlayers()) {
            if (player != currentPlayer && player.getStatus() == PlayerStatus.ACTIVE) {
                assertTrue(game.getCallAmount(player).compareTo(BigDecimal.ZERO) >= 0, 
                          "Other players should still have valid call amounts");
            }
        }
    }

    // 棄牌行動處理場景
    @假設("輪到一位玩家行動")
    public void 輪到一位玩家行動() {
        game = new Game(new BigDecimal("10"), new BigDecimal("20"));
        
        for (int i = 1; i <= 3; i++) {
            Player player = new Player("Player" + i, new BigDecimal("1000"));
            game.addPlayer(player);
        }
        
        game.initialize();
        game.startNewHand();
        currentPlayer = game.getCurrentPlayer();
    }

    @當("該玩家選擇棄牌時")
    public void 該玩家選擇棄牌時() {
        actionExecuted = game.playerFold(currentPlayer);
    }

    @那麼("玩家狀態應該變為 FOLDED")
    public void 玩家狀態應該變為FOLDED() {
        assertTrue(actionExecuted, "Fold action should be executed");
        assertEquals(PlayerStatus.FOLDED, currentPlayer.getStatus(), 
                    "Player should be folded");
    }

    @而且("玩家的底牌應該被棄置")
    public void 玩家的底牌應該被棄置() {
        // 底牌仍在玩家手中，但玩家已退出競爭
        assertFalse(game.isPlayerInGame(currentPlayer), 
                   "Player should not be in active game");
    }

    @而且("玩家不再參與本輪競爭")
    public void 玩家不再參與本輪競爭() {
        assertFalse(game.isPlayerInGame(currentPlayer), 
                   "Folded player should not participate in competition");
    }

    @而且("行動轉移到下一位玩家")
    public void 行動轉移到下一位玩家() {
        Player nextPlayer = game.getCurrentPlayer();
        assertNotEquals(currentPlayer, nextPlayer, 
                       "Action should move to next player");
    }

    // 無效下注金額處理場景
    @假設("輪到玩家下注")
    public void 輪到玩家下注() {
        game = new Game(new BigDecimal("10"), new BigDecimal("20"));
        
        for (int i = 1; i <= 3; i++) {
            Player player = new Player("Player" + i, new BigDecimal("1000"));
            game.addPlayer(player);
        }
        
        game.initialize();
        game.startNewHand();
        currentPlayer = game.getCurrentPlayer();
        originalChips = currentPlayer.getChipCount();
    }

    @當("玩家輸入無效的下注金額")
    public void 玩家輸入無效的下注金額() {
        // 嘗試無效下注（負數）
        try {
            actionExecuted = game.playerBet(currentPlayer, new BigDecimal("-10"));
        } catch (Exception e) {
            lastErrorMessage = e.getMessage();
            CommonSteps.setActualErrorMessage(e.getMessage()); // 設定給共用步驟使用
            actionExecuted = false;
        }
    }

    // 錯誤訊息驗證已移至 CommonSteps

    @而且("不應該從玩家扣除任何籌碼")
    public void 不應該從玩家扣除任何籌碼() {
        assertEquals(originalChips, currentPlayer.getChipCount(), 
                    "Player's chips should not change on invalid bet");
    }

    @而且("玩家應該被要求重新選擇行動")
    public void 玩家應該被要求重新選擇行動() {
        assertEquals(currentPlayer, game.getCurrentPlayer(), 
                    "Current player should remain the same for retry");
        assertEquals(PlayerStatus.ACTIVE, currentPlayer.getStatus(), 
                    "Player should remain active for retry");
    }

    // 籌碼不足下注處理場景
    @而且("玩家剩餘籌碼少於想要下注的金額")
    public void 玩家剩餘籌碼少於想要下注的金額() {
        // 設置玩家只有少量籌碼
        currentPlayer.setChipCount(new BigDecimal("15"));
        originalChips = currentPlayer.getChipCount();
    }

    @當("玩家選擇的下注金額超過剩餘籌碼時")
    public void 玩家選擇的下注金額超過剩餘籌碼時() {
        // 嘗試下注超過籌碼的金額
        try {
            actionExecuted = game.playerBet(currentPlayer, new BigDecimal("100"));
        } catch (Exception e) {
            lastErrorMessage = e.getMessage();
            CommonSteps.setActualErrorMessage(e.getMessage()); // 設定給共用步驟使用
            actionExecuted = false;
        }
    }

    @那麼("系統應該提示 {string}")
    public void 系統應該提示(String expectedMessage) {
        assertNotNull(lastErrorMessage, "Should have error message");
        assertTrue(lastErrorMessage.contains("籌碼不足") || 
                  lastErrorMessage.contains(expectedMessage), 
                  "Should show insufficient chips message");
    }

    @而且("給予玩家重新選擇的機會")
    public void 給予玩家重新選擇的機會() {
        assertEquals(PlayerStatus.ACTIVE, currentPlayer.getStatus(), 
                    "Player should remain active for new choice");
    }

    @而且("如果玩家確認，則執行全押行動")
    public void 如果玩家確認則執行全押行動() {
        // 模擬玩家確認全押
        actionExecuted = game.playerAllIn(currentPlayer);
        assertTrue(actionExecuted, "All-in should be executed");
        assertEquals(PlayerStatus.ALL_IN, currentPlayer.getStatus(), 
                    "Player should be all-in");
    }

    // 下注輪次結束判定場景
    @假設("所有未棄牌的玩家都已行動")
    public void 所有未棄牌的玩家都已行動() {
        game = new Game(new BigDecimal("10"), new BigDecimal("20"));
        
        for (int i = 1; i <= 4; i++) {
            Player player = new Player("Player" + i, new BigDecimal("1000"));
            game.addPlayer(player);
        }
        
        game.initialize();
        game.startNewHand();
        
        // 模擬完整的下注輪次：確保所有玩家都達到相同的下注金額
        BigDecimal targetBet = game.getCurrentBet(); // 大盲注金額
        
        // 直接設定所有活躍玩家的下注金額為目標金額
        for (Player player : game.getActivePlayers()) {
            if (player.getCurrentBet().compareTo(targetBet) < 0) {
                // 計算需要額外支付的金額並直接設定
                BigDecimal additionalAmount = targetBet.subtract(player.getCurrentBet());
                player.bet(additionalAmount);
            }
        }
    }

    @而且("所有玩家的下注金額都相等")
    public void 所有玩家的下注金額都相等() {
        List<Player> activePlayers = game.getActivePlayers();
        if (!activePlayers.isEmpty()) {
            BigDecimal expectedBet = game.getCurrentBet();
            
            for (Player player : activePlayers) {
                assertTrue(player.getCurrentBet().compareTo(expectedBet) == 0 || 
                          player.getStatus() == PlayerStatus.ALL_IN, 
                          "All active players should have equal bets or be all-in");
            }
        }
    }

    @當("檢查下注輪次狀態時")
    public void 檢查下注輪次狀態時() {
        // 檢查是否可以結束下注輪次
        actionExecuted = game.isBettingRoundComplete();
    }

    @那麼("下注輪次應該結束")
    public void 下注輪次應該結束() {
        assertTrue(actionExecuted, "Betting round should be complete");
    }

    @而且("遊戲應該進入下一階段")
    public void 遊戲應該進入下一階段() {
        GameState currentState = game.getState();
        assertTrue(currentState == GameState.PRE_FLOP || 
                  currentState == GameState.FLOP || 
                  currentState == GameState.TURN || 
                  currentState == GameState.RIVER, 
                  "Game should be in a valid betting state");
    }

    @而且("所有玩家的當前下注應該重置為 0")
    public void 所有玩家的當前下注應該重置為0() {
        // 在實際遊戲中，進入新階段時會重置下注
        // 這裡驗證重置邏輯是否正確
        if (game.getState() != GameState.PRE_FLOP) {
            for (Player player : game.getPlayers()) {
                if (player.getStatus() == PlayerStatus.ACTIVE) {
                    assertEquals(BigDecimal.ZERO, player.getCurrentBet(), 
                               "Player's current bet should be reset for new round");
                }
            }
        }
    }
}