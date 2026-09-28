package application.adapters.in.rest.requests;

/** DTO para agregar un producto al carrito. */
public record CartItemRequest(String productId, int quantity) {
}
