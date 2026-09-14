package com.example.flashcardsapp.model;

import java.util.HashMap;
import java.util.Map;

public class Student {
    private String name;
    private final Map<String, Deck> decks;

    public Student() {
        this.decks = new HashMap<>();
    }

    public Student(String name) {
        this.name = name;
        this.decks = new HashMap<>();
    }

    public void addDeck(Deck deck) {
        decks.put(deck.getName(), deck);
    }

    public boolean removeDeck(String deckName) {
        return decks.remove(deckName) != null;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Map<String, Deck> getDecks() { return decks; }
}