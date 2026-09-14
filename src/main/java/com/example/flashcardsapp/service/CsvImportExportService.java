package com.example.flashcardsapp.service;

import com.example.flashcardsapp.model.Deck;
import com.example.flashcardsapp.model.Flashcard;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class CsvImportExportService {

    public static Deck importCsv(File file) throws IOException {
        String deckName = file.getName().replace(".csv", "");
        Deck deck = new Deck(deckName);

        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.contains(";") ? line.split(";") : line.split(",");
                if (parts.length >= 2) {
                    deck.addCard(new Flashcard(parts[0].trim(), parts[1].trim()));
                }
            }
        }
        return deck;
    }
}