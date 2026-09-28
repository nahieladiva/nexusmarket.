package application.adapters.in.rest.requests;

/** DTO de entrada de mercancía a una bodega. */
public record ReceiveStockRequest(String productId, String warehouseId, int quantity) {
}
