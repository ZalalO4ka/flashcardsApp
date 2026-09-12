package com.example.flashcardsapp.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {
    private String name;
    private String coverImagePath; // Кастомная рубашка/картинка колоды
    private List<Flashcard> cards;
    private int currentIndex;

    public Deck() {
        this.cards = new java.util.ArrayList<>();
        this.currentIndex = 0;
    }

    public Deck(String name) {
        this.name = name;
        this.cards = new ArrayList<>();
        this.currentIndex = 0;
    }

    // CRUD для карточек
    public void addCard(Flashcard card) {
        cards.add(card);
    }

    public boolean removeCard(String cardId) {
        boolean removed = cards.removeIf(card -> card.getId().equals(cardId));
        if (currentIndex >= cards.size() && !cards.isEmpty()) {
            currentIndex = cards.size() - 1;
        }
        return removed;
    }

    public Flashcard getCurrentCard() {
        if (cards.isEmpty()) return null;
        if (currentIndex >= cards.size()) currentIndex = 0;
        return cards.get(currentIndex);
    }

    public Flashcard nextCard() {
        if (cards.isEmpty()) return null;
        currentIndex = (currentIndex + 1) % cards.size();
        return getCurrentCard();
    }

    public void shuffle() {
        Collections.shuffle(cards);
        currentIndex = 0;
    }

    // Клонирование колоды (для копирования другому ученику)
    public Deck makeCopy(String newName) {
        Deck copy = new Deck(newName);
        copy.setCoverImagePath(this.coverImagePath);
        for (Flashcard card : this.cards) {
            copy.addCard(new Flashcard(card.getTerm(), card.getDefinition(), card.getImagePath()));
        }
        return copy;
    }
    public void setCards(List<Flashcard> cards) {
        this.cards = cards;
    }

    // Геттеры и сеттеры
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCoverImagePath() { return coverImagePath; }
    public void setCoverImagePath(String coverImagePath) { this.coverImagePath = coverImagePath; }

    public List<Flashcard> getCards() { return cards; }
    public boolean isEmpty() { return cards.isEmpty(); }
}