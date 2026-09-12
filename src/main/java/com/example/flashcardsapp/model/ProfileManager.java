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

    // Установка текущего ученика по имени
    public void setCurrentStudent(String name) {
        if (students.containsKey(name)) {
            this.currentStudent = students.get(name);
        }
    }

    public Deck getCurrentDeck() { return currentDeck; }

    // Установка текущей колоды
    public void setCurrentDeck(Deck deck) {
        this.currentDeck = deck;
    }

    // Добавление ученика
    public boolean addStudent(String name) {
        if (students.containsKey(name)) return false;
        Student student = new Student(name);
        students.put(name, student);
        currentStudent = student;
        return true;
    }

    // Добавление колоды текущему ученику
    public boolean addDeckToCurrentStudent(String deckName) {
        if (currentStudent == null) return false;
        if (currentStudent.getDecks().containsKey(deckName)) return false;

        Deck deck = new Deck(deckName);
        currentStudent.getDecks().put(deckName, deck);
        currentDeck = deck;
        return true;
    }

    // Переименование ученика
    public boolean renameStudent(String oldName, String newName) {
        if (students.containsKey(oldName) && !students.containsKey(newName)) {
            Student student = students.remove(oldName);
            student.setName(newName);
            students.put(newName, student);
            if (currentStudent == student) {
                currentStudent = student;
            }
            return true;
        }
        return false;
    }

    // Переименование текущей колоды
    public boolean renameCurrentDeck(String newName) {
        if (currentStudent != null && currentDeck != null) {
            String oldName = currentDeck.getName();
            if (currentStudent.getDecks().containsKey(oldName) && !currentStudent.getDecks().containsKey(newName)) {
                Deck deck = currentStudent.getDecks().remove(oldName);
                deck.setName(newName);
                currentStudent.getDecks().put(newName, deck);
                currentDeck = deck;
                return true;
            }
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

    // Удаление ученика
    public void removeStudent(String name) {
        students.remove(name);
        if (currentStudent != null && currentStudent.getName().equals(name)) {
            currentStudent = students.isEmpty() ? null : students.values().iterator().next();
        }
    }
}