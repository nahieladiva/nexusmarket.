package application.adapters.in.rest.requests;

/** DTO para despachar un envío con su número de guía. */
public record DispatchShipmentRequest(String trackingNumber) {
}
