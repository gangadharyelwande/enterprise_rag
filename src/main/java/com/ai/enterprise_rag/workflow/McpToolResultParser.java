package com.ai.enterprise_rag.workflow;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class McpToolResultParser {

    private static final ObjectMapper OBJECT_MAPPER =
            new ObjectMapper();

    public static String extractText(String toolResult) {

        try {
            JsonNode root = OBJECT_MAPPER.readTree(toolResult);

            if (root.isArray() && !root.isEmpty()) {

                JsonNode textNode = root.get(0).get("text");

                if (textNode != null) {
                    return textNode.asText();
                }
            }

            return toolResult;

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Unable to parse MCP tool result",
                    exception
            );
        }
    }
}