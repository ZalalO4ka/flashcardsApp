package com.example.flashcardsapp.model;

import java.util.UUID;

public class Flashcard {
    private String id; // Убрали final
    private String term;
    private String definition;
    private String imagePath;

    public Flashcard() {
        this.id = UUID.randomUUID().toString();
    }

    public Flashcard(String term, String definition) {
        this(term, definition, null);
    }

    public Flashcard(String term, String definition, String imagePath) {
        this.id = UUID.randomUUID().toString();
        this.term = term;
        this.definition = definition;
        this.imagePath = imagePath;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; } // Добавили сеттер

    public String getTerm() { return term; }
    public void setTerm(String term) { this.term = term; }

    public String getDefinition() { return definition; }
    public void setDefinition(String definition) { this.definition = definition; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
}