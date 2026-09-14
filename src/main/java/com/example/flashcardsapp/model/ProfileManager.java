package com.example.flashcardsapp.model;

import java.util.HashMap;
import java.util.Map;

public class ProfileManager {
    private Map<String, Student> students = new HashMap<>();
    private Student currentStudent;
    private Deck currentDeck;

    public Map<String, Student> getStudents() { return students; }

    public void setStudents(Map<String, Student> students) {
        this.students = (students != null) ? students : new HashMap<>();
        if (!this.students.isEmpty() && currentStudent == null) {
            this.currentStudent = this.students.values().iterator().next();
        }
    }

    public Student getCurrentStudent() { return currentStudent; }

    public void setCurrentStudent(String name) {
        if (students.containsKey(name)) {
            this.currentStudent = students.get(name);
        }
    }

    public Deck getCurrentDeck() { return currentDeck; }
    public void setCurrentDeck(Deck deck) { this.currentDeck = deck; }

    public boolean addStudent(String name) {
        if (students.containsKey(name)) return false;
        Student student = new Student(name);
        students.put(name, student);
        currentStudent = student;
        return true;
    }

    public boolean addDeckToCurrentStudent(String deckName) {
        if (currentStudent == null || currentStudent.getDecks().containsKey(deckName)) return false;
        Deck deck = new Deck(deckName);
        currentStudent.getDecks().put(deckName, deck);
        currentDeck = deck;
        return true;
    }

    public boolean renameStudent(String oldName, String newName) {
        if (students.containsKey(oldName) && !students.containsKey(newName)) {
            Student student = students.remove(oldName);
            student.setName(newName);
            students.put(newName, student);
            return true;
        }
        return false;
    }

    public boolean renameDeck(Deck deck, String newName) {
        if (currentStudent != null && deck != null) {
            String oldName = deck.getName();
            if (currentStudent.getDecks().containsKey(oldName) && !currentStudent.getDecks().containsKey(newName)) {
                currentStudent.getDecks().remove(oldName);
                deck.setName(newName);
                currentStudent.getDecks().put(newName, deck);
                return true;
            }
        }
        return false;
    }

    public boolean removeStudent(String name) {
        if (name == null || !students.containsKey(name)) {
            return false;
        }
        students.remove(name);
        if (currentStudent != null && currentStudent.getName().equals(name)) {
            currentStudent = null;
        }
        return true;
    }
}