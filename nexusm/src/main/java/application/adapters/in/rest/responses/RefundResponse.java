package application.adapters.in.rest.responses;

import java.time.LocalDateTime;

public record RefundResponse(String id, String returnId, String orderId, String amount,
                             String currency, String status, LocalDateTime createdAt,
                             LocalDateTime completedAt) {
}
