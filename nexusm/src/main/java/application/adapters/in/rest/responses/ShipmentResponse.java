package application.adapters.in.rest.responses;

import java.time.LocalDateTime;

public record ShipmentResponse(String id, String orderId, String warehouseId,
                               AddressResponse shippingAddress, String logisticOperatorId,
                               String trackingNumber, String status, LocalDateTime createdAt,
                               LocalDateTime shippedAt, LocalDateTime deliveredAt) {
}
