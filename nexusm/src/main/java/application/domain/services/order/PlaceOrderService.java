package application.domain.services.order;

import application.domain.enums.OperationType;
import application.domain.models.Cart;
import application.domain.models.Invoice;
import application.domain.models.Order;
import application.domain.models.User;
import application.domain.ports.out.CartRepository;
import application.domain.ports.out.OrderRepository;
import application.domain.services.DomainService;
import application.domain.services.audit.RegisterAuditEventService;
import application.domain.services.cart.ConsultCartService;
import application.domain.services.inventory.ReserveStockService;
import application.domain.services.invoice.IssueInvoiceService;

import java.util.Map;

/**
 * Convierte el carrito del comprador en un pedido:
 * <ol>
 *   <li>checkout del carrito: el pedido queda {@code PENDING_PAYMENT} y el carrito vacío;</li>
 *   <li>reserva de stock en bodegas activas (solo productos físicos);</li>
 *   <li>emisión de la factura;</li>
 *   <li>auditoría.</li>
 * </ol>
 */
@DomainService
public class PlaceOrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ConsultCartService consultCartService;
    private final ReserveStockService reserveStockService;
    private final IssueInvoiceService issueInvoiceService;
    private final RegisterAuditEventService registerAuditEventService;

    public PlaceOrderService(OrderRepository orderRepository, CartRepository cartRepository,
                             ConsultCartService consultCartService,
                             ReserveStockService reserveStockService,
                             IssueInvoiceService issueInvoiceService,
                             RegisterAuditEventService registerAuditEventService) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.consultCartService = consultCartService;
        this.reserveStockService = reserveStockService;
        this.issueInvoiceService = issueInvoiceService;
        this.registerAuditEventService = registerAuditEventService;
    }

    public Order execute(User buyer) {
        Cart cart = consultCartService.execute(buyer);
        Order order = cart.checkout();
        reserveStockService.execute(buyer, order);
        Order saved = orderRepository.save(order);
        cartRepository.save(cart);
        Invoice invoice = issueInvoiceService.execute(buyer, saved);
        registerAuditEventService.execute(OperationType.ORDER_PLACED, buyer, saved.getId(),
            Map.of("total", saved.getTotal().getAmount().toPlainString(),
                "items", String.valueOf(saved.getItems().size()),
                "invoiceNumber", invoice.getInvoiceNumber()));
        return saved;
    }
}
