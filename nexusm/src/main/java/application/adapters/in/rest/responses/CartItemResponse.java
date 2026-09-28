package application.adapters.in.rest.responses;

public record CartItemResponse(String productId, int quantity, String unitPriceAmount,
                               String unitPriceCurrency, String subtotalAmount) {
}
