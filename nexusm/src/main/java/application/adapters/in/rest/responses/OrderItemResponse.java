package application.adapters.in.rest.responses;

public record OrderItemResponse(String productId, int quantity,
                                String unitPriceAmount, String unitPriceCurrency,
                                String subtotalAmount, String warehouseId) {
}
