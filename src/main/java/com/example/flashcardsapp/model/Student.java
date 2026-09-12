package com.example.flashcardsapp.model;

import java.util.HashMap;
import java.util.Map;

public class Student {
    private String name;
    // Ключ: Название колоды, Значение: Колода
    private Map<String, Deck> decks;

    public Student() {
        this.decks = new java.util.HashMap<>();
    }

    public Student(String name) {
        this.name = name;
        this.decks = new HashMap<>();
    }

    public void addDeck(Deck deck) {
        decks.put(deck.getName(), deck);
    }

    public boolean renameDeck(String oldName, String newName) {
        if (decks.containsKey(oldName) && !decks.containsKey(newName)) {
            Deck deck = decks.remove(oldName);
            deck.setName(newName);
            decks.put(newName, deck);
            return true;
        }
        return false;
    }

    public boolean removeDeck(String deckName) {
        return decks.remove(deckName) != null;
    }

    public Deck getDeck(String deckName) {
        return decks.get(deckName);
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Map<String, Deck> getDecks() { return decks; }
}