package com.mcp.mcp_tool_server.exception;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ToolExceptionHandler {

    private static final Logger logger =
            LoggerFactory.getLogger(ToolExceptionHandler.class);

    public String handle(
            String toolName,
            String user,
            Exception exception) {

        if (exception instanceof InvalidToolArgumentException) {

            logger.warn(
                    "Tool validation failed. tool={}, user={}, error={}",
                    toolName,
                    user,
                    exception.getMessage()
            );

            return "Invalid input provided for the tool.";
        }

        if (exception instanceof ToolExecutionException toolException) {

            logger.error(
                    "Tool execution failed. tool={}, user={}, errorCode={}",
                    toolName,
                    user,
                    toolException.getErrorCode(),
                    exception
            );

            return "The requested operation could not be completed.";
        }

        logger.error(
                "Unexpected tool error. tool={}, user={}",
                toolName,
                user,
                exception
        );

        return "The tool is temporarily unavailable.";
    }
}
