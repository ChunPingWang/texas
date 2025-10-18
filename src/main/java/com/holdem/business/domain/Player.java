package com.holdem.business.domain;

import com.holdem.shared.enums.PlayerAction;
import com.holdem.shared.enums.PlayerStatus;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 玩家領域實體
 * 代表 Texas Hold'em 遊戲中的一位玩家
 * 
 * @author Development Team
 * @version 1.0
 * @since 2025-10-18
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Player {
    
    @EqualsAndHashCode.Include
    private final String id;
    private final String name;
    private BigDecimal chipCount;
    private PlayerStatus status;
    private int seatNumber;
    private List<Card> holeCards;
    private BigDecimal currentBet;
    private PlayerAction lastAction;
    private Hand hand; // 玩家當前的最佳牌型
    
    /**
     * 建立玩家實例
     * 
     * @param name 玩家名稱
     * @param initialChips 初始籌碼
     * @throws IllegalArgumentException 當參數無效時拋出
     */
    public Player(String name, BigDecimal initialChips) {
        this(UUID.randomUUID().toString(), name, initialChips);
    }
    
    /**
     * 建立玩家實例（僅名稱，用於測試）
     * 
     * @param name 玩家名稱
     */
    public Player(String name) {
        this(name, BigDecimal.valueOf(1000)); // 預設籌碼
    }
    
    /**
     * 建立玩家實例（指定ID，主要用於測試）
     * 
     * @param id 玩家ID
     * @param name 玩家名稱
     * @param initialChips 初始籌碼
     * @throws IllegalArgumentException 當參數無效時拋出
     */
    public Player(String id, String name, BigDecimal initialChips) {
        this.id = Objects.requireNonNull(id, "Player ID cannot be null");
        this.name = Objects.requireNonNull(name, "Player name cannot be null");
        
        if (initialChips == null || initialChips.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Initial chips must be non-negative");
        }
        
        this.chipCount = initialChips;
        this.status = PlayerStatus.WAITING;
        this.seatNumber = -1; // 未分配座位
        this.holeCards = new ArrayList<>();
        this.currentBet = BigDecimal.ZERO;
        this.lastAction = null;
        this.hand = null;
    }
    
    /**
     * 分配座位
     * 
     * @param seatNumber 座位號碼
     * @throws IllegalArgumentException 當座位號碼無效時拋出
     */
    public void assignSeat(int seatNumber) {
        if (seatNumber < 1) {
            throw new IllegalArgumentException("Seat number must be positive");
        }
        this.seatNumber = seatNumber;
        this.status = PlayerStatus.ACTIVE;
    }
    
    /**
     * 發放底牌
     * 
     * @param cards 底牌列表
     * @throws IllegalArgumentException 當底牌數量不正確時拋出
     */
    public void dealHoleCards(List<Card> cards) {
        Objects.requireNonNull(cards, "Hole cards cannot be null");
        if (cards.size() != 2) {
            throw new IllegalArgumentException("Texas Hold'em requires exactly 2 hole cards");
        }
        
        this.holeCards = new ArrayList<>(cards);
    }
    
    /**
     * 執行下注行動
     * 
     * @param action 行動類型
     * @param amount 下注金額 (對於 FOLD, CHECK 可以為0)
     * @return 實際投入的金額
     * @throws IllegalArgumentException 當行動或金額無效時拋出
     * @throws IllegalStateException 當玩家無法執行行動時拋出
     */
    public BigDecimal performAction(PlayerAction action, BigDecimal amount) {
        Objects.requireNonNull(action, "Action cannot be null");
        
        if (!status.canAct()) {
            throw new IllegalStateException("Player cannot act in current status: " + status);
        }
        
        validateActionAmount(action, amount);
        
        BigDecimal actualAmount = switch (action) {
            case FOLD -> {
                status = PlayerStatus.FOLDED;
                yield BigDecimal.ZERO;
            }
            case CHECK -> {
                if (currentBet.compareTo(BigDecimal.ZERO) != 0) {
                    throw new IllegalStateException("Cannot check when there are outstanding bets");
                }
                yield BigDecimal.ZERO;
            }
            case CALL -> performCall();
            case BET, RAISE -> performBetOrRaise(amount);
            case ALL_IN -> performAllIn();
        };
        
        this.lastAction = action;
        return actualAmount;
    }
    
    private void validateActionAmount(PlayerAction action, BigDecimal amount) {
        if (action.requiresChips()) {
            if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Amount must be non-negative for action: " + action);
            }
        }
    }
    
    private BigDecimal performCall() {
        BigDecimal callAmount = currentBet;
        if (callAmount.compareTo(chipCount) >= 0) {
            // 玩家籌碼不足，自動全押
            return performAllIn();
        }
        
        chipCount = chipCount.subtract(callAmount);
        return callAmount;
    }
    
    private BigDecimal performBetOrRaise(BigDecimal amount) {
        if (amount.compareTo(chipCount) >= 0) {
            // 下注金額超過或等於籌碼，自動全押
            return performAllIn();
        }
        
        chipCount = chipCount.subtract(amount);
        currentBet = currentBet.add(amount);
        return amount;
    }
    
    private BigDecimal performAllIn() {
        BigDecimal allInAmount = chipCount;
        chipCount = BigDecimal.ZERO;
        status = PlayerStatus.ALL_IN;
        return allInAmount;
    }
    
    /**
     * 新手牌開始時重置玩家狀態
     */
    public void resetForNewHand() {
        if (chipCount.compareTo(BigDecimal.ZERO) == 0) {
            status = PlayerStatus.ELIMINATED;
        } else {
            status = PlayerStatus.ACTIVE;
        }
        
        holeCards.clear();
        currentBet = BigDecimal.ZERO;
        lastAction = null;
    }
    
    /**
     * 贏得籌碼
     * 
     * @param amount 獲得的籌碼數量
     * @throws IllegalArgumentException 當金額為負數時拋出
     */
    public void winChips(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Win amount must be non-negative");
        }
        
        chipCount = chipCount.add(amount);
        
        // 如果玩家之前全押且獲得籌碼，恢復為活躍狀態
        if (status == PlayerStatus.ALL_IN && chipCount.compareTo(BigDecimal.ZERO) > 0) {
            status = PlayerStatus.ACTIVE;
        }
    }
    
    /**
     * 支付盲注
     * 
     * @param blindAmount 盲注金額
     * @return 實際支付的金額
     * @throws IllegalArgumentException 當盲注金額無效時拋出
     */
    public BigDecimal payBlind(BigDecimal blindAmount) {
        Objects.requireNonNull(blindAmount, "Blind amount cannot be null");
        if (blindAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Blind amount must be positive");
        }
        
        if (blindAmount.compareTo(chipCount) >= 0) {
            // 籌碼不足支付完整盲注，全押
            BigDecimal paidAmount = chipCount;
            chipCount = BigDecimal.ZERO;
            status = PlayerStatus.ALL_IN;
            currentBet = paidAmount;
            return paidAmount;
        } else {
            chipCount = chipCount.subtract(blindAmount);
            currentBet = blindAmount;
            return blindAmount;
        }
    }
    
    /**
     * 檢查玩家是否仍在當前手牌中
     * 
     * @return 如果玩家仍在遊戲中返回 true
     */
    public boolean isInHand() {
        return status.isInGame();
    }
    
    /**
     * 檢查玩家是否有底牌
     * 
     * @return 如果有底牌返回 true
     */
    public boolean hasHoleCards() {
        return holeCards != null && holeCards.size() == 2;
    }
    
    /**
     * 獲取底牌副本（防止外部修改）
     * 
     * @return 底牌列表副本
     */
    public List<Card> getHoleCards() {
        return new ArrayList<>(holeCards);
    }
    
    /**
     * 檢查是否已被淘汰
     * 
     * @return 如果籌碼為0返回 true
     */
    public boolean isEliminated() {
        return chipCount.compareTo(BigDecimal.ZERO) == 0;
    }
    
    /**
     * 重置當前下注金額
     * 用於新的下注輪次開始時
     */
    public void resetCurrentBet() {
        this.currentBet = BigDecimal.ZERO;
    }
    
    /**
     * 檢查玩家是否有足夠籌碼支付指定金額
     * 
     * @param amount 需要支付的金額
     * @return 如果籌碼足夠返回 true
     * @throws IllegalArgumentException 當金額為 null 或負數時拋出
     */
    public boolean canAfford(BigDecimal amount) {
        Objects.requireNonNull(amount, "Amount cannot be null");
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        return chipCount.compareTo(amount) >= 0;
    }
    
    /**
     * 執行棄牌動作
     * 將玩家狀態設置為已棄牌
     */
    public void fold() {
        this.status = PlayerStatus.FOLDED;
        // 底牌保留在玩家手中，但不再參與競爭
    }
    
    /**
     * 執行跟注動作
     * 
     * @param callAmount 需要跟注的金額
     * @return 實際支付的金額
     * @throws IllegalArgumentException 當跟注金額無效時拋出
     * @throws IllegalStateException 當玩家狀態不允許跟注時拋出
     */
    public BigDecimal call(BigDecimal callAmount) {
        Objects.requireNonNull(callAmount, "Call amount cannot be null");
        if (callAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Call amount cannot be negative");
        }
        if (status != PlayerStatus.ACTIVE) {
            throw new IllegalStateException("Player must be active to call");
        }
        
        if (callAmount.compareTo(chipCount) >= 0) {
            // 籌碼不足，全押
            return allIn();
        } else {
            chipCount = chipCount.subtract(callAmount);
            currentBet = currentBet.add(callAmount);
            return callAmount;
        }
    }
    
    /**
     * 執行下注動作
     * 
     * @param betAmount 下注金額
     * @return 實際下注的金額
     * @throws IllegalArgumentException 當下注金額無效時拋出
     * @throws IllegalStateException 當玩家狀態不允許下注時拋出
     */
    public BigDecimal bet(BigDecimal betAmount) {
        Objects.requireNonNull(betAmount, "Bet amount cannot be null");
        if (betAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Bet amount must be positive");
        }
        if (status != PlayerStatus.ACTIVE) {
            throw new IllegalStateException("Player must be active to bet");
        }
        
        if (betAmount.compareTo(chipCount) >= 0) {
            // 籌碼不足，全押
            return allIn();
        } else {
            chipCount = chipCount.subtract(betAmount);
            currentBet = currentBet.add(betAmount);
            return betAmount;
        }
    }
    
    /**
     * 執行加注動作
     * 
     * @param raiseAmount 加注到的總金額
     * @return 實際加注的金額
     * @throws IllegalArgumentException 當加注金額無效時拋出
     * @throws IllegalStateException 當玩家狀態不允許加注時拋出
     */
    public BigDecimal raise(BigDecimal raiseAmount) {
        Objects.requireNonNull(raiseAmount, "Raise amount cannot be null");
        if (raiseAmount.compareTo(currentBet) <= 0) {
            throw new IllegalArgumentException("Raise amount must be greater than current bet");
        }
        if (status != PlayerStatus.ACTIVE) {
            throw new IllegalStateException("Player must be active to raise");
        }
        
        BigDecimal additionalAmount = raiseAmount.subtract(currentBet);
        
        if (additionalAmount.compareTo(chipCount) >= 0) {
            // 籌碼不足，全押
            return allIn();
        } else {
            chipCount = chipCount.subtract(additionalAmount);
            currentBet = raiseAmount;
            return additionalAmount;
        }
    }
    
    /**
     * 執行全押動作
     * 將所有剩餘籌碼投入
     * 
     * @return 全押的金額
     * @throws IllegalStateException 當玩家狀態不允許全押時拋出
     */
    public BigDecimal allIn() {
        if (status != PlayerStatus.ACTIVE) {
            throw new IllegalStateException("Player must be active to go all-in");
        }
        if (chipCount.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalStateException("Player has no chips to go all-in");
        }
        
        BigDecimal allInAmount = chipCount;
        chipCount = BigDecimal.ZERO;
        currentBet = currentBet.add(allInAmount);
        status = PlayerStatus.ALL_IN;
        
        return allInAmount;
    }
    
    /**
     * 檢查玩家是否可以執行指定動作
     * 
     * @param action 要檢查的動作
     * @return 如果可以執行返回 true
     */
    public boolean canPerformAction(String action) {
        if (status != PlayerStatus.ACTIVE) {
            return false;
        }
        
        switch (action.toLowerCase()) {
            case "fold":
                return true;
            case "call":
                return chipCount.compareTo(BigDecimal.ZERO) > 0;
            case "bet":
            case "raise":
                return chipCount.compareTo(BigDecimal.ZERO) > 0;
            case "allin":
                return chipCount.compareTo(BigDecimal.ZERO) > 0;
            default:
                return false;
        }
    }
    
    // ==================== 測試輔助方法 ====================
    
    /**
     * 設置籌碼數量（主要用於測試）
     * 
     * @param chipCount 新的籌碼數量
     * @throws IllegalArgumentException 當籌碼數量為負數時拋出
     */
    public void setChipCount(BigDecimal chipCount) {
        Objects.requireNonNull(chipCount, "Chip count cannot be null");
        if (chipCount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Chip count cannot be negative");
        }
        this.chipCount = chipCount;
    }
    
    /**
     * 設置玩家狀態（主要用於測試）
     * 
     * @param status 新的玩家狀態
     */
    public void setStatus(PlayerStatus status) {
        Objects.requireNonNull(status, "Status cannot be null");
        this.status = status;
    }
    
    /**
     * 設置當前下注金額（主要用於測試）
     * 
     * @param currentBet 當前下注金額
     * @throws IllegalArgumentException 當下注金額為負數時拋出
     */
    public void setCurrentBet(BigDecimal currentBet) {
        Objects.requireNonNull(currentBet, "Current bet cannot be null");
        if (currentBet.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Current bet cannot be negative");
        }
        this.currentBet = currentBet;
    }
    
    /**
     * 獲取玩家當前的最佳牌型
     * 
     * @return 最佳牌型，如果尚未評估則返回 null
     */
    public Hand getHand() {
        return hand;
    }
    
    /**
     * 設置玩家的最佳牌型（主要用於測試）
     * 
     * @param hand 牌型
     */
    public void setHand(Hand hand) {
        this.hand = hand;
    }
    
    @Override
    public String toString() {
        return String.format("Player{id='%s', name='%s', chips=%s, status=%s, seat=%d}", 
                           id, name, chipCount, status, seatNumber);
    }
}