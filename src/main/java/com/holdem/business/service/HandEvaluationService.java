package com.holdem.business.service;

import com.holdem.business.domain.Card;
import com.holdem.business.domain.Hand;
import com.holdem.shared.enums.HandRank;
import com.holdem.shared.enums.Rank;
import com.holdem.shared.enums.Suit;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 牌型評估服務
 * 負責德州撲克牌型的判定和比較
 * 
 * @author Development Team
 * @version 1.0
 * @since 2025-10-18
 */
@Service
public class HandEvaluationService {
    
    /**
     * 從七張牌中評估出最佳的五張牌組合
     * @param allCards 所有可用的牌（2張底牌 + 5張公共牌）
     * @return 最佳牌型
     */
    public Hand evaluateBestHand(List<Card> allCards) {
        if (allCards.size() != 7) {
            throw new IllegalArgumentException("Must provide exactly 7 cards for evaluation");
        }
        
        Hand bestHand = null;
        
        // 從7張牌中選出5張牌的所有組合（C(7,5) = 21種組合）
        List<List<Card>> combinations = generateCombinations(allCards, 5);
        
        for (List<Card> combination : combinations) {
            Hand currentHand = evaluateHand(combination);
            if (bestHand == null || currentHand.compareTo(bestHand) > 0) {
                bestHand = currentHand;
            }
        }
        
        return bestHand;
    }
    
    /**
     * 評估五張牌的牌型
     * @param cards 五張牌
     * @return 牌型
     */
    public Hand evaluateHand(List<Card> cards) {
        if (cards.size() != 5) {
            throw new IllegalArgumentException("Must provide exactly 5 cards for hand evaluation");
        }
        
        // 按牌值排序（從大到小）
        List<Card> sortedCards = cards.stream()
                .sorted((c1, c2) -> c2.getRank().compareTo(c1.getRank()))
                .collect(Collectors.toList());
        
        // 統計牌值和花色
        Map<Rank, Integer> rankCounts = countRanks(sortedCards);
        Map<Suit, Integer> suitCounts = countSuits(sortedCards);
        
        boolean isFlush = suitCounts.values().stream().anyMatch(count -> count == 5);
        boolean isStraight = isStraight(sortedCards);
        boolean isLowAceStraight = isLowAceStraight(sortedCards);
        
        // 判定牌型
        Hand hand = determineHandRank(sortedCards, rankCounts, isFlush, isStraight, isLowAceStraight);
        
        return hand;
    }
    
    /**
     * 比較兩個牌型
     * @param hand1 第一個牌型
     * @param hand2 第二個牌型
     * @return 正數表示hand1更強，負數表示hand2更強，0表示平手
     */
    public int compareHands(Hand hand1, Hand hand2) {
        return hand1.compareTo(hand2);
    }
    
    /**
     * 獲取勝利原因說明
     * @param winningHand 獲勝牌型
     * @param losingHand 失敗牌型
     * @return 勝利原因說明
     */
    public String getWinReason(Hand winningHand, Hand losingHand) {
        int comparison = winningHand.compareTo(losingHand);
        
        if (comparison == 0) {
            return "平手";
        }
        
        if (winningHand.getRank() != losingHand.getRank()) {
            return "牌型等級較高";
        }
        
        // 相同牌型等級的情況
        switch (winningHand.getRank()) {
            case FOUR_OF_A_KIND:
                if (!winningHand.getKeyCard().equals(losingHand.getKeyCard())) {
                    return "四條牌值較高";
                }
                return "踢腳牌較高";
                
            case FULL_HOUSE:
                if (!winningHand.getKeyCard().equals(losingHand.getKeyCard())) {
                    return "三條牌值較高";
                }
                return "對子牌值較高";
                
            case FLUSH:
            case HIGH_CARD:
                return "關鍵牌較高";
                
            case STRAIGHT:
            case STRAIGHT_FLUSH:
                return "順子高牌較高";
                
            case THREE_OF_A_KIND:
                if (!winningHand.getKeyCard().equals(losingHand.getKeyCard())) {
                    return "三條牌值較高";
                }
                return "踢腳牌較高";
                
            case TWO_PAIR:
                List<Rank> winPairs = winningHand.getPairs();
                List<Rank> losePairs = losingHand.getPairs();
                if (!winPairs.get(0).equals(losePairs.get(0))) {
                    return "高對牌值較高";
                }
                if (!winPairs.get(1).equals(losePairs.get(1))) {
                    return "低對牌值較高";
                }
                return "踢腳牌較高";
                
            case ONE_PAIR:
                if (!winningHand.getPairs().get(0).equals(losingHand.getPairs().get(0))) {
                    return "對子牌值較高";
                }
                return "踢腳牌較高";
                
            default:
                return "牌值較高";
        }
    }
    
    // 私有方法實現
    
    private Hand determineHandRank(List<Card> sortedCards, Map<Rank, Integer> rankCounts, 
                                  boolean isFlush, boolean isStraight, boolean isLowAceStraight) {
        
        List<Integer> counts = rankCounts.values().stream()
                .sorted(Collections.reverseOrder())
                .collect(Collectors.toList());
        
        // 皇家同花順
        if (isFlush && isStraight && sortedCards.get(0).getRank() == Rank.ACE && 
            sortedCards.get(1).getRank() == Rank.KING) {
            Hand hand = new Hand(HandRank.ROYAL_FLUSH, sortedCards);
            return hand;
        }
        
        // 同花順
        if (isFlush && (isStraight || isLowAceStraight)) {
            Hand hand = new Hand(HandRank.STRAIGHT_FLUSH, sortedCards);
            if (isLowAceStraight) {
                hand.setKeyCard(Rank.FIVE);
                hand.setLowAceStraight(true);
            } else {
                hand.setKeyCard(sortedCards.get(0).getRank());
            }
            return hand;
        }
        
        // 四條
        if (counts.get(0) == 4) {
            Hand hand = new Hand(HandRank.FOUR_OF_A_KIND, sortedCards);
            Rank fourOfAKindRank = findRankWithCount(rankCounts, 4);
            Rank kicker = findRankWithCount(rankCounts, 1);
            hand.setKeyCard(fourOfAKindRank);
            hand.addKicker(kicker);
            return hand;
        }
        
        // 葫蘆
        if (counts.get(0) == 3 && counts.get(1) == 2) {
            Hand hand = new Hand(HandRank.FULL_HOUSE, sortedCards);
            Rank threeOfAKindRank = findRankWithCount(rankCounts, 3);
            Rank pairRank = findRankWithCount(rankCounts, 2);
            hand.setKeyCard(threeOfAKindRank);
            hand.addPair(pairRank);
            return hand;
        }
        
        // 同花
        if (isFlush) {
            Hand hand = new Hand(HandRank.FLUSH, sortedCards);
            List<Rank> keyCards = sortedCards.stream()
                    .map(Card::getRank)
                    .collect(Collectors.toList());
            hand.setKeyCards(keyCards);
            return hand;
        }
        
        // 順子
        if (isStraight || isLowAceStraight) {
            Hand hand = new Hand(HandRank.STRAIGHT, sortedCards);
            if (isLowAceStraight) {
                hand.setKeyCard(Rank.FIVE);
                hand.setLowAceStraight(true);
            } else {
                hand.setKeyCard(sortedCards.get(0).getRank());
            }
            return hand;
        }
        
        // 三條
        if (counts.get(0) == 3) {
            Hand hand = new Hand(HandRank.THREE_OF_A_KIND, sortedCards);
            Rank threeOfAKindRank = findRankWithCount(rankCounts, 3);
            List<Rank> kickers = findRanksWithCount(rankCounts, 1);
            hand.setKeyCard(threeOfAKindRank);
            hand.setKickers(kickers);
            return hand;
        }
        
        // 兩對
        if (counts.get(0) == 2 && counts.get(1) == 2) {
            Hand hand = new Hand(HandRank.TWO_PAIR, sortedCards);
            List<Rank> pairs = findRanksWithCount(rankCounts, 2);
            pairs.sort(Collections.reverseOrder()); // 確保高對在前
            Rank kicker = findRankWithCount(rankCounts, 1);
            hand.setPairs(pairs);
            hand.addKicker(kicker);
            return hand;
        }
        
        // 一對
        if (counts.get(0) == 2) {
            Hand hand = new Hand(HandRank.ONE_PAIR, sortedCards);
            Rank pairRank = findRankWithCount(rankCounts, 2);
            List<Rank> kickers = findRanksWithCount(rankCounts, 1);
            hand.addPair(pairRank);
            hand.setKickers(kickers);
            return hand;
        }
        
        // 高牌
        Hand hand = new Hand(HandRank.HIGH_CARD, sortedCards);
        List<Rank> keyCards = sortedCards.stream()
                .map(Card::getRank)
                .collect(Collectors.toList());
        hand.setKeyCards(keyCards);
        return hand;
    }
    
    private Map<Rank, Integer> countRanks(List<Card> cards) {
        Map<Rank, Integer> counts = new HashMap<>();
        for (Card card : cards) {
            counts.merge(card.getRank(), 1, Integer::sum);
        }
        return counts;
    }
    
    private Map<Suit, Integer> countSuits(List<Card> cards) {
        Map<Suit, Integer> counts = new HashMap<>();
        for (Card card : cards) {
            counts.merge(card.getSuit(), 1, Integer::sum);
        }
        return counts;
    }
    
    private boolean isStraight(List<Card> sortedCards) {
        for (int i = 0; i < 4; i++) {
            if (sortedCards.get(i).getRank().getValue() != 
                sortedCards.get(i + 1).getRank().getValue() + 1) {
                return false;
            }
        }
        return true;
    }
    
    private boolean isLowAceStraight(List<Card> sortedCards) {
        // A-2-3-4-5 順子
        return sortedCards.get(0).getRank() == Rank.ACE &&
               sortedCards.get(1).getRank() == Rank.FIVE &&
               sortedCards.get(2).getRank() == Rank.FOUR &&
               sortedCards.get(3).getRank() == Rank.THREE &&
               sortedCards.get(4).getRank() == Rank.TWO;
    }
    
    private Rank findRankWithCount(Map<Rank, Integer> rankCounts, int count) {
        return rankCounts.entrySet().stream()
                .filter(entry -> entry.getValue() == count)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }
    
    private List<Rank> findRanksWithCount(Map<Rank, Integer> rankCounts, int count) {
        return rankCounts.entrySet().stream()
                .filter(entry -> entry.getValue() == count)
                .map(Map.Entry::getKey)
                .sorted(Collections.reverseOrder())
                .collect(Collectors.toList());
    }
    
    private List<List<Card>> generateCombinations(List<Card> cards, int k) {
        List<List<Card>> combinations = new ArrayList<>();
        generateCombinationsHelper(cards, k, 0, new ArrayList<>(), combinations);
        return combinations;
    }
    
    private void generateCombinationsHelper(List<Card> cards, int k, int start, 
                                          List<Card> current, List<List<Card>> result) {
        if (current.size() == k) {
            result.add(new ArrayList<>(current));
            return;
        }
        
        for (int i = start; i < cards.size(); i++) {
            current.add(cards.get(i));
            generateCombinationsHelper(cards, k, i + 1, current, result);
            current.remove(current.size() - 1);
        }
    }
}