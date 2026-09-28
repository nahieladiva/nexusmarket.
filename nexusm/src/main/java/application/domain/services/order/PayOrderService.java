package application.domain.services.order;

import application.domain.enums.OperationType;
import application.domain.models.Invoice;
import application.domain.models.Order;
import application.domain.models.User;
import application.domain.ports.out.InvoiceRepository;
import application.domain.ports.out.OrderRepository;
import application.domain.services.DomainService;
import application.domain.services.audit.RegisterAuditEventService;
import application.domain.services.authorization.AuthorizeBuyerOperationService;
import application.domain.services.authorization.ValidateOrderOwnershipService;
import application.domain.services.invoice.ConsultInvoiceService;
import application.domain.valueobjects.OrderId;

import java.util.Map;

/**
 * El comprador dueño paga su pedido: el pedido pasa a {@code PAID}
 * y la factura a {@code PAID}.
 */
@DomainService
public class PayOrderService {

    private final OrderRepository orderRepository;
    private final InvoiceRepository invoiceRepository;
    private final ConsultOrderService consultOrderService;
    private final ConsultInvoiceService consultInvoiceService;
    private final AuthorizeBuyerOperationService authorizeBuyerOperationService;
    private final ValidateOrderOwnershipService validateOrderOwnershipService;
    private final RegisterAuditEventService registerAuditEventService;

    public PayOrderService(OrderRepository orderRepository, InvoiceRepository invoiceRepository,
                           ConsultOrderService consultOrderService,
                           ConsultInvoiceService consultInvoiceService,
                           AuthorizeBuyerOperationService authorizeBuyerOperationService,
                           ValidateOrderOwnershipService validateOrderOwnershipService,
                           RegisterAuditEventService registerAuditEventService) {
        this.orderRepository = orderRepository;
        this.invoiceRepository = invoiceRepository;
        this.consultOrderService = consultOrderService;
        this.consultInvoiceService = consultInvoiceService;
        this.authorizeBuyerOperationService = authorizeBuyerOperationService;
        this.validateOrderOwnershipService = validateOrderOwnershipService;
        this.registerAuditEventService = registerAuditEventService;
    }

    public Order execute(User buyer, OrderId orderId) {
        authorizeBuyerOperationService.execute(buyer);
        Order order = consultOrderService.require(orderId);
        validateOrderOwnershipService.execute(buyer, order);
        order.markAsPaid();
        Invoice invoice = consultInvoiceService.require(orderId);
        invoice.markAsPaid();
        orderRepository.save(order);
        invoiceRepository.save(invoice);
        registerAuditEventService.execute(OperationType.ORDER_PAID, buyer, orderId,
            Map.of("invoiceNumber", invoice.getInvoiceNumber(),
                "total", order.getTotal().getAmount().toPlainString()));
        return order;
    }
}
