package com.ai.enterprise_rag.infrastructure.ingestion;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TextCleanerTest {

    private static final Logger log =
            LoggerFactory.getLogger(TextCleanerTest.class);

    private final TextCleaner textCleaner = new TextCleaner();

    @Test
    @DisplayName("Should remove leading and trailing spaces")
    void shouldRemoveLeadingAndTrailingSpaces() {

        log.info("▶ START: shouldRemoveLeadingAndTrailingSpaces");

        String input = "   Java is used for enterprise applications.   ";

        String result = textCleaner.clean(input);

        assertEquals(
                "Java is used for enterprise applications.",
                result
        );

        log.info("✓ PASS: Leading/trailing spaces removed");
        log.info("◀ END: shouldRemoveLeadingAndTrailingSpaces");
    }

    @Test
    @DisplayName("Should replace multiple spaces with a single space")
    void shouldReplaceMultipleSpacesWithSingleSpace() {

        log.info("▶ START: shouldReplaceMultipleSpacesWithSingleSpace");

        String input =
                "Java   is    used     for enterprise applications.";

        String result = textCleaner.clean(input);

        assertEquals(
                "Java is used for enterprise applications.",
                result
        );

        log.info("✓ PASS: Multiple spaces replaced");
        log.info("◀ END: shouldReplaceMultipleSpacesWithSingleSpace");
    }

    @Test
    @DisplayName("Should replace tabs with spaces")
    void shouldReplaceTabsWithSpaces() {

        log.info("▶ START: shouldReplaceTabsWithSpaces");

        String input =
                "Java\tis\tused\tfor enterprise applications.";

        String result = textCleaner.clean(input);

        assertEquals(
                "Java is used for enterprise applications.",
                result
        );

        log.info("✓ PASS: Tabs replaced with spaces");
        log.info("◀ END: shouldReplaceTabsWithSpaces");
    }

    @Test
    @DisplayName("Should remove blank lines")
    void shouldRemoveBlankLines() {

        log.info("▶ START: shouldRemoveBlankLines");

        String input = """
                Java is used for enterprise applications.

                
                Java is scalable.
                """;

        String result = textCleaner.clean(input);

        assertEquals(
                "Java is used for enterprise applications. Java is scalable.",
                result
        );

        log.info("✓ PASS: Blank lines removed");
        log.info("◀ END: shouldRemoveBlankLines");
    }

    @Test
    @DisplayName("Should remove unnecessary spaces before punctuation")
    void shouldRemoveSpacesBeforePunctuation() {

        log.info("▶ START: shouldRemoveSpacesBeforePunctuation");

        String input =
                "Java is powerful , scalable , and widely used .";

        String result = textCleaner.clean(input);

        assertEquals(
                "Java is powerful, scalable, and widely used.",
                result
        );

        log.info("✓ PASS: Spaces before punctuation removed");
        log.info("◀ END: shouldRemoveSpacesBeforePunctuation");
    }

    @Test
    @DisplayName("Should return empty string when input is null")
    void shouldReturnEmptyStringForNull() {

        log.info("▶ START: shouldReturnEmptyStringForNull");

        String result = textCleaner.clean(null);

        assertEquals("", result);

        log.info("✓ PASS: Null handled correctly");
        log.info("◀ END: shouldReturnEmptyStringForNull");
    }

    @Test
    @DisplayName("Should return empty string when input is blank")
    void shouldReturnEmptyStringForBlankText() {

        log.info("▶ START: shouldReturnEmptyStringForBlankText");

        String result = textCleaner.clean("   ");

        assertEquals("", result);

        log.info("✓ PASS: Blank text handled correctly");
        log.info("◀ END: shouldReturnEmptyStringForBlankText");
    }
}