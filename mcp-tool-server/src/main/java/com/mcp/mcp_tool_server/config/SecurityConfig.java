package com.mcp.mcp_tool_server.config;

import com.mcp.mcp_tool_server.security.ApiKeyAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

@Configuration
public class SecurityConfig {

    private final ApiKeyAuthenticationFilter apiKeyAuthenticationFilter;

    public SecurityConfig(
            ApiKeyAuthenticationFilter apiKeyAuthenticationFilter) {

        this.apiKeyAuthenticationFilter =
                apiKeyAuthenticationFilter;
    }

    @Bean
    SecurityContextRepository securityContextRepository() {
        return new RequestAttributeSecurityContextRepository();
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            SecurityContextRepository securityContextRepository)
            throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)

                .securityContext(securityContext -> securityContext
                        .securityContextRepository(
                                securityContextRepository)
                )

                .authorizeHttpRequests(authorize -> authorize

                        .requestMatchers("/error")
                        .permitAll()

                        .requestMatchers("/mcp/**")
                        .hasAuthority("MCP_TOOL_ACCESS")

                        .anyRequest()
                        .authenticated()
                )

                .addFilterBefore(
                        apiKeyAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}