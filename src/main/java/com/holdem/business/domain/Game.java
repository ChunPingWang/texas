package com.holdem.business.domain;

import com.holdem.shared.constants.GameConstants;
import com.holdem.shared.enums.GameState;
import com.holdem.shared.enums.PlayerStatus;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 遊戲領域實體
 * 代表 Texas Hold'em 遊戲的完整狀態和行為
 * 
 * @author Development Team
 * @version 1.0
 * @since 2025-10-18
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Game {
    
    @EqualsAndHashCode.Include
    private final String id;
    private final List<Player> players;
    private final Deck deck;
    private final BigDecimal smallBlind;
    private final BigDecimal bigBlind;
    private final Instant createdAt;
    
    private GameState state;
    private List<Card> communityCards;
    private int dealerButtonPosition;
    private int currentPlayerIndex;
    private BigDecimal currentPot;
    private BigDecimal currentBet;
    private int handNumber;
    
    /**
     * 建立新的遊戲實例
     * 
     * @param smallBlind 小盲注金額
     * @param bigBlind 大盲注金額
     * @throws IllegalArgumentException 當盲注設定無效時拋出
     */
    public Game(BigDecimal smallBlind, BigDecimal bigBlind) {
        this(UUID.randomUUID().toString(), smallBlind, bigBlind);
    }
    
    /**
     * 建立新的遊戲實例（指定ID，主要用於測試）
     * 
     * @param id 遊戲ID
     * @param smallBlind 小盲注金額
     * @param bigBlind 大盲注金額
     * @throws IllegalArgumentException 當參數無效時拋出
     */
    public Game(String id, BigDecimal smallBlind, BigDecimal bigBlind) {
        this.id = Objects.requireNonNull(id, "Game ID cannot be null");
        
        validateBlinds(smallBlind, bigBlind);
        this.smallBlind = smallBlind;
        this.bigBlind = bigBlind;
        
        this.players = new ArrayList<>();
        this.deck = new Deck();
        this.createdAt = Instant.now();
        
        // 初始化遊戲狀態
        this.state = GameState.WAITING_FOR_PLAYERS;
        this.communityCards = new ArrayList<>();
        this.dealerButtonPosition = -1;
        this.currentPlayerIndex = -1;
        this.currentPot = BigDecimal.ZERO;
        this.currentBet = BigDecimal.ZERO;
        this.handNumber = 0;
    }
    
    private void validateBlinds(BigDecimal smallBlind, BigDecimal bigBlind) {
        Objects.requireNonNull(smallBlind, "Small blind cannot be null");
        Objects.requireNonNull(bigBlind, "Big blind cannot be null");
        
        if (smallBlind.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Small blind must be positive");
        }
        if (bigBlind.compareTo(smallBlind.multiply(BigDecimal.valueOf(2))) != 0) {
            throw new IllegalArgumentException("Big blind must be exactly twice the small blind");
        }
    }
    
    /**
     * 加入玩家到遊戲
     * 
     * @param player 要加入的玩家
     * @throws IllegalArgumentException 當玩家無效時拋出
     * @throws IllegalStateException 當遊戲狀態不允許加入玩家時拋出
     */
    public void addPlayer(Player player) {
        Objects.requireNonNull(player, "Player cannot be null");
        
        if (state != GameState.WAITING_FOR_PLAYERS) {
            throw new IllegalStateException("Cannot add players when game is in state: " + state);
        }
        
        if (players.size() >= GameConstants.MAX_PLAYERS) {
            throw new IllegalStateException("Game is full (max " + GameConstants.MAX_PLAYERS + " players)");
        }
        
        if (players.stream().anyMatch(p -> p.getId().equals(player.getId()))) {
            throw new IllegalArgumentException("Player already in game: " + player.getId());
        }
        
        // 分配座位
        int seatNumber = players.size() + 1;
        player.assignSeat(seatNumber);
        players.add(player);
    }
    
    /**
     * 初始化遊戲 - 確定莊家位置並準備開始
     * 
     * @throws IllegalStateException 當遊戲狀態或玩家數量不正確時拋出
     */
    public void initialize() {
        if (state != GameState.WAITING_FOR_PLAYERS) {
            throw new IllegalStateException("Game can only be initialized from WAITING_FOR_PLAYERS state");
        }
        
        if (players.size() < GameConstants.MIN_PLAYERS) {
            throw new IllegalStateException("Need at least " + GameConstants.MIN_PLAYERS + " players to start");
        }
        
        // 隨機確定第一個莊家位置
        dealerButtonPosition = new Random().nextInt(players.size());
        state = GameState.INITIALIZED;
    }
    
    /**
     * 設定莊家按鈕位置（主要用於測試）
     * 
     * @param position 莊家位置（0-based index）
     * @throws IllegalArgumentException 當位置無效時拋出
     */
    public void setDealerButtonPosition(int position) {
        if (position < 0 || position >= players.size()) {
            throw new IllegalArgumentException("Invalid dealer position: " + position);
        }
        this.dealerButtonPosition = position;
    }
    
    /**
     * 開始新的一手牌
     * 
     * @throws IllegalStateException 當遊戲狀態不正確時拋出
     */
    public void startNewHand() {
        if (state != GameState.INITIALIZED && state != GameState.FINISHED) {
            throw new IllegalStateException("Cannot start new hand from state: " + state);
        }
        
        // 檢查是否有足夠的活躍玩家
        List<Player> activePlayers = getActivePlayers();
        if (activePlayers.size() < GameConstants.MIN_PLAYERS) {
            throw new IllegalStateException("Not enough active players to start hand");
        }
        
        // 重置手牌狀態
        resetHandState();
        
        // 移動莊家按鈕（除了第一手牌）
        if (handNumber > 0) {
            moveDealerButton();
        }
        
        handNumber++;
        
        // 洗牌
        deck.shuffle();
        
        // 收取盲注
        collectBlinds();
        
        // 發底牌
        dealHoleCards();
        
        // 開始翻牌前下注
        state = GameState.PRE_FLOP;
        setFirstPlayerToAct();
    }
    
    private void resetHandState() {
        communityCards.clear();
        currentPot = BigDecimal.ZERO;
        currentBet = BigDecimal.ZERO;
        currentPlayerIndex = -1;
        
        // 重置所有玩家的手牌狀態
        players.forEach(Player::resetForNewHand);
    }
    
    private void moveDealerButton() {
        List<Player> activePlayers = getActivePlayers();
        if (activePlayers.isEmpty()) {
            return;
        }
        
        // 找到下一個活躍玩家作為莊家
        int originalPosition = dealerButtonPosition;
        do {
            dealerButtonPosition = (dealerButtonPosition + 1) % players.size();
        } while (!players.get(dealerButtonPosition).getStatus().isInGame() && 
                 dealerButtonPosition != originalPosition);
    }
    
    private void collectBlinds() {
        List<Player> activePlayers = getActivePlayers();
        if (activePlayers.size() < 2) {
            return;
        }
        
        if (activePlayers.size() == 2) {
            // Heads-up 規則：莊家支付小盲注
            Player dealer = players.get(dealerButtonPosition);
            Player bigBlindPlayer = getNextActivePlayer(dealerButtonPosition);
            
            BigDecimal smallBlindPaid = dealer.payBlind(smallBlind);
            BigDecimal bigBlindPaid = bigBlindPlayer.payBlind(bigBlind);
            
            currentPot = currentPot.add(smallBlindPaid).add(bigBlindPaid);
            currentBet = bigBlind;
        } else {
            // 正常遊戲：莊家後第一個玩家支付小盲注，第二個玩家支付大盲注
            int smallBlindPosition = getNextActivePlayerIndex(dealerButtonPosition);
            int bigBlindPosition = getNextActivePlayerIndex(smallBlindPosition);
            
            Player smallBlindPlayer = players.get(smallBlindPosition);
            Player bigBlindPlayer = players.get(bigBlindPosition);
            
            BigDecimal smallBlindPaid = smallBlindPlayer.payBlind(smallBlind);
            BigDecimal bigBlindPaid = bigBlindPlayer.payBlind(bigBlind);
            
            currentPot = currentPot.add(smallBlindPaid).add(bigBlindPaid);
            currentBet = bigBlind;
        }
    }
    
    private void dealHoleCards() {
        List<Player> activePlayers = getActivePlayers();
        
        // 從小盲注位置開始發牌
        int startPosition = getSmallBlindPosition();
        
        // 每位玩家收集兩張牌
        Map<Player, List<Card>> playerCards = new HashMap<>();
        
        // 發兩輪牌
        for (int round = 0; round < 2; round++) {
            int currentPosition = startPosition;
            for (int i = 0; i < activePlayers.size(); i++) {
                Player player = players.get(currentPosition);
                if (player.getStatus().isInGame()) {
                    playerCards.computeIfAbsent(player, k -> new ArrayList<>()).add(deck.dealCard());
                }
                currentPosition = getNextActivePlayerIndex(currentPosition);
            }
        }
        
        // 給每位玩家發放收集到的兩張底牌
        for (Map.Entry<Player, List<Card>> entry : playerCards.entrySet()) {
            entry.getKey().dealHoleCards(entry.getValue());
        }
    }
    
    private void setFirstPlayerToAct() {
        if (getActivePlayers().size() == 2) {
            // Heads-up: 莊家先行動
            currentPlayerIndex = dealerButtonPosition;
        } else {
            // 正常遊戲: 大盲注後的第一個玩家先行動
            int bigBlindPosition = getBigBlindPosition();
            currentPlayerIndex = getNextActivePlayerIndex(bigBlindPosition);
        }
    }
    
    private int getSmallBlindPosition() {
        if (getActivePlayers().size() == 2) {
            return dealerButtonPosition; // Heads-up: 莊家是小盲注
        } else {
            return getNextActivePlayerIndex(dealerButtonPosition);
        }
    }
    
    private int getBigBlindPosition() {
        if (getActivePlayers().size() == 2) {
            return getNextActivePlayerIndex(dealerButtonPosition);
        } else {
            int smallBlindPos = getNextActivePlayerIndex(dealerButtonPosition);
            return getNextActivePlayerIndex(smallBlindPos);
        }
    }
    
    private int getNextActivePlayerIndex(int fromIndex) {
        int nextIndex = (fromIndex + 1) % players.size();
        while (nextIndex != fromIndex && !players.get(nextIndex).getStatus().isInGame()) {
            nextIndex = (nextIndex + 1) % players.size();
        }
        return nextIndex;
    }
    
    private Player getNextActivePlayer(int fromIndex) {
        return players.get(getNextActivePlayerIndex(fromIndex));
    }
    
    /**
     * 獲取當前活躍玩家列表
     * 
     * @return 活躍玩家列表
     */
    public List<Player> getActivePlayers() {
        return players.stream()
                     .filter(player -> player.getStatus().isInGame())
                     .collect(Collectors.toList());
    }
    
    /**
     * 獲取當前行動的玩家
     * 
     * @return 當前行動的玩家，如果沒有則返回 null
     */
    public Player getCurrentPlayer() {
        if (currentPlayerIndex < 0 || currentPlayerIndex >= players.size()) {
            return null;
        }
        return players.get(currentPlayerIndex);
    }
    
    /**
     * 檢查遊戲是否可以開始
     * 
     * @return 如果有足夠玩家可以開始返回 true
     */
    public boolean canStart() {
        return players.size() >= GameConstants.MIN_PLAYERS && 
               players.size() <= GameConstants.MAX_PLAYERS;
    }
    
    /**
     * 檢查遊戲是否已結束
     * 
     * @return 如果只剩一個或沒有活躍玩家返回 true
     */
    public boolean isFinished() {
        return getActivePlayers().size() <= 1;
    }
    
    /**
     * 獲取玩家副本列表（防止外部修改）
     * 
     * @return 玩家列表副本
     */
    public List<Player> getPlayers() {
        return new ArrayList<>(players);
    }
    
    /**
     * 獲取社區牌副本列表（防止外部修改）
     * 
     * @return 社區牌列表副本
     */
    public List<Card> getCommunityCards() {
        return new ArrayList<>(communityCards);
    }
    
    /**
     * 發放翻牌（3張公共牌）
     * 
     * @throws IllegalStateException 當遊戲狀態不正確時拋出
     */
    public void dealFlop() {
        if (state != GameState.PRE_FLOP) {
            throw new IllegalStateException("Cannot deal flop from state: " + state);
        }
        
        validateSufficientPlayers();
        
        // 銷毀一張牌
        if (deck.getRemainingCards() < 4) { // 需要1張銷牌 + 3張翻牌
            throw new RuntimeException("牌組不足，無法完成該手牌");
        }
        
        deck.dealCard(); // 銷牌
        
        // 發放3張翻牌
        for (int i = 0; i < 3; i++) {
            communityCards.add(deck.dealCard());
        }
        
        state = GameState.FLOP;
        resetBettingRound();
    }
    
    /**
     * 發放轉牌（第4張公共牌）
     * 
     * @throws IllegalStateException 當遊戲狀態不正確時拋出
     */
    public void dealTurn() {
        if (state != GameState.FLOP) {
            throw new IllegalStateException("Cannot deal turn from state: " + state);
        }
        
        validateSufficientPlayers();
        
        // 銷毀一張牌
        if (deck.getRemainingCards() < 2) { // 需要1張銷牌 + 1張轉牌
            throw new RuntimeException("牌組不足，無法完成該手牌");
        }
        
        deck.dealCard(); // 銷牌
        communityCards.add(deck.dealCard()); // 轉牌
        
        state = GameState.TURN;
        resetBettingRound();
    }
    
    /**
     * 發放河牌（第5張公共牌）
     * 
     * @throws IllegalStateException 當遊戲狀態不正確時拋出
     */
    public void dealRiver() {
        if (state != GameState.TURN) {
            throw new IllegalStateException("Cannot deal river from state: " + state);
        }
        
        validateSufficientPlayers();
        
        // 銷毀一張牌
        if (deck.getRemainingCards() < 2) { // 需要1張銷牌 + 1張河牌
            throw new RuntimeException("牌組不足，無法完成該手牌");
        }
        
        deck.dealCard(); // 銷牌
        communityCards.add(deck.dealCard()); // 河牌
        
        state = GameState.RIVER;
        resetBettingRound();
    }
    
    /**
     * 處理發牌錯誤
     * 
     * @param errorMessage 錯誤訊息
     * @throws IllegalStateException 拋出發牌錯誤異常
     */
    public void handleDealingError(String errorMessage) {
        throw new IllegalStateException("發牌錯誤: " + errorMessage);
    }
    
    /**
     * 重置下注輪次
     * 清除所有玩家的當前下注並重設遊戲的當前下注
     */
    private void resetBettingRound() {
        currentBet = BigDecimal.ZERO;
        players.forEach(player -> {
            if (player.getStatus().isInGame()) {
                player.resetCurrentBet();
            }
        });
        setFirstPlayerToAct();
    }
    
    // ==================== 下注系統相關方法 ====================
    
    /**
     * 移動到下一位玩家
     */
    public void moveToNextPlayer() {
        List<Player> activePlayers = getActivePlayers();
        if (!activePlayers.isEmpty()) {
            currentPlayerIndex = (currentPlayerIndex + 1) % activePlayers.size();
        }
    }
    
    /**
     * 檢查玩家是否可以棄牌
     * 
     * @param player 要檢查的玩家
     * @return 如果可以棄牌返回 true
     */
    public boolean canPlayerFold(Player player) {
        return player != null && 
               player.getStatus() == PlayerStatus.ACTIVE && 
               player == getCurrentPlayer();
    }
    
    /**
     * 執行玩家棄牌動作
     * 
     * @param player 要棄牌的玩家
     * @return 如果棄牌成功返回 true
     */
    public boolean playerFold(Player player) {
        if (!canPlayerFold(player)) {
            return false;
        }
        
        player.fold();
        moveToNextPlayer();
        return true;
    }
    
    /**
     * 檢查玩家是否可以跟注
     * 
     * @param player 要檢查的玩家
     * @return 如果可以跟注返回 true
     */
    public boolean canPlayerCall(Player player) {
        return player != null && 
               player.getStatus() == PlayerStatus.ACTIVE && 
               player == getCurrentPlayer() &&
               player.getChipCount().compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * 獲取玩家需要跟注的金額
     * 
     * @param player 要檢查的玩家
     * @return 需要跟注的金額
     */
    public BigDecimal getCallAmount(Player player) {
        if (player == null) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal needed = currentBet.subtract(player.getCurrentBet());
        return needed.compareTo(BigDecimal.ZERO) > 0 ? needed : BigDecimal.ZERO;
    }
    
    /**
     * 執行玩家跟注動作
     * 
     * @param player 要跟注的玩家
     * @return 如果跟注成功返回 true
     */
    public boolean playerCall(Player player) {
        if (!canPlayerCall(player)) {
            return false;
        }
        
        BigDecimal callAmount = getCallAmount(player);
        if (callAmount.compareTo(BigDecimal.ZERO) == 0) {
            // 不需要額外支付，相當於過牌
            moveToNextPlayer();
            return true;
        }
        
        BigDecimal paidAmount = player.call(callAmount);
        currentPot = currentPot.add(paidAmount);
        moveToNextPlayer();
        return true;
    }
    
    /**
     * 檢查玩家是否可以加注
     * 
     * @param player 要檢查的玩家
     * @return 如果可以加注返回 true
     */
    public boolean canPlayerRaise(Player player) {
        return player != null && 
               player.getStatus() == PlayerStatus.ACTIVE && 
               player == getCurrentPlayer() &&
               player.getChipCount().compareTo(getMinimumRaise()) >= 0;
    }
    
    /**
     * 獲取最小加注金額
     * 
     * @return 最小加注金額
     */
    public BigDecimal getMinimumRaise() {
        // 返回最小加注增量（而不是加注後的總金額）
        return bigBlind;
    }
    
    /**
     * 執行玩家加注動作
     * 
     * @param player 要加注的玩家
     * @param raiseToAmount 加注到的總金額
     * @return 如果加注成功返回 true
     */
    public boolean playerRaise(Player player, BigDecimal raiseToAmount) {
        if (!canPlayerRaise(player)) {
            return false;
        }
        
        // 最小加注到的金額 = 當前下注 + 最小加注增量
        BigDecimal minimumRaiseToAmount = currentBet.add(getMinimumRaise());
        if (raiseToAmount.compareTo(minimumRaiseToAmount) < 0) {
            throw new IllegalArgumentException("Raise amount too small");
        }
        
        BigDecimal additionalAmount = player.raise(raiseToAmount);
        currentPot = currentPot.add(additionalAmount);
        currentBet = raiseToAmount;
        moveToNextPlayer();
        return true;
    }
    
    /**
     * 檢查玩家是否可以全押
     * 
     * @param player 要檢查的玩家
     * @return 如果可以全押返回 true
     */
    public boolean canPlayerAllIn(Player player) {
        return player != null && 
               player.getStatus() == PlayerStatus.ACTIVE && 
               player == getCurrentPlayer() &&
               player.getChipCount().compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * 執行玩家全押動作
     * 
     * @param player 要全押的玩家
     * @return 如果全押成功返回 true
     */
    public boolean playerAllIn(Player player) {
        if (!canPlayerAllIn(player)) {
            return false;
        }
        
        BigDecimal allInAmount = player.allIn();
        currentPot = currentPot.add(allInAmount);
        
        // 如果全押金額超過當前下注，更新當前下注
        if (player.getCurrentBet().compareTo(currentBet) > 0) {
            currentBet = player.getCurrentBet();
        }
        
        moveToNextPlayer();
        return true;
    }
    
    /**
     * 執行玩家下注動作
     * 
     * @param player 要下注的玩家
     * @param betAmount 下注金額
     * @return 如果下注成功返回 true
     */
    public boolean playerBet(Player player, BigDecimal betAmount) {
        if (player == null || player != getCurrentPlayer()) {
            return false;
        }
        
        if (betAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("請輸入有效的下注金額");
        }
        
        if (!player.canAfford(betAmount)) {
            throw new IllegalArgumentException("籌碼不足，是否全押？");
        }
        
        BigDecimal paidAmount = player.bet(betAmount);
        currentPot = currentPot.add(paidAmount);
        
        if (player.getCurrentBet().compareTo(currentBet) > 0) {
            currentBet = player.getCurrentBet();
        }
        
        moveToNextPlayer();
        return true;
    }
    
    /**
     * 檢查玩家是否在遊戲中（未棄牌且有參與資格）
     * 
     * @param player 要檢查的玩家
     * @return 如果玩家在遊戲中返回 true
     */
    public boolean isPlayerInGame(Player player) {
        return player != null && 
               (player.getStatus() == PlayerStatus.ACTIVE || 
                player.getStatus() == PlayerStatus.ALL_IN);
    }
    
    /**
     * 檢查下注輪次是否完成
     * 
     * @return 如果下注輪次完成返回 true
     */
    public boolean isBettingRoundComplete() {
        List<Player> activePlayers = getActivePlayers();
        if (activePlayers.size() <= 1) {
            return true;
        }
        
        // 檢查所有在遊戲中的玩家是否都已下注相同金額
        BigDecimal expectedBet = currentBet;
        for (Player player : activePlayers) {
            if (isPlayerInGame(player)) {
                if (player.getStatus() == PlayerStatus.ACTIVE && 
                    player.getCurrentBet().compareTo(expectedBet) < 0) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * 設置當前下注金額（主要用於測試）
     * 
     * @param amount 下注金額
     */
    public void setCurrentBet(BigDecimal amount) {
        this.currentBet = amount;
    }
    
    /**
     * 獲取小盲注玩家索引
     * 
     * @return 小盲注玩家索引
     */
    private int getSmallBlindPlayerIndex() {
        List<Player> activePlayers = getActivePlayers();
        if (activePlayers.size() == 2) {
            return dealerButtonPosition; // heads-up 時莊家是小盲注
        } else {
            return (dealerButtonPosition + 1) % activePlayers.size();
        }
    }
    
    /**
     * 獲取大盲注玩家索引
     * 
     * @return 大盲注玩家索引
     */
    private int getBigBlindPlayerIndex() {
        List<Player> activePlayers = getActivePlayers();
        if (activePlayers.size() == 2) {
            return (dealerButtonPosition + 1) % activePlayers.size();
        } else {
            return (dealerButtonPosition + 2) % activePlayers.size();
        }
    }
    
    /**
     * 驗證是否有足夠的玩家繼續遊戲
     * 
     * @throws IllegalStateException 當活躍玩家不足時拋出
     */
    private void validateSufficientPlayers() {
        long activePlayerCount = getActivePlayers().stream()
            .filter(player -> player.getStatus().isInGame())
            .count();
        
        if (activePlayerCount < 2) {
            throw new IllegalStateException("Not enough active players to continue");
        }
    }
    
    @Override
    public String toString() {
        return String.format("Game{id='%s', state=%s, players=%d, pot=%s, hand=%d}", 
                           id, state, players.size(), currentPot, handNumber);
    }
}