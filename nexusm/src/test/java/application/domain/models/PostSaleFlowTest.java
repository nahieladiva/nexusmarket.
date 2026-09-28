package application.domain.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import application.domain.enums.InvoiceStatus;
import application.domain.enums.RefundStatus;
import application.domain.enums.ReturnStatus;
import application.domain.enums.ShipmentStatus;
import application.domain.enums.UserRole;
import application.domain.exceptions.InvalidStatusTransitionException;
import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;
import application.domain.valueobjects.UserId;
import application.domain.valueobjects.WarehouseId;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Flujo completo: factura, envío, devolución y reembolso.
 */
class PostSaleFlowTest {

    private final ProductId productId = ProductId.random();

    private Order pendingPaymentOrder() {
        Order order = Order.create(UserId.random(),
            List.of(new OrderItem(productId, Quantity.of(2), Money.of("50.00", "USD"))));
        order.checkout();
        return order;
    }

    @Test
    void invoiceIsIssuedForPendingPaymentOrderAndThenPaid() {
        Order order = pendingPaymentOrder();
        Invoice invoice = Invoice.issueFor(order, "FAC-0001");
        assertEquals(InvoiceStatus.ISSUED, invoice.getStatus());
        assertEquals(order.getTotal(), invoice.getTotal());
        invoice.markAsPaid();
        assertThrows(InvalidStatusTransitionException.class, invoice::voidInvoice);
    }

    @Test
    void onlyLogisticOperatorsDispatchShipmentsOfPaidOrders() {
        Order order = pendingPaymentOrder();
        assertThrows(IllegalStateException.class,
            () -> Shipment.prepareFor(order, WarehouseId.random(), TestData.address()));
        order.markAsPaid();
        Shipment shipment = Shipment.prepareFor(order, WarehouseId.random(), TestData.address());
        User admin = TestData.staff("1017000010", "adm@test.com", UserRole.ADMIN);
        assertThrows(UnauthorizedOperationException.class, () -> shipment.dispatch(admin, "GUIA-1"));
        User operator = TestData.staff("1017000011", "op@test.com", UserRole.LOGISTIC_OPERATOR);
        shipment.dispatch(operator, "GUIA-1");
        assertEquals(ShipmentStatus.IN_TRANSIT, shipment.getStatus());
        shipment.confirmDelivery();
        assertEquals(ShipmentStatus.DELIVERED, shipment.getStatus());
    }

    @Test
    void returnAndRefundOnlyAfterDelivery() {
        Order order = pendingPaymentOrder();
        assertThrows(IllegalStateException.class,
            () -> ReturnRequest.request(order, productId, Quantity.of(1), "Defectuoso"));
        order.markAsPaid();
        order.ship();
        order.deliver();
        assertThrows(IllegalArgumentException.class,
            () -> ReturnRequest.request(order, productId, Quantity.of(3), "Defectuoso"));

        ReturnRequest returnRequest =
            ReturnRequest.request(order, productId, Quantity.of(1), "Defectuoso");
        assertThrows(IllegalStateException.class,
            () -> Refund.createFor(returnRequest, Money.of("50.00", "USD")));
        returnRequest.approve();
        returnRequest.markAsReceived();
        assertEquals(ReturnStatus.RECEIVED, returnRequest.getStatus());

        Refund refund = Refund.createFor(returnRequest, Money.of("50.00", "USD"));
        refund.complete();
        assertEquals(RefundStatus.COMPLETED, refund.getStatus());
    }
}
