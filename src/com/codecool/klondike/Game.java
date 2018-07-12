package com.codecool.klondike;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Game extends Pane {

    private static double STOCK_GAP = 0.3;
    private static double FOUNDATION_GAP = 0;
    private static double TABLEAU_GAP = 30;
    private List<Card> deck = new ArrayList<>();
    private Pile stockPile;
    private Pile discardPile;
    private List<Pile> foundationPiles = FXCollections.observableArrayList();
    private List<Pile> tableauPiles = FXCollections.observableArrayList();
    private double dragStartX, dragStartY;
    private List<Card> draggedCards = FXCollections.observableArrayList();
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
        System.out.println(isGameWon());
        if (draggedCards.isEmpty())
            return;
        Card card = (Card) e.getSource();
        Pile tableaupile = getValidIntersectingPile(card, tableauPiles);
        Pile foundationpile = getValidIntersectingPile(card, foundationPiles);
        System.out.println(foundationPiles);
        if (tableaupile != null) {
            if (draggedCards.size() <= 1) {
                handleValidMove(card, tableaupile);
                if (!card.getContainingPile().getPileType().equals(Pile.PileType.DISCARD) && card.getContainingPile().getLastoBeforeTopCard().isFaceDown())
                    card.getContainingPile().getLastoBeforeTopCard().flip();
            } else if (draggedCards.size() > 1) {
                handleValidMove(card, tableaupile);
                if (!card.getContainingPile().getPileType().equals(Pile.PileType.DISCARD) && card.getContainingPile().getLastoBeforeTopCard().isFaceDown())
                    card.getContainingPile().getLastoBeforeTopCard().flip();
                System.out.println("szivás");
            }
        } else if (foundationpile != null) {
            if (draggedCards.size() <= 1) {
                handleValidMove(card, foundationpile);
                isGameWon();
                if (!card.getContainingPile().getPileType().equals(Pile.PileType.DISCARD) && card.getContainingPile().getLastoBeforeTopCard().isFaceDown())
                    card.getContainingPile().getLastoBeforeTopCard().flip();
            }
        } else {
            draggedCards.forEach(MouseUtil::slideBack);
            draggedCards.clear();
        }

    };

    public Game() {
        deck = Card.createNewDeck();
        initPiles();
        shuffleCards();
        dealCards();
    }

    public boolean isGameWon() {
        if ((foundationPiles.get(0).numOfCards() == 1) && (foundationPiles.get(1).numOfCards() == 0) &&
                (foundationPiles.get(2).numOfCards() == 0) && (foundationPiles.get(3).numOfCards() == 0)) {
            JOptionPane.showMessageDialog(null, "You won!!!!!", "Goood boooy!!", JOptionPane.PLAIN_MESSAGE);
            restart();
            deck = Card.createNewDeck();
            shuffleCards();
            dealCards();

            //return true;
        }
        return false;
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
        }
        return false;
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
                    if (tableauPiles.get(i).getTopCard().isFaceDown())
                        tableauPiles.get(i).getTopCard().flip();
/*                } else {
                    if (tableauPiles.get(i).getCards().get(i).isFaceDown())
                        tableauPiles.get(i).getCards().get(i).flip();*/
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
        //stockPile.numOfCards();

    }

    public void shuffleCards() {
        Collections.shuffle(deck);
    }

    public void setTableBackground(Image tableBackground) {
        setBackground(new Background(new BackgroundImage(tableBackground,
                BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT,
                BackgroundPosition.CENTER, BackgroundSize.DEFAULT)));
    }

    public HBox addHBox() {
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(15, 12, 15, 12));
        hbox.setSpacing(10);
        hbox.setStyle("-fx-background-color: #336699;");

        Button buttonNewGame = new Button("New Game");
        buttonNewGame.setPrefSize(100, 20);
        buttonNewGame.setOnAction(__ ->
        {
            System.out.println("started a new game");
            restart();
            deck = Card.createNewDeck();
            shuffleCards();
            dealCards();
        });


        Button buttonRestartGame = new Button("RestartGame");
        buttonRestartGame.setPrefSize(100, 20);

        Scene primaryStage = getScene();
        buttonRestartGame.setOnAction(__ ->
        {
            System.out.println("restarting app");
            restart();
/*            for (int i = 0; i < tableauPiles.size(); i++) {
                for (int j = 0; j <= i; j++) {
                    if (tableauPiles.get(i).getTopCard().isFaceDown())
                        tableauPiles.get(i).getTopCard().flip();
                }
            }*/
            dealCards();

        });

        hbox.getChildren().addAll(buttonNewGame, buttonRestartGame);
        return hbox;
    }

    private void restart() {
        for (Pile p : tableauPiles) {
            for (Card c : p.getCards()) {
                getChildren().remove(c);
            }
            p.getCards().clear();
        }

        for (Pile p : foundationPiles) {
            for (Card c : p.getCards()) {
                getChildren().remove(c);
            }
            p.getCards().clear();
        }

        for (Card c : stockPile.getCards()) {
            getChildren().remove(c);
        }
        stockPile.getCards().clear();

        for (Card c : discardPile.getCards()) {

            getChildren().remove(c);
        }

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
