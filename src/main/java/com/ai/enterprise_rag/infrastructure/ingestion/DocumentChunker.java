package com.ai.enterprise_rag.infrastructure.ingestion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class DocumentChunker {

    private static final Logger logger =
            LoggerFactory.getLogger(DocumentChunker.class);

    /*
     * Detects section headings such as:
     *
     * 1. INTRODUCTION
     * 2. CODE OF CONDUCT
     * 3. EMPLOYMENT POLICIES
     * 4. WORKING HOURS AND ATTENDANCE
     *
     * Groups:
     *   group(1) = section number
     *   group(2) = section title
     *
     * (?m) = multiline mode, so ^ and $ work for each line.
     */
    private static final Pattern SECTION_PATTERN =
            Pattern.compile(
                    "(?m)^\\s*(\\d+)\\.\\s+([A-Z][A-Z /&-]+)\\s*$"
            );

    /*
     * TokenTextSplitter is responsible for breaking a section
     * into smaller chunks that can be embedded and searched.
     *
     * 200 = maximum chunk size used in this learning project.
     * 400 = safety limit on the total number of chunks.
     *
     * IMPORTANT:
     * We split into sections FIRST and chunks SECOND.
     * This allows every chunk to keep the correct section metadata.
     */
    private final TextSplitter splitter;

    public DocumentChunker() {

        this.splitter = TokenTextSplitter.builder()
                .withChunkSize(200)
                .withMaxNumChunks(400)
                .build();
    }

    /*
     * Main chunking flow:
     *
     * Documents
     *     ↓
     * Split into logical sections
     *     ↓
     * Add section metadata
     *     ↓
     * Split each section into smaller token chunks
     *     ↓
     * Return final chunks
     *
     * Example:
     *
     * PDF
     *  ↓
     * Section 4 - WORKING HOURS
     *  ↓
     * Chunk A → section=4
     * Chunk B → section=4
     */
    public List<Document> chunk(List<Document> documents) {

        // STEP 1:
        // Convert the large document into separate section-level Documents.
        List<Document> sectionDocuments =
                splitIntoSections(documents);

        // Final list containing all token-level chunks.
        List<Document> chunks = new ArrayList<>();

        // STEP 2:
        // Split each section independently.
        //
        // This is important because the section metadata
        // already belongs to that particular section.
        for (Document sectionDocument : sectionDocuments) {

            List<Document> sectionChunks =
                    splitter.split(List.of(sectionDocument));

            chunks.addAll(sectionChunks);
        }

        /*
         * STEP 3:
         * Log the metadata of every final chunk.
         *
         * This is especially useful while learning/debugging
         * because we want to verify that:
         *
         * §4 → WORKING HOURS
         * §5 → LEAVE POLICY
         * etc.
         *
         * are correctly attached to the chunks.
         */
        for (Document chunk : chunks) {

            logger.info(
                    "CHUNK | id={} | section={} | title={}",
                    chunk.getId(),
                    chunk.getMetadata().get("section"),
                    chunk.getMetadata().get("sectionTitle")
            );
        }

        logger.info(
                "Chunking completed: {} documents → {} sections → {} chunks",
                documents.size(),
                sectionDocuments.size(),
                chunks.size()
        );

        return chunks;
    }

    /*
     * Converts a large document into section-level Documents.
     *
     * Why do this?
     *
     * Tika may return the entire PDF as ONE Document.
     *
     * If we directly run TokenTextSplitter on that document,
     * all chunks can inherit the same metadata.
     *
     * Instead:
     *
     * Large Document
     *       ↓
     * Find section headings
     *       ↓
     * Extract each section
     *       ↓
     * Add section metadata
     *       ↓
     * Return section Documents
     */
    private List<Document> splitIntoSections(
            List<Document> documents) {

        List<Document> sections = new ArrayList<>();

        /*
         * Tika normally gives us one or more Documents.
         * We process each Document independently.
         */
        for (Document document : documents) {

            String text = document.getText();

            // Ignore empty documents.
            if (text == null || text.isBlank()) {
                continue;
            }

            /*
             * Find ALL section headings in the document.
             *
             * Example:
             *
             * 1. INTRODUCTION
             * ...
             * 2. CODE OF CONDUCT
             * ...
             * 3. EMPLOYMENT POLICIES
             *
             * while() is important because we need every section,
             * not just the first one.
             */
            Matcher matcher =
                    SECTION_PATTERN.matcher(text);

            List<SectionRange> sectionRanges =
                    new ArrayList<>();

            /*
             * Store the location of every section heading.
             *
             * Example:
             *
             * Section 1 → starts at character 0
             * Section 2 → starts at character 500
             * Section 3 → starts at character 900
             *
             * Later we use these positions to determine
             * where each section starts and ends.
             */
            while (matcher.find()) {

                // Extract section number.
                int sectionNumber =
                        Integer.parseInt(matcher.group(1));

                // Extract section title.
                String sectionTitle =
                        matcher.group(2).trim();

                // Character position where this section starts.
                int start =
                        matcher.start();

                sectionRanges.add(
                        new SectionRange(
                                sectionNumber,
                                sectionTitle,
                                start
                        )
                );
            }

            /*
             * Now create one Document for each section.
             *
             * For example:
             *
             * Section 4:
             *
             * start = position of "4. WORKING HOURS..."
             *
             * end = position where Section 5 starts
             *
             * Therefore:
             *
             * text.substring(start, end)
             *
             * gives us ONLY Section 4.
             */
            for (int i = 0; i < sectionRanges.size(); i++) {

                SectionRange current =
                        sectionRanges.get(i);

                // Beginning of the current section.
                int start = current.start();

                /*
                 * End of the current section:
                 *
                 * If another section exists,
                 * end at the next section's starting position.
                 *
                 * If this is the last section,
                 * end at the end of the entire document.
                 */
                int end =
                        i + 1 < sectionRanges.size()
                                ? sectionRanges.get(i + 1).start()
                                : text.length();

                // Extract only the current section's text.
                String sectionText =
                        text.substring(start, end).trim();

                /*
                 * Copy the original document metadata.
                 *
                 * This preserves metadata added earlier by
                 * DocumentMetadataEnricher, such as:
                 *
                 * documentType
                 * category
                 * department
                 * country
                 * version
                 * accessLevel
                 * tenantId
                 */
                Map<String, Object> metadata =
                        new HashMap<>(
                                document.getMetadata()
                        );

                /*
                 * Add SECTION-SPECIFIC metadata.
                 *
                 * This is the important fix.
                 *
                 * The metadata belongs to THIS section,
                 * so when TokenTextSplitter later creates
                 * chunks from this section, those chunks
                 * inherit the correct section metadata.
                 */
                metadata.put(
                        "section",
                        current.sectionNumber()
                );

                metadata.put(
                        "sectionTitle",
                        current.sectionTitle()
                );

                /*
                 * Create a new Spring AI Document representing
                 * one complete logical section.
                 */
                Document sectionDocument =
                        new Document(
                                sectionText,
                                metadata
                        );

                sections.add(sectionDocument);

                /*
                 * Helpful ingestion log.
                 *
                 * Example:
                 *
                 * SECTION | section=4 |
                 * title=WORKING HOURS AND ATTENDANCE |
                 * chars=420
                 */
                logger.info(
                        "SECTION | section={} | title={} | chars={}",
                        current.sectionNumber(),
                        current.sectionTitle(),
                        sectionText.length()
                );
            }
        }

        return sections;
    }

    /*
     * Small internal data structure used while detecting sections.
     *
     * We temporarily remember:
     *
     * section number
     * section title
     * character position where section starts
     *
     * Example:
     *
     * SectionRange(
     *     4,
     *     "WORKING HOURS AND ATTENDANCE",
     *     1250
     * )
     */
    private record SectionRange(
            int sectionNumber,
            String sectionTitle,
            int start
    ) {
    }
}