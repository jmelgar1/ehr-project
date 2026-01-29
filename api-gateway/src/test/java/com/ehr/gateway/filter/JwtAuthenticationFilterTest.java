package com.ehr.gateway.filter;

import com.ehr.gateway.constant.ApiPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import static com.ehr.gateway.utils.JwtTestUtils.TEST_SECRET;
import static com.ehr.gateway.utils.JwtTestUtils.TEST_USER_ID;
import static com.ehr.gateway.utils.JwtTestUtils.expiredToken;
import static com.ehr.gateway.utils.JwtTestUtils.tokenWithWrongSecret;
import static com.ehr.gateway.utils.JwtTestUtils.validToken;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentCaptor;
import org.springframework.web.server.ServerWebExchange;

class JwtAuthenticationFilterTest {

    private JwtAuthenticationFilter filter;
    private GatewayFilterChain chain;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(TEST_SECRET);
        chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());
    }

    @Test
    void givenLoginPath_whenFilter_thenPassesThrough() {
        MockServerHttpRequest request = MockServerHttpRequest
                .post(ApiPaths.AUTH_LOGIN)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        verify(chain).filter(exchange);
        assertThat(exchange.getResponse().getStatusCode()).isNotEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void givenRegisterPath_whenFilter_thenPassesThrough() {
        MockServerHttpRequest request = MockServerHttpRequest
                .post(ApiPaths.AUTH_REGISTER)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        verify(chain).filter(exchange);
        assertThat(exchange.getResponse().getStatusCode()).isNotEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void givenProtectedPathWithoutToken_whenFilter_thenReturnsUnauthorized() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/patients")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        verify(chain, never()).filter(any());
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void givenProtectedPathWithInvalidAuthHeader_whenFilter_thenReturnsUnauthorized() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/patients")
                .header(HttpHeaders.AUTHORIZATION, "InvalidHeader")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        verify(chain, never()).filter(any());
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void givenProtectedPathWithMalformedToken_whenFilter_thenReturnsUnauthorized() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/patients")
                .header(HttpHeaders.AUTHORIZATION, "Bearer not.a.valid-token")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        verify(chain, never()).filter(any());
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void givenProtectedPathWithExpiredToken_whenFilter_thenReturnsUnauthorized() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/patients")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken())
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        verify(chain, never()).filter(any());
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void givenProtectedPathWithWrongSecretToken_whenFilter_thenReturnsUnauthorized() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/patients")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenWithWrongSecret())
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        verify(chain, never()).filter(any());
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void givenProtectedPathWithValidToken_whenFilter_thenPassesThroughWithUserIdHeader() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/patients")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken())
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        ArgumentCaptor<ServerWebExchange> exchangeCaptor = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(chain).filter(exchangeCaptor.capture());

        ServerWebExchange capturedExchange = exchangeCaptor.getValue();
        assertThat(capturedExchange.getRequest().getHeaders().getFirst("X-User-Id"))
                .isEqualTo(TEST_USER_ID.toString());
        assertThat(exchange.getResponse().getStatusCode()).isNotEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void givenLoginPathWithGetMethod_whenFilter_thenReturnsUnauthorized() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get(ApiPaths.AUTH_LOGIN)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        verify(chain, never()).filter(any());
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void givenDeleteUsersPath_whenFilter_thenRequiresAuthentication() {
        MockServerHttpRequest request = MockServerHttpRequest
                .delete("/api/auth/users/123")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        verify(chain, never()).filter(any());
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
