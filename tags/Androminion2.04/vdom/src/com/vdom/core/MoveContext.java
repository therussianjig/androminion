package com.vdom.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import com.vdom.api.ActionCard;
import com.vdom.api.Card;
import com.vdom.api.CardCostComparator;
import com.vdom.api.GameEventListener;
import com.vdom.api.GameType;
import com.vdom.api.TreasureCard;
import com.vdom.api.VictoryCard;

public class MoveContext {
    public int actions = 1;
    public int buys = 1;
    public int addGold = 0;

    public int gold;
    public int potions;
    public int actionsPlayedSoFar = 0;
    public int treasuresPlayedSoFar = 0;
    public int goldAvailable;
    public boolean copperPlayed = false;
    public int coppersmithsPlayed = 0;
    public int goonsPlayed = 0;
    public int hoardsPlayed = 0;
    public int freeActionInEffect = 0;
    public int quarriesPlayed = 0;
    public boolean royalSealPlayed = false;
    public int talismansPlayed = 0;
    public int foolsGoldPlayed = 0;
    public int schemesPlayed = 0;
    public int cardCostModifier = 0;
    public int victoryCardsBoughtThisTurn = 0;
    public int totalCardsBoughtThisTurn = 0;
    public boolean buyPhase = false;
    public ArrayList<Card> cantBuy = new ArrayList<Card>();

    // For checking Achievements
    public int vpsGainedThisTurn = 0;
    public int cardsTrashedThisTurn = 0;

    public String message;
    public ArrayList<Card> playedCards = new ArrayList<Card>();
    public Player player;
    public Game game;
    
    public Player attackedPlayer;

    public MoveContext(Game game, Player player) {
        this.game = game;
        this.player = player;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public int calculateLead(Card card) {
        int lead = game.calculateLead(player);
        if (canBuy(card) && card instanceof VictoryCard) {
            lead += ((VictoryCard) card).getVictoryPoints();
        }

        return lead;
    }
    
    public boolean isQuickPlay() {
        return Game.quickPlay;
    }
    
    public int getPotions() {
        return potions;
    }
    
    public ArrayList<Card> getCantBuy() {
        return cantBuy;
    }

    public int getActionCardsInPlayThisTurn() {
        int actionsInPlay = 0;
        for(Card c : getPlayedCards()) {
            if(c instanceof ActionCard) {
                actionsInPlay++;
            }
        }
        for(Card c : player.nextTurnCards) {
            if(c instanceof ActionCard) {
                actionsInPlay++;
            }
        }

        return actionsInPlay;
    }
    
    public int getVictoryCardsBoughtThisTurn() {
        return victoryCardsBoughtThisTurn;
    }

    public int getTotalCardsBoughtThisTurn() {
        return totalCardsBoughtThisTurn;
    }

    public boolean buyWouldEndGame(Card card) {
        return game.buyWouldEndGame(card);
    }

    public int getThroneRoomsInEffect() {
        return freeActionInEffect;
    }

    public int getQuarriesPlayed() {
        return quarriesPlayed;
    }
    
    public ArrayList<Card> getPlayedCards() {
        return playedCards;
    }

    public int getPileSize(Card card) {
        return game.pileSize(card);
    }

    public int emptyPileCount() {
        return game.emptyPiles();
    }

    public int getEmbargos(Card card) {
        return game.getEmbargos(card.getName());
    }

    public ArrayList<Card> getCardsObtainedByLastPlayer() {
        return game.getCardsObtainedByLastPlayer();
    }

    public HashMap<String, Integer> getCardCounts() {
        HashMap<String, Integer> cardCounts = new HashMap<String, Integer>();
        for (String cardName : game.piles.keySet()) {
            int count = game.piles.get(cardName).getCount();
            if (count > 0) {
                cardCounts.put(cardName, count);
            }
        }
        return cardCounts;
    }

    public Card[] getBuyableCards() {
        ArrayList<Card> buyableCards = new ArrayList<Card>();
        for (Card card : getCardsInPlay()) {
            if (canBuy(card)) {
                buyableCards.add(card);
            }
        }

        Collections.sort(buyableCards, new CardCostComparator());
        return buyableCards.toArray(new Card[0]);
    }

    public void addGameListener(GameEventListener listener) {
        if (listener != null && !game.listeners.contains(listener)) {
            game.listeners.add(listener);
        }
    }

    public void removeGameListener(GameEventListener listener) {
        if (listener != null && game.listeners.contains(listener)) {
            game.listeners.remove(listener);
        }
    }

    public boolean cardsSpecifiedOnStartup() {
        return Game.cardsSpecifiedAtLaunch != null && Game.cardsSpecifiedAtLaunch.length > 0;
    }

    public GameType getGameType() {
        return Game.gameType;
    }

    public boolean canPlay(Card card) {
        if (card instanceof ActionCard) {
            return game.isValidAction(this, (ActionCard) card);
        } else {
            return false;
        }
    }

    public boolean canBuy(Card card) {
        return game.isValidBuy(this, card);
    }

    public boolean canBuy(Card card, int gold) {
        return game.isValidBuy(this, card, gold);
    }

    public int getActionsLeft() {
        return actions;
    }

    public int getBuysLeft() {
        return buys;
    }

    public int getCoinAvailableForBuy() {
        return gold + addGold;
    }

    public int getCoinForStatus() {
        if(playedCards.size() > 0) {
            return getCoinAvailableForBuy();
        }

        int coin = 0;
        int foolsgoldcount = 0;
        for (Card card : player.getHand()) {
            if (card instanceof TreasureCard) {
                coin += ((TreasureCard) card).getValue();
                 if (card.getType() == Cards.Type.FoolsGold) {
                 foolsgoldcount++;
                 if (foolsgoldcount > 1) {
                 coin += 3;
                 }
                 }
            }
        }

        return coin;
    }

    public int getPotionsForStatus(Player p) {
        if(playedCards.size() > 0) {
            return potions;
        }

        int count = 0;
        for (Card card : player.getHand()) {
            if (card.equals(Cards.potion)) {
                count++;
            }
        }

        return count;
    }
    
    public Card[] getCardsInPlay() {
        return getCardsInPlay(null);
    }

    public Card[] getCardsInPlay(Class<?> c) {
        ArrayList<Card> cards = new ArrayList<Card>();
        for (CardPile pile : game.piles.values()) {
            if (c == null || c.isInstance(pile.card))
                cards.add(pile.card);
        }
        return cards.toArray(new Card[0]);
    }

    public Card[] getActionsInPlay() {
        return getCardsInPlay(ActionCard.class);
    }

    public boolean cardInPlay(Card card) {
        boolean cardInPlay = false;
        for (Card thisCard : getCardsInPlay()) {
            if (thisCard.equals(card)) {
                cardInPlay = true;
                break;
            }
        }
        return cardInPlay;
    }

    public Card[] getTreasureCardsInPlay() {
        return getCardsInPlay(TreasureCard.class);
    }

    public Card[] getCardsInPlayOrderByCost() {
        Card[] cardsInPlay = getCardsInPlay();
        Arrays.sort(cardsInPlay, new CardCostComparator());
        return cardsInPlay;
    }

    public int getCardsLeft(Card card) {
        CardPile pile = game.piles.get(card.getName());
        if (pile == null || pile.getCount() < 0) {
            return 0;
        }

        return pile.getCount();
    }

    public void debug(String msg) {
        debug(msg, true);
    }

    private void debug(String msg, boolean prefixWithPlayerName) {
        if (!prefixWithPlayerName || player == null) {
            Util.debug(msg);
        } else {
            player.debug(msg);
        }
    }
    
    public String getAttackedPlayer() {
        return (attackedPlayer == null)?null:attackedPlayer.getPlayerName();
    }
    
    public String getMessage() {
        return message;
    }
}