package application.adapters.in.rest.requests;

/**
 * DTO para ajustar stock: {@code deltaQuantity} positivo incrementa, negativo decrementa.
 * {@code reason} describe el motivo del ajuste (conteo físico, merma, etc.).
 */
public record AdjustInventoryRequest(String productId, String warehouseId,
                                     int deltaQuantity, String reason) {
}
