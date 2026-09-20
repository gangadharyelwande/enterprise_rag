package com.mcp.mcp_tool_server.exception;

public class ToolExecutionException extends RuntimeException {

    private final ToolErrorCode errorCode;

    public ToolExecutionException(
            ToolErrorCode errorCode,
            String message) {

        super(message);
        this.errorCode = errorCode;
    }

    public ToolExecutionException(
            ToolErrorCode errorCode,
            String message,
            Throwable cause) {

        super(message, cause);
        this.errorCode = errorCode;
    }

    public ToolErrorCode getErrorCode() {
        return errorCode;
    }
}
