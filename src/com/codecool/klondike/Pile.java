package com.codecool.klondike;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.ListIterator;

public class Pile extends Pane {

    private PileType pileType;
    private String name; // Fasznak sem kell. ... törölhető
    private double cardGap;
    private ObservableList<Card> cards = FXCollections.observableArrayList(); // TODO ebbe teszi bele a felforgatott kártyát

    public Pile(PileType pileType, String name, double cardGap) {
        this.pileType = pileType;
        this.cardGap = cardGap;
    }

    public PileType getPileType() {
        return pileType;
    }

    public String getName() {
        return name;
    }

    public double getCardGap() {
        return cardGap;
    }

    public ObservableList<Card> getCards() {
        return cards;
    }

    public int numOfCards() {
        int numberOfCards = cards.size();
//        String pileName = getName(); //How to print out the name of the pile?
        System.out.println("There is " + numberOfCards + " cards in ? pile.");
        return numberOfCards;
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public void clear() {
        //TODO  Meghivható amikor a diskardbol akarjuk a kártyákat visszarakni a sktackbe
        this.clear();
    }

    public void addCard(Card card) {
        cards.add(card);
        card.setContainingPile(this);
        card.toFront();
        layoutCard(card);
    }

    private void layoutCard(Card card) {
        card.relocate(card.getLayoutX() + card.getTranslateX(), card.getLayoutY() + card.getTranslateY());
        card.setTranslateX(0);
        card.setTranslateY(0);
        card.setLayoutX(getLayoutX());
        card.setLayoutY(getLayoutY() + (cards.size() - 1) * cardGap);
    }

    public Card getTopCard() {
        if (cards.isEmpty())
            return null;
        else
            return cards.get(cards.size() - 1);
    }

    public void setBlurredBackground() {
        setPrefSize(Card.WIDTH, Card.HEIGHT);
        BackgroundFill backgroundFill = new BackgroundFill(Color.gray(0.0, 0.2), null, null);
        Background background = new Background(backgroundFill);
        GaussianBlur gaussianBlur = new GaussianBlur(10);
        setBackground(background);
        setEffect(gaussianBlur);
    }

    public enum PileType {
        STOCK,
        DISCARD,
        FOUNDATION,
        TABLEAU
    }

    @Override
    public String toString() {
        return "Pile{" +
                "pileType=" + pileType +
                ", name='" + name + '\'' +
                ", cardGap=" + cardGap +
                ", cards=" + cards +
                '}';
    }
}
