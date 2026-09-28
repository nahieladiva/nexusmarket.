package application.domain.services.returns;

import application.domain.enums.OperationType;
import application.domain.enums.ReturnStatus;
import application.domain.models.Order;
import application.domain.models.OrderItem;
import application.domain.models.ReturnRequest;
import application.domain.models.User;
import application.domain.ports.out.ReturnRequestRepository;
import application.domain.services.DomainService;
import application.domain.services.audit.RegisterAuditEventService;
import application.domain.services.authorization.AuthorizeBuyerOperationService;
import application.domain.services.authorization.ValidateOrderOwnershipService;
import application.domain.services.order.ConsultOrderService;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;

import java.util.Map;

/**
 * El comprador dueño solicita la devolución de un producto de un pedido
 * entregado (regla R12). La suma de las devoluciones no rechazadas de un
 * producto nunca supera la cantidad comprada.
 */
@DomainService
public class RequestReturnService {

    private final ReturnRequestRepository returnRequestRepository;
    private final ConsultOrderService consultOrderService;
    private final AuthorizeBuyerOperationService authorizeBuyerOperationService;
    private final ValidateOrderOwnershipService validateOrderOwnershipService;
    private final RegisterAuditEventService registerAuditEventService;

    public RequestReturnService(ReturnRequestRepository returnRequestRepository,
                                ConsultOrderService consultOrderService,
                                AuthorizeBuyerOperationService authorizeBuyerOperationService,
                                ValidateOrderOwnershipService validateOrderOwnershipService,
                                RegisterAuditEventService registerAuditEventService) {
        this.returnRequestRepository = returnRequestRepository;
        this.consultOrderService = consultOrderService;
        this.authorizeBuyerOperationService = authorizeBuyerOperationService;
        this.validateOrderOwnershipService = validateOrderOwnershipService;
        this.registerAuditEventService = registerAuditEventService;
    }

    public ReturnRequest execute(User buyer, OrderId orderId, ProductId productId,
                                 Quantity quantity, String reason) {
        authorizeBuyerOperationService.execute(buyer);
        Order order = consultOrderService.require(orderId);
        validateOrderOwnershipService.execute(buyer, order);
        int alreadyReturned = returnRequestRepository.findByOrderId(orderId).stream()
            .filter(r -> r.getProductId().equals(productId))
            .filter(r -> r.getStatus() != ReturnStatus.REJECTED)
            .mapToInt(r -> r.getQuantity().getValue())
            .sum();
        int purchased = order.findItem(productId).map(OrderItem::getQuantity)
            .map(Quantity::getValue).orElse(0);
        if (alreadyReturned + quantity.getValue() > purchased) {
            throw new IllegalArgumentException("La cantidad a devolver supera la cantidad comprada ("
                + purchased + ") menos lo ya devuelto (" + alreadyReturned + ")");
        }
        ReturnRequest returnRequest = returnRequestRepository.save(
            ReturnRequest.request(order, productId, quantity, reason));
        registerAuditEventService.execute(OperationType.RETURN_REQUESTED, buyer, returnRequest.getId(),
            Map.of("orderId", orderId.toString(), "productId", productId.toString(),
                "quantity", String.valueOf(quantity.getValue())));
        return returnRequest;
    }
}
