package application.adapters.in.rest.requests;

/** DTO de solicitud de devolución de un producto de un pedido entregado. */
public record CreateReturnRequest(String orderId, String productId, int quantity, String reason) {
}
