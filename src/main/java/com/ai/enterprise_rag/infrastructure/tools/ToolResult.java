package com.ai.enterprise_rag.infrastructure.tools;

public record ToolResult<T>(
        boolean success,
        T data,
        String errorCode,
        String message
) {

    public static <T> ToolResult<T> success(T data) {
        return new ToolResult<>(
                true,
                data,
                null,
                null
        );
    }

    public static <T> ToolResult<T> error(
            String errorCode,
            String message) {

        return new ToolResult<>(
                false,
                null,
                errorCode,
                message
        );
    }
}