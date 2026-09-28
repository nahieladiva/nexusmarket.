package application.adapters.in.rest.responses;

import java.time.LocalDateTime;

public record InvoiceResponse(String id, String invoiceNumber, String orderId, String buyerId,
                              String totalAmount, String totalCurrency, String status,
                              LocalDateTime issuedAt, LocalDateTime paidAt) {
}
