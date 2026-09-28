package application.adapters.in.rest.mappers;

import application.adapters.in.rest.responses.OrderItemResponse;
import application.adapters.in.rest.responses.OrderResponse;
import application.domain.models.Order;
import application.domain.models.OrderItem;

import java.util.List;

import org.springframework.stereotype.Component;

/**
 * Mapea entre DTOs REST y el modelo de dominio de órdenes.
 */
@Component
public class OrderMapper {

    public OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
            .map(this::toItemResponse)
            .toList();
        return new OrderResponse(
            order.getId().toString(),
            order.getBuyerId().toString(),
            order.getStatus().name(),
            order.getTotal().getAmount().toPlainString(),
            order.getTotal().getCurrency().getCurrencyCode(),
            order.getCreatedAt(),
            order.getUpdatedAt(),
            items);
    }

    public OrderItemResponse toItemResponse(OrderItem item) {
        return new OrderItemResponse(
            item.getProductId().toString(),
            item.getQuantity().getValue(),
            item.getUnitPrice().getAmount().toPlainString(),
            item.getUnitPrice().getCurrency().getCurrencyCode(),
            item.subtotal().getAmount().toPlainString(),
            item.hasWarehouse() ? item.getWarehouseId().toString() : null);
    }
}