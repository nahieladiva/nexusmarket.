package application.domain.services.order;

import application.domain.enums.OperationType;
import application.domain.models.Order;
import application.domain.models.User;
import application.domain.ports.out.InvoiceRepository;
import application.domain.ports.out.OrderRepository;
import application.domain.services.DomainService;
import application.domain.services.audit.RegisterAuditEventService;
import application.domain.services.authorization.AuthorizeSupervisionOperationService;
import application.domain.services.authorization.ValidateActiveUserService;
import application.domain.services.authorization.ValidateOrderOwnershipService;
import application.domain.services.inventory.ReleaseStockService;
import application.domain.valueobjects.OrderId;

import java.util.Map;

/**
 * Cancela un pedido no pagado ({@code CART} o {@code PENDING_PAYMENT}):
 * libera el stock reservado y anula la factura. Lo puede hacer el comprador
 * dueño o un Administrador/Supervisor.
 */
@DomainService
public class CancelOrderService {

    private final OrderRepository orderRepository;
    private final InvoiceRepository invoiceRepository;
    private final ConsultOrderService consultOrderService;
    private final ValidateActiveUserService validateActiveUserService;
    private final ValidateOrderOwnershipService validateOrderOwnershipService;
    private final AuthorizeSupervisionOperationService authorizeSupervisionOperationService;
    private final ReleaseStockService releaseStockService;
    private final RegisterAuditEventService registerAuditEventService;

    public CancelOrderService(OrderRepository orderRepository, InvoiceRepository invoiceRepository,
                              ConsultOrderService consultOrderService,
                              ValidateActiveUserService validateActiveUserService,
                              ValidateOrderOwnershipService validateOrderOwnershipService,
                              AuthorizeSupervisionOperationService authorizeSupervisionOperationService,
                              ReleaseStockService releaseStockService,
                              RegisterAuditEventService registerAuditEventService) {
        this.orderRepository = orderRepository;
        this.invoiceRepository = invoiceRepository;
        this.consultOrderService = consultOrderService;
        this.validateActiveUserService = validateActiveUserService;
        this.validateOrderOwnershipService = validateOrderOwnershipService;
        this.authorizeSupervisionOperationService = authorizeSupervisionOperationService;
        this.releaseStockService = releaseStockService;
        this.registerAuditEventService = registerAuditEventService;
    }

    public Order execute(User actor, OrderId orderId) {
        validateActiveUserService.execute(actor);
        Order order = consultOrderService.require(orderId);
        if (actor.isBuyer()) {
            validateOrderOwnershipService.execute(actor, order);
        } else {
            authorizeSupervisionOperationService.execute(actor);
        }
        String previous = order.getStatus().name();
        order.cancel();
        releaseStockService.execute(actor, order);
        orderRepository.save(order);
        invoiceRepository.findByOrderId(orderId).ifPresent(invoice -> {
            invoice.voidInvoice();
            invoiceRepository.save(invoice);
        });
        registerAuditEventService.execute(OperationType.ORDER_CANCELLED, actor, orderId,
            Map.of("previousStatus", previous));
        return order;
    }
}
