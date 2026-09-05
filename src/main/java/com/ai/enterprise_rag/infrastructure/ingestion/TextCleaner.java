package com.ai.enterprise_rag.infrastructure.ingestion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TextCleaner {

    private static final Logger log = LoggerFactory.getLogger(TextCleaner.class);

    public String clean(String text) {

        if (text == null || text.isBlank()) {
            log.warn("Received null or blank text. Nothing to clean.");
            return "";
        }

        log.info("Starting text cleaning. Original length: {}", text.length());

        String cleanedText = text
                // Replace tabs with a single space
                .replace("\t", " ")

                // Remove leading/trailing spaces from each line
                .lines()
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .reduce((line1, line2) -> line1 + " " + line2)
                .orElse("")

                // Replace multiple spaces with one space
                .replaceAll("\\s+", " ")

                // Remove unnecessary spaces before punctuation
                .replaceAll("\\s+([,.!?;:])", "$1")

                .trim();

        log.debug("Text cleaning completed. Cleaned length: {}", cleanedText.length());

        if (!text.equals(cleanedText)) {
            log.info("Text was cleaned. Original length: {}, Cleaned length: {}",
                    text.length(),
                    cleanedText.length());
        } else {
            log.debug("No cleaning changes were required.");
        }

        return cleanedText;
    }
}