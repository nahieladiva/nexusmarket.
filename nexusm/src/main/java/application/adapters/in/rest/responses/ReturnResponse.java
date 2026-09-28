package application.adapters.in.rest.responses;

import java.time.LocalDateTime;

public record ReturnResponse(String id, String orderId, String buyerId, String productId,
                             int quantity, String reason, String status,
                             LocalDateTime requestedAt, LocalDateTime resolvedAt) {
}
