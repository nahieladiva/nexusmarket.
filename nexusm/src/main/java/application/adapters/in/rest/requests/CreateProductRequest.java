package application.adapters.in.rest.requests;

/**
 * DTO de creación de producto. El vendedor se toma de la cabecera
 * {@code X-User-Id}. {@code type} debe ser PHYSICAL o DIGITAL.
 */
public record CreateProductRequest(String code, String name, String description,
                                   PriceRequest price, String type) {
}
