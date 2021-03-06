package com.codecool.klondike;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Game extends Pane {

    private List<Card> deck = new ArrayList<>();

    private Pile stockPile;
    private Pile discardPile;
    private List<Pile> foundationPiles = FXCollections.observableArrayList();
    private List<Pile> tableauPiles = FXCollections.observableArrayList();

    private double dragStartX, dragStartY;
    private List<Card> draggedCards = FXCollections.observableArrayList();

    private static double STOCK_GAP = 0.3;
    private static double FOUNDATION_GAP = 0;
    private static double TABLEAU_GAP = 30;

    private EventHandler<MouseEvent> onMouseClickedHandler = e -> {
        Card card = (Card) e.getSource();
        Pile activePile = card.getContainingPile();
        Card topCard = activePile.getTopCard();
        if (card.getContainingPile().getPileType() == Pile.PileType.STOCK) {
            card.moveToPile(discardPile);
            card.flip();
            card.setMouseTransparent(false);
            System.out.println("Placed " + card + " to the waste.");
            stockPile.numOfCards(); //Counts the cards in the discord pile during the game.
            discardPile.numOfCards(); //Counts the cards in the discord pile during the game.
        }
        if (card.getContainingPile().getPileType() == Pile.PileType.TABLEAU)
            if (card.isFaceDown() && (topCard.equals(card))) {
                card.flip();
            }
    };

    private EventHandler<MouseEvent> stockReverseCardsHandler = e -> {
        refillStockFromDiscard();
    };

    private EventHandler<MouseEvent> onMousePressedHandler = e -> {
        dragStartX = e.getSceneX();
        dragStartY = e.getSceneY();
    };

    private EventHandler<MouseEvent> onMouseDraggedHandler = e -> {
        Card card = (Card) e.getSource();
        Pile activePile = card.getContainingPile();
        draggedCards.clear();
        card.toFront();

        if (activePile.getPileType() == Pile.PileType.STOCK) {
            return;
        }
        double offsetX = e.getSceneX() - dragStartX;
        double offsetY = e.getSceneY() - dragStartY;

        if (activePile.getPileType() == Pile.PileType.TABLEAU) {


            for (int i = activePile.getCards().indexOf(card); i < activePile.getCards().size(); i++) {
                draggedCards.add(activePile.getCards().get(i));
            }

        } else {
            draggedCards.add(card);
        }

        for (Card item : draggedCards) {
            item.getDropShadow().setRadius(20);
            item.getDropShadow().setOffsetX(10);
            item.getDropShadow().setOffsetY(10);

            item.setTranslateX(offsetX);
            item.setTranslateY(offsetY);
            item.toFront();
        }


    };

    private EventHandler<MouseEvent> onMouseReleasedHandler = e -> {

        if (draggedCards.isEmpty())
            return;
        Card card = (Card) e.getSource();

        Pile pile = getValidIntersectingPile(card, tableauPiles);  // Ebben a methodusban már meghivtuk az isMOveValid methodust

        if (pile != null) {
            handleValidMove(card, pile);
        Pile tableaupile = getValidIntersectingPile(card, tableauPiles);
        Pile foundationpile = getValidIntersectingPile(card, foundationPiles);
        if (tableaupile != null) {
            handleValidMove(card, tableaupile);
        }else if (foundationpile!=null) {
            handleValidMove(card, foundationpile);

        } else {
            draggedCards.forEach(MouseUtil::slideBack);
            draggedCards.clear();
        }
    };

    public boolean isGameWon() {
        //TODO win win
        return false;
    }

    public Game() {
        deck = Card.createNewDeck();
        //System.out.println("deck" + deck.toString());
        initPiles();
        shuffleCards();

        dealCards();
    }


    public void addMouseEventHandlers(Card card) {
        card.setOnMousePressed(onMousePressedHandler);
        card.setOnMouseDragged(onMouseDraggedHandler);
        card.setOnMouseReleased(onMouseReleasedHandler);
        card.setOnMouseClicked(onMouseClickedHandler);
    }

    public void refillStockFromDiscard() {
        ArrayList<Card> lista = new ArrayList<Card>(discardPile.getCards());
        for (Card item : lista) {
            item.moveToPile(stockPile);
            item.flip();
        }
    }

    public boolean isMoveValid(Card card, Pile destPile) {
        if (destPile.getPileType().equals(Pile.PileType.FOUNDATION)) {

            if (destPile.getTopCard() == null) {
                if (Card.isAce(card) == true) {
                    return true;
                }
                return false;
            } else {
                if (Card.isAscendingOrder(card, destPile.getTopCard()) == true) {
                    return true;
                }
                return false;
            }

        } else if (destPile.getPileType().equals(Pile.PileType.TABLEAU)) {
            if (destPile.getTopCard() == null) {
                if (Card.isItAKing(card) == true) {
                    return true;
                }
                return false;
            } else {
                if ((Card.isOppositeColor(card, destPile.getTopCard()) == true) && 
                    (Card.isDescendingOrder(card, destPile.getTopCard()) == true)) {
                    return true;
                }
                return false;
            }
        }return false;
    }
        
    private Pile getValidIntersectingPile(Card card, List<Pile> piles) {
        Pile result = null;
        for (Pile pile : piles) {
            if (!pile.equals(card.getContainingPile()) &&
                    isOverPile(card, pile) &&
                    isMoveValid(card, pile))
                result = pile;
        }
        return result;
    }

    private boolean isOverPile(Card card, Pile pile) {
        if (pile.isEmpty())
            return card.getBoundsInParent().intersects(pile.getBoundsInParent());
        else
            return card.getBoundsInParent().intersects(pile.getTopCard().getBoundsInParent());
    }

    private void handleValidMove(Card card, Pile destPile) {
        String msg = null;
        if (destPile.isEmpty()) {
            if (destPile.getPileType().equals(Pile.PileType.FOUNDATION))
                msg = String.format("Placed %s to the foundation.", card);
            if (destPile.getPileType().equals(Pile.PileType.TABLEAU))
                msg = String.format("Placed %s to a new pile.", card);
        } else {
            msg = String.format("Placed %s to %s.", card, destPile.getTopCard());
        }
        System.out.println(msg);
        MouseUtil.slideToDest(draggedCards, destPile);
        draggedCards.clear();
    }

    private void initPiles() {
        stockPile = new Pile(Pile.PileType.STOCK, "Stock", STOCK_GAP);
        stockPile.setBlurredBackground();
        stockPile.setLayoutX(95);
        stockPile.setLayoutY(20);
        stockPile.setOnMouseClicked(stockReverseCardsHandler);
        getChildren().add(stockPile);

        discardPile = new Pile(Pile.PileType.DISCARD, "Discard", STOCK_GAP);
        discardPile.setBlurredBackground();
        discardPile.setLayoutX(285);
        discardPile.setLayoutY(20);
        getChildren().add(discardPile);

        for (int i = 0; i < 4; i++) {
            Pile foundationPile = new Pile(Pile.PileType.FOUNDATION, "Foundation " + i, FOUNDATION_GAP);
            foundationPile.setBlurredBackground();
            foundationPile.setLayoutX(610 + i * 180);
            foundationPile.setLayoutY(20);
            foundationPiles.add(foundationPile);
            getChildren().add(foundationPile);
        }
        for (int i = 0; i < 7; i++) {
            Pile tableauPile = new Pile(Pile.PileType.TABLEAU, "Tableau " + i, TABLEAU_GAP);
            tableauPile.setBlurredBackground();
            tableauPile.setLayoutX(95 + i * 180);
            tableauPile.setLayoutY(275);
            tableauPiles.add(tableauPile);
            getChildren().add(tableauPile);
        }
    }

    public void dealCards() {
        Iterator<Card> deckIterator = deck.iterator();
        int countCard = 0;
        for (int i = 0; i < tableauPiles.size(); i++) {
            for (int j = 0; j <= i; j++) {
                tableauPiles.get(i).addCard(deck.get(countCard++));
                if (j == i) {
                    tableauPiles.get(i).getTopCard().flip();
                }
            }
        }
        for (; countCard < deck.size(); countCard++) {
            stockPile.addCard(deck.get(countCard));
        }

        deckIterator.forEachRemaining(card -> {
            addMouseEventHandlers(card);
            getChildren().add(card);

        });
        stockPile.numOfCards(); //Counts the cards in the stock pile at the start.

    }

    public void shuffleCards() {
        Collections.shuffle(deck);
    }

    public void setTableBackground(Image tableBackground) {
        setBackground(new Background(new BackgroundImage(tableBackground,
                BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT,
                BackgroundPosition.CENTER, BackgroundSize.DEFAULT)));
    }

    @Override
    public String toString() {
        return "Game{" +
                "deck=" + deck +
                ", stockPile=" + stockPile +
                ", discardPile=" + discardPile +
                ", foundationPiles=" + foundationPiles +
                ", tableauPiles=" + tableauPiles +
                ", dragStartX=" + dragStartX +
                ", dragStartY=" + dragStartY +
                ", draggedCards=" + draggedCards +
                ", onMouseClickedHandler=" + onMouseClickedHandler +
                ", stockReverseCardsHandler=" + stockReverseCardsHandler +
                ", onMousePressedHandler=" + onMousePressedHandler +
                ", onMouseDraggedHandler=" + onMouseDraggedHandler +
                ", onMouseReleasedHandler=" + onMouseReleasedHandler +
                '}';
    }
}
