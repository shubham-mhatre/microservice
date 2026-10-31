package com.sm.api_gateway.filter;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;

@Component
public class AuthorizationFilter
        extends AbstractGatewayFilterFactory<AuthorizationFilter.Config> {

    private static final String ADMIN_ROLE = "ROLE_ADMIN";

    private static final Set<HttpMethod> WRITE_METHODS = Set.of(
            HttpMethod.POST,
            HttpMethod.PUT,
            HttpMethod.PATCH,
            HttpMethod.DELETE
    );

    private final ObjectMapper objectMapper;

    public AuthorizationFilter(ObjectMapper objectMapper) {
        super(Config.class);
        this.objectMapper = objectMapper;
    }

    @Override
    public GatewayFilter apply(Config config) {

        return (exchange, chain) -> {

            String path = exchange.getRequest().getURI().getPath();

            HttpMethod method = exchange.getRequest().getMethod();

            String rolesHeader = exchange.getRequest()
                    .getHeaders()
                    .getFirst(AuthenticationFilter.ROLES_HEADER);

            if (rolesHeader == null || rolesHeader.isBlank()) {
                return writeForbidden(exchange, "User roles are missing");
            }

            Set<String> roles = Set.of(rolesHeader.split(","));

            boolean isAdmin = roles.contains(ADMIN_ROLE);

            if (requiresAdmin(path, method) && !isAdmin) {
                return writeForbidden(exchange,
                        "You do not have permission to access this resource");
            }

            if (isInternalInventoryOperation(path, method)) {
                return writeForbidden(exchange,
                        "This operation is restricted to internal service communication");
            }

            return chain.filter(exchange);
        };
    }

    private boolean requiresAdmin(String path, HttpMethod method) {

        if (method == null) {
            return false;
        }

        // PRODUCT SERVICE

        // Create product
        if (path.equals("/product/api")
                && HttpMethod.POST.equals(method)) {
            return true;
        }

        // Update and delete product
        if (path.matches("/product/api/[^/]+")
                && (HttpMethod.PUT.equals(method)
                || HttpMethod.DELETE.equals(method))) {
            return true;
        }

        // INVENTORY SERVICE

        // Create inventory and list all inventory
        if (path.equals("/inventory/api")
                && (HttpMethod.POST.equals(method)
                || HttpMethod.GET.equals(method))) {
            return true;
        }

        // Inventory management operations
        if (path.matches("/inventory/api/sku/[^/]+/(deactivate|quantity|adjust|activate)")
                && WRITE_METHODS.contains(method)) {
            return true;
        }

        // ORDER SERVICE

        // Retrieve all orders
        if (path.equals("/order/api")
                && HttpMethod.GET.equals(method)) {
            return true;
        }

        // Update order status
        if (path.matches("/order/api/[^/]+/status")
                && HttpMethod.PATCH.equals(method)) {
            return true;
        }

        // PAYMENT SERVICE

        // Process payment
        if (path.matches("/payment/api/[^/]+")
                && HttpMethod.PATCH.equals(method)) {
            return true;
        }

        // Retrieve all payments
        if (path.equals("/payment/api")
                && HttpMethod.GET.equals(method)) {
            return true;
        }

        // Retry payment/order synchronization
        if (path.matches("/payment/api/[^/]+/sync-order")
                && HttpMethod.POST.equals(method)) {
            return true;
        }

        return false;
    }

    private boolean isInternalInventoryOperation(
            String path, HttpMethod method) {

        return path.matches("/inventory/api/sku/[^/]+/(reserve|release)")
                && HttpMethod.POST.equals(method);
    }

    private Mono<Void> writeForbidden(
            ServerWebExchange exchange,
            String message) {

        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);

        exchange.getResponse().getHeaders()
                .setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("time", LocalDateTime.now());
        response.put("message", message);
        response.put("status", 403);
        response.put("error", "Forbidden");
        response.put("path", exchange.getRequest().getURI().getPath());
        response.put("success", false);

        try {

            byte[] bytes = objectMapper.writeValueAsBytes(response);

            return exchange.getResponse().writeWith(
                    Mono.just(exchange.getResponse()
                            .bufferFactory()
                            .wrap(bytes)));

        } catch (Exception ex) {
            return Mono.error(ex);
        }
    }

    public static class Config {
    }
}