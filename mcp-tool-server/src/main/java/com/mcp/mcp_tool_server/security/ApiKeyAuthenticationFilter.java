package com.mcp.mcp_tool_server.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class ApiKeyAuthenticationFilter
        extends OncePerRequestFilter {

    private static final String API_KEY_HEADER =
            "X-MCP-API-KEY";

    private final String expectedApiKey;

    public ApiKeyAuthenticationFilter(
            @Value("${mcp.security.api-key}")
            String expectedApiKey) {

        this.expectedApiKey = expectedApiKey;
    }

    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request) {

        return "/error".equals(
                request.getRequestURI());
    }

    /**
     * MCP uses asynchronous processing.
     */
    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String apiKey =
                request.getHeader(API_KEY_HEADER);

        System.out.println(
                "========================================");

        System.out.println(
                "API KEY AUTHENTICATION");

        System.out.println(
                "Request URI ==> "
                        + request.getRequestURI());

        System.out.println(
                "Dispatcher Type ==> "
                        + request.getDispatcherType());

        System.out.println(
                "expectedApiKey ==> "
                        + expectedApiKey);

        System.out.println(
                "apiKey ==> "
                        + apiKey);

        /*
         * Validate API key.
         */
        if (apiKey == null ||
                !apiKey.equals(expectedApiKey)) {

            System.out.println(
                    "API KEY VALIDATION ==> FAILED");

            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED);

            response.setContentType(
                    "application/json");

            response.getWriter().write(
                    """
                    {
                      "error": "Unauthorized",
                      "message": "Valid MCP API key is required"
                    }
                    """
            );

            return;
        }

        System.out.println(
                "API KEY VALIDATION ==> SUCCESS");

        /*
         * Create authentication.
         */
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        "mcp-client",
                        null,
                        List.of(
                                new SimpleGrantedAuthority(
                                        "MCP_TOOL_ACCESS"
                                )
                        )
                );

        /*
         * Create SecurityContext.
         */
        SecurityContext securityContext =
                SecurityContextHolder.createEmptyContext();

        securityContext.setAuthentication(
                authentication);

        /*
         * Set SecurityContext.
         */
        SecurityContextHolder.setContext(
                securityContext);

        /*
         * Debug.
         */
        System.out.println(
                "Authentication ==> "
                        + SecurityContextHolder
                        .getContext()
                        .getAuthentication());

        System.out.println(
                "Authorities ==> "
                        + SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getAuthorities());

        System.out.println(
                "Authenticated ==> "
                        + SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .isAuthenticated());

        System.out.println(
                "========================================");

        filterChain.doFilter(
                request,
                response);
    }
}