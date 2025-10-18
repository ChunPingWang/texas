package com.holdem.business.domain;

import com.holdem.shared.enums.HandRank;
import com.holdem.shared.enums.Rank;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 德州撲克牌型領域模型
 * 表示玩家的最佳五張牌組合
 * 
 * @author Development Team
 * @version 1.0
 * @since 2025-10-18
 */
public class Hand {
    
    private final HandRank rank;
    private final List<Card> cards;
    private Rank keyCard;
    private List<Rank> pairs;
    private List<Rank> kickers;
    private List<Rank> keyCards;
    private boolean isLowAceStraight;
    
    /**
     * 建構子
     * @param rank 牌型等級
     */
    public Hand(HandRank rank) {
        this.rank = rank;
        this.cards = new ArrayList<>();
        this.pairs = new ArrayList<>();
        this.kickers = new ArrayList<>();
        this.keyCards = new ArrayList<>();
        this.isLowAceStraight = false;
    }
    
    /**
     * 建構子 - 包含牌組
     * @param rank 牌型等級
     * @param cards 組成牌型的五張牌
     */
    public Hand(HandRank rank, List<Card> cards) {
        this.rank = rank;
        this.cards = new ArrayList<>(cards);
        this.pairs = new ArrayList<>();
        this.kickers = new ArrayList<>();
        this.keyCards = new ArrayList<>();
        this.isLowAceStraight = false;
    }
    
    /**
     * 獲取牌型等級
     * @return 牌型等級
     */
    public HandRank getRank() {
        return rank;
    }
    
    /**
     * 獲取組成牌型的牌
     * @return 牌組
     */
    public List<Card> getCards() {
        return new ArrayList<>(cards);
    }
    
    /**
     * 添加牌到牌型中
     * @param card 要添加的牌
     */
    public void addCard(Card card) {
        if (cards.size() < 5) {
            cards.add(card);
        }
    }
    
    /**
     * 獲取關鍵牌（如四條的牌值、三條的牌值等）
     * @return 關鍵牌
     */
    public Rank getKeyCard() {
        return keyCard;
    }
    
    /**
     * 設定關鍵牌
     * @param keyCard 關鍵牌
     */
    public void setKeyCard(Rank keyCard) {
        this.keyCard = keyCard;
    }
    
    /**
     * 獲取對子牌值列表（用於兩對、葫蘆等）
     * @return 對子牌值列表
     */
    public List<Rank> getPairs() {
        return new ArrayList<>(pairs);
    }
    
    /**
     * 設定對子牌值列表
     * @param pairs 對子牌值列表
     */
    public void setPairs(List<Rank> pairs) {
        this.pairs = new ArrayList<>(pairs);
    }
    
    /**
     * 添加對子牌值
     * @param pairRank 對子牌值
     */
    public void addPair(Rank pairRank) {
        pairs.add(pairRank);
    }
    
    /**
     * 獲取踢腳牌列表
     * @return 踢腳牌列表
     */
    public List<Rank> getKickers() {
        return new ArrayList<>(kickers);
    }
    
    /**
     * 設定踢腳牌列表
     * @param kickers 踢腳牌列表
     */
    public void setKickers(List<Rank> kickers) {
        this.kickers = new ArrayList<>(kickers);
    }
    
    /**
     * 添加踢腳牌
     * @param kicker 踢腳牌
     */
    public void addKicker(Rank kicker) {
        kickers.add(kicker);
    }
    
    /**
     * 獲取關鍵牌組列表（用於同花等按順序比較的牌型）
     * @return 關鍵牌組列表
     */
    public List<Rank> getKeyCards() {
        return new ArrayList<>(keyCards);
    }
    
    /**
     * 設定關鍵牌組列表
     * @param keyCards 關鍵牌組列表
     */
    public void setKeyCards(List<Rank> keyCards) {
        this.keyCards = new ArrayList<>(keyCards);
    }
    
    /**
     * 檢查是否為低 A 順子（A-2-3-4-5）
     * @return 如果是低 A 順子返回 true
     */
    public boolean isLowAceStraight() {
        return isLowAceStraight;
    }
    
    /**
     * 設定是否為低 A 順子
     * @param lowAceStraight 是否為低 A 順子
     */
    public void setLowAceStraight(boolean lowAceStraight) {
        this.isLowAceStraight = lowAceStraight;
    }
    
    /**
     * 比較兩個牌型的強度
     * @param other 另一個牌型
     * @return 正數表示本牌型更強，負數表示對方更強，0表示相等
     */
    public int compareTo(Hand other) {
        // 首先比較牌型等級
        int rankComparison = this.rank.compareStrength(other.rank);
        if (rankComparison != 0) {
            return rankComparison;
        }
        
        // 牌型等級相同時，比較具體牌值
        return compareByDetails(other);
    }
    
    /**
     * 當牌型等級相同時，比較具體牌值
     * @param other 另一個牌型
     * @return 比較結果
     */
    private int compareByDetails(Hand other) {
        switch (this.rank) {
            case ROYAL_FLUSH:
                return 0; // 皇家同花順都是平手
                
            case STRAIGHT_FLUSH:
            case STRAIGHT:
                return compareKey(other);
                
            case FOUR_OF_A_KIND:
                // 先比較四條的牌值，再比較踢腳牌
                int fourOfAKindComp = compareKey(other);
                if (fourOfAKindComp != 0) return fourOfAKindComp;
                return compareKickers(other);
                
            case FULL_HOUSE:
                // 先比較三條的牌值，再比較對子的牌值
                int threeOfAKindComp = compareKey(other);
                if (threeOfAKindComp != 0) return threeOfAKindComp;
                return comparePairs(other);
                
            case FLUSH:
            case HIGH_CARD:
                return compareKeyCards(other);
                
            case THREE_OF_A_KIND:
                // 先比較三條的牌值，再比較踢腳牌
                int threeComp = compareKey(other);
                if (threeComp != 0) return threeComp;
                return compareKickers(other);
                
            case TWO_PAIR:
                // 先比較高對，再比較低對，最後比較踢腳牌
                int pairsComp = comparePairs(other);
                if (pairsComp != 0) return pairsComp;
                return compareKickers(other);
                
            case ONE_PAIR:
                // 先比較對子的牌值，再比較踢腳牌
                int pairComp = comparePairs(other);
                if (pairComp != 0) return pairComp;
                return compareKickers(other);
                
            default:
                return 0;
        }
    }
    
    private int compareKey(Hand other) {
        if (this.keyCard == null && other.keyCard == null) return 0;
        if (this.keyCard == null) return -1;
        if (other.keyCard == null) return 1;
        return this.keyCard.compareTo(other.keyCard);
    }
    
    private int comparePairs(Hand other) {
        for (int i = 0; i < Math.min(this.pairs.size(), other.pairs.size()); i++) {
            int comp = this.pairs.get(i).compareTo(other.pairs.get(i));
            if (comp != 0) return comp;
        }
        return Integer.compare(this.pairs.size(), other.pairs.size());
    }
    
    private int compareKickers(Hand other) {
        for (int i = 0; i < Math.min(this.kickers.size(), other.kickers.size()); i++) {
            int comp = this.kickers.get(i).compareTo(other.kickers.get(i));
            if (comp != 0) return comp;
        }
        return Integer.compare(this.kickers.size(), other.kickers.size());
    }
    
    private int compareKeyCards(Hand other) {
        for (int i = 0; i < Math.min(this.keyCards.size(), other.keyCards.size()); i++) {
            int comp = this.keyCards.get(i).compareTo(other.keyCards.get(i));
            if (comp != 0) return comp;
        }
        return Integer.compare(this.keyCards.size(), other.keyCards.size());
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Hand)) return false;
        Hand hand = (Hand) o;
        return rank == hand.rank &&
               Objects.equals(cards, hand.cards) &&
               Objects.equals(keyCard, hand.keyCard) &&
               Objects.equals(pairs, hand.pairs) &&
               Objects.equals(kickers, hand.kickers);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(rank, cards, keyCard, pairs, kickers);
    }
    
    @Override
    public String toString() {
        return "Hand{" +
               "rank=" + rank +
               ", keyCard=" + keyCard +
               ", pairs=" + pairs +
               ", kickers=" + kickers +
               ", cards=" + cards +
               '}';
    }
}