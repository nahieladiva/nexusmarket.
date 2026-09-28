package application.adapters.in.rest.requests;

/** DTO para preparar el envío de un pedido pagado. */
public record CreateShipmentRequest(String orderId) {
}
