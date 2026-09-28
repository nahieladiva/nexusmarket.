package application.adapters.in.rest.mappers;

import application.adapters.in.rest.responses.InvoiceResponse;
import application.adapters.in.rest.responses.RefundResponse;
import application.adapters.in.rest.responses.ReturnResponse;
import application.adapters.in.rest.responses.ShipmentResponse;
import application.domain.models.Invoice;
import application.domain.models.Refund;
import application.domain.models.ReturnRequest;
import application.domain.models.Shipment;

import org.springframework.stereotype.Component;

/**
 * Traduce facturas, envíos, devoluciones y reembolsos a sus DTOs de respuesta.
 */
@Component
public class PostSaleMapper {

    private final UserMapper userMapper;

    public PostSaleMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public InvoiceResponse toResponse(Invoice invoice) {
        return new InvoiceResponse(
            invoice.getId().toString(),
            invoice.getInvoiceNumber(),
            invoice.getOrderId().toString(),
            invoice.getBuyerId().toString(),
            invoice.getTotal().getAmount().toPlainString(),
            invoice.getTotal().getCurrency().getCurrencyCode(),
            invoice.getStatus().name(),
            invoice.getIssuedAt(),
            invoice.getPaidAt());
    }

    public ShipmentResponse toResponse(Shipment shipment) {
        return new ShipmentResponse(
            shipment.getId().toString(),
            shipment.getOrderId().toString(),
            shipment.getWarehouseId().toString(),
            userMapper.toResponse(shipment.getShippingAddress()),
            shipment.getLogisticOperatorId() == null ? null : shipment.getLogisticOperatorId().toString(),
            shipment.getTrackingNumber(),
            shipment.getStatus().name(),
            shipment.getCreatedAt(),
            shipment.getShippedAt(),
            shipment.getDeliveredAt());
    }

    public ReturnResponse toResponse(ReturnRequest returnRequest) {
        return new ReturnResponse(
            returnRequest.getId().toString(),
            returnRequest.getOrderId().toString(),
            returnRequest.getBuyerId().toString(),
            returnRequest.getProductId().toString(),
            returnRequest.getQuantity().getValue(),
            returnRequest.getReason(),
            returnRequest.getStatus().name(),
            returnRequest.getRequestedAt(),
            returnRequest.getResolvedAt());
    }

    public RefundResponse toResponse(Refund refund) {
        return new RefundResponse(
            refund.getId().toString(),
            refund.getReturnId().toString(),
            refund.getOrderId().toString(),
            refund.getAmount().getAmount().toPlainString(),
            refund.getAmount().getCurrency().getCurrencyCode(),
            refund.getStatus().name(),
            refund.getCreatedAt(),
            refund.getCompletedAt());
    }
}
