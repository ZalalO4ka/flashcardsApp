package com.example.flashcardsapp.model;

import java.util.ArrayList;
import java.util.List;

public class Deck {
    private String name;
    private String coverImagePath;
    private final List<Flashcard> cards;

    public Deck() {
        this.cards = new ArrayList<>();
    }

    public boolean removeCard(Flashcard card) {
        return cards.remove(card);
    }

    public Deck(String name) {
        this.name = name;
        this.cards = new ArrayList<>();
    }

    public void addCard(Flashcard card) {
        cards.add(card);
    }

    public Deck makeCopy(String newName) {
        Deck copy = new Deck(newName);
        copy.setCoverImagePath(this.coverImagePath);
        for (Flashcard card : this.cards) {
            copy.addCard(new Flashcard(card.getTerm(), card.getDefinition(), card.getImagePath()));
        }
        return copy;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCoverImagePath() { return coverImagePath; }
    public void setCoverImagePath(String coverImagePath) { this.coverImagePath = coverImagePath; }

    public List<Flashcard> getCards() { return cards; }
    public boolean hasCards() { return !cards.isEmpty(); }
}