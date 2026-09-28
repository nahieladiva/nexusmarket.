package application.adapters.in.rest.responses;

import java.time.LocalDateTime;

public record ProductResponse(String id, String code, String name, String description,
                              String priceAmount, String priceCurrency, String sellerId,
                              String type, String status, LocalDateTime createdAt) {
}
