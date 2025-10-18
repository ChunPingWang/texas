package com.holdem.business.domain;

import com.holdem.shared.constants.GameConstants;
import com.holdem.shared.enums.Rank;
import com.holdem.shared.enums.Suit;
import lombok.Getter;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 牌組領域實體
 * 管理52張標準撲克牌的集合，提供洗牌和發牌功能
 * 
 * @author Development Team
 * @version 1.0
 * @since 2025-10-18
 */
@Getter
public class Deck {
    
    private final List<Card> cards;
    private final SecureRandom random;
    private int nextCardIndex;
    
    /**
     * 建立標準52張牌組
     */
    public Deck() {
        this(new SecureRandom());
    }
    
    /**
     * 建立牌組並指定隨機數生成器 (用於測試)
     * 
     * @param random 隨機數生成器
     */
    public Deck(SecureRandom random) {
        this.random = Objects.requireNonNull(random, "Random generator cannot be null");
        this.cards = createStandardDeck();
        this.nextCardIndex = 0;
    }
    
    /**
     * 建立標準52張牌
     * 
     * @return 包含所有52張牌的列表
     */
    private List<Card> createStandardDeck() {
        List<Card> standardCards = new ArrayList<>(GameConstants.DECK_SIZE);
        
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                standardCards.add(new Card(rank, suit));
            }
        }
        
        return standardCards;
    }
    
    /**
     * 洗牌 - 使用 Fisher-Yates 洗牌算法
     */
    public void shuffle() {
        Collections.shuffle(cards, random);
        nextCardIndex = 0;
    }
    
    /**
     * 發一張牌
     * 
     * @return 牌組頂部的牌
     * @throws IllegalStateException 當牌組沒有剩餘牌時拋出
     */
    public Card dealCard() {
        if (isEmpty()) {
            throw new IllegalStateException("No more cards in deck");
        }
        
        return cards.get(nextCardIndex++);
    }
    
    /**
     * 發多張牌
     * 
     * @param count 需要發牌的數量
     * @return 發出的牌列表
     * @throws IllegalArgumentException 當數量無效時拋出
     * @throws IllegalStateException 當牌不夠時拋出
     */
    public List<Card> dealCards(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("Card count cannot be negative");
        }
        if (count > getRemainingCards()) {
            throw new IllegalStateException("Not enough cards in deck: requested " + count + ", available " + getRemainingCards());
        }
        
        List<Card> dealtCards = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            dealtCards.add(dealCard());
        }
        
        return dealtCards;
    }
    
    /**
     * 銷牌 - 丟棄一張牌但不返回
     */
    public void burnCard() {
        if (isEmpty()) {
            throw new IllegalStateException("No cards to burn");
        }
        nextCardIndex++;
    }
    
    /**
     * 檢查牌組是否為空
     * 
     * @return 如果沒有剩餘牌返回 true
     */
    public boolean isEmpty() {
        return nextCardIndex >= cards.size();
    }
    
    /**
     * 獲取剩餘牌數
     * 
     * @return 剩餘牌數
     */
    public int getRemainingCards() {
        return cards.size() - nextCardIndex;
    }
    
    /**
     * 重置牌組 - 將所有牌收回
     */
    public void reset() {
        nextCardIndex = 0;
    }
    
    /**
     * 獲取所有牌的副本 (防止外部修改)
     * 
     * @return 牌組副本
     */
    public List<Card> getAllCards() {
        return new ArrayList<>(cards);
    }
    
    /**
     * 驗證牌組是否為標準52張牌
     * 
     * @return 如果是標準牌組返回 true
     */
    public boolean isStandardDeck() {
        if (cards.size() != GameConstants.DECK_SIZE) {
            return false;
        }
        
        // 檢查是否有重複牌
        Set<Card> uniqueCards = new HashSet<>(cards);
        if (uniqueCards.size() != GameConstants.DECK_SIZE) {
            return false;
        }
        
        // 檢查是否包含所有標準牌
        Set<Card> standardCards = createStandardDeck().stream().collect(Collectors.toSet());
        return uniqueCards.equals(standardCards);
    }
    
    /**
     * 獲取下一張牌（不發牌，僅查看）
     * 
     * @return 下一張要發的牌
     * @throws IllegalStateException 當沒有剩餘牌時拋出
     */
    public Card peekNextCard() {
        if (isEmpty()) {
            throw new IllegalStateException("No more cards in deck");
        }
        return cards.get(nextCardIndex);
    }
    
    @Override
    public String toString() {
        return String.format("Deck{totalCards=%d, remainingCards=%d, nextIndex=%d}", 
                           cards.size(), getRemainingCards(), nextCardIndex);
    }
}