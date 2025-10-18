package com.holdem.business.domain;

import com.holdem.shared.enums.Rank;
import com.holdem.shared.enums.Suit;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

/**
 * 撲克牌領域實體
 * 代表單張撲克牌，包含花色和點數
 * 
 * @author Development Team
 * @version 1.0
 * @since 2025-10-18
 */
@Getter
@ToString
@EqualsAndHashCode
public final class Card implements Comparable<Card> {
    
    private final Rank rank;
    private final Suit suit;
    
    /**
     * 建立撲克牌實例
     * 
     * @param rank 點數
     * @param suit 花色
     * @throws IllegalArgumentException 當參數為 null 時拋出
     */
    public Card(Rank rank, Suit suit) {
        this.rank = Objects.requireNonNull(rank, "Rank cannot be null");
        this.suit = Objects.requireNonNull(suit, "Suit cannot be null");
    }
    
    /**
     * 比較兩張牌的大小
     * 主要依據點數，相同點數時依據花色
     * 
     * @param other 另一張牌
     * @return 比較結果
     */
    @Override
    public int compareTo(Card other) {
        Objects.requireNonNull(other, "Cannot compare with null card");
        
        int rankComparison = Integer.compare(this.rank.getValue(), other.rank.getValue());
        if (rankComparison != 0) {
            return rankComparison;
        }
        
        // 點數相同時比較花色
        return Integer.compare(this.suit.getPriority(), other.suit.getPriority());
    }
    
    /**
     * 獲取牌的簡短表示 (例如: "AS", "KH")
     * 
     * @return 牌的字符串表示
     */
    public String getShortName() {
        return rank.getSymbol() + suit.getSymbol();
    }
    
    /**
     * 獲取牌的完整中文名稱
     * 
     * @return 中文名稱
     */
    public String getChineseName() {
        return suit.getChineseName() + rank.getSymbol();
    }
    
    /**
     * 檢查是否為紅色牌
     * 
     * @return 如果是紅心或方塊返回 true
     */
    public boolean isRed() {
        return suit == Suit.HEARTS || suit == Suit.DIAMONDS;
    }
    
    /**
     * 檢查是否為黑色牌
     * 
     * @return 如果是黑桃或梅花返回 true
     */
    public boolean isBlack() {
        return suit == Suit.SPADES || suit == Suit.CLUBS;
    }
    
    /**
     * 靜態工廠方法：根據字符串創建撲克牌
     * 
     * @param cardString 牌的字符串表示，如 "AS", "KH"
     * @return Card 實例
     * @throws IllegalArgumentException 當字符串格式無效時拋出
     */
    public static Card fromString(String cardString) {
        if (cardString == null || cardString.length() != 2) {
            throw new IllegalArgumentException("Card string must be exactly 2 characters");
        }
        
        char rankChar = cardString.charAt(0);
        char suitChar = cardString.charAt(1);
        
        Rank rank = parseRank(rankChar);
        Suit suit = parseSuit(suitChar);
        
        return new Card(rank, suit);
    }
    
    private static Rank parseRank(char rankChar) {
        return switch (rankChar) {
            case '2' -> Rank.TWO;
            case '3' -> Rank.THREE;
            case '4' -> Rank.FOUR;
            case '5' -> Rank.FIVE;
            case '6' -> Rank.SIX;
            case '7' -> Rank.SEVEN;
            case '8' -> Rank.EIGHT;
            case '9' -> Rank.NINE;
            case 'T' -> Rank.TEN;
            case 'J' -> Rank.JACK;
            case 'Q' -> Rank.QUEEN;
            case 'K' -> Rank.KING;
            case 'A' -> Rank.ACE;
            default -> throw new IllegalArgumentException("Invalid rank character: " + rankChar);
        };
    }
    
    private static Suit parseSuit(char suitChar) {
        return switch (suitChar) {
            case '♠', 'S' -> Suit.SPADES;
            case '♥', 'H' -> Suit.HEARTS;
            case '♦', 'D' -> Suit.DIAMONDS;
            case '♣', 'C' -> Suit.CLUBS;
            default -> throw new IllegalArgumentException("Invalid suit character: " + suitChar);
        };
    }
}