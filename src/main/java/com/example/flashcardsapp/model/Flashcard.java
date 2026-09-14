package com.example.flashcardsapp.model;

import java.util.UUID;

public class Flashcard {
    private String id;
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
    @SuppressWarnings("unused")
        public String getId () {
            return id;
        }
    @SuppressWarnings("unused")
        public void setId (String id){
            this.id = id;
        }

    public String getTerm() { return term; }
    public void setTerm(String term) { this.term = term; }

    public String getDefinition() { return definition; }
    public void setDefinition(String definition) { this.definition = definition; }

    public String getImagePath() { return imagePath; }
}