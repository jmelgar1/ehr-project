package com.ehr.gateway.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;

class CorsConfigTest {

    private CorsConfig corsConfig;
    private UrlBasedCorsConfigurationSource source;

    @BeforeEach
    void setUp() {
        corsConfig = new CorsConfig();
        source = corsConfig.corsConfigurationSource();
    }

    @Test
    void givenCorsConfig_whenGetCorsWebFilter_thenReturnsConfiguredFilter() {
        CorsWebFilter filter = corsConfig.corsWebFilter(source);

        assertThat(filter).isNotNull();
    }

    @Test
    void givenCorsConfig_whenCreated_thenAllowsLocalhost3000() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        CorsConfiguration config = source.getCorsConfiguration(exchange);

        assertThat(config).isNotNull();
        assertThat(config.getAllowedOrigins()).containsExactly("http://localhost:3000");
    }

    @Test
    void givenCorsConfig_whenCreated_thenAllowsRequiredMethods() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        CorsConfiguration config = source.getCorsConfiguration(exchange);

        assertThat(config).isNotNull();
        assertThat(config.getAllowedMethods()).containsExactlyInAnyOrder("GET", "POST", "PUT", "DELETE", "OPTIONS");
    }

    @Test
    void givenCorsConfig_whenCreated_thenAllowsRequiredHeaders() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        CorsConfiguration config = source.getCorsConfiguration(exchange);

        assertThat(config).isNotNull();
        assertThat(config.getAllowedHeaders()).containsExactlyInAnyOrder("Authorization", "Content-Type");
    }

    @Test
    void givenCorsConfig_whenCreated_thenAllowsCredentials() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        CorsConfiguration config = source.getCorsConfiguration(exchange);

        assertThat(config).isNotNull();
        assertThat(config.getAllowCredentials()).isTrue();
    }

    @Test
    void givenCorsConfig_whenRequestToAnyPath_thenReturnsCorsConfiguration() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/any/path/here").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        CorsConfiguration config = source.getCorsConfiguration(exchange);

        assertThat(config).isNotNull();
    }
}
