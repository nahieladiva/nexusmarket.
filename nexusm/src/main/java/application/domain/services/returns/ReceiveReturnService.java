package application.domain.services.returns;

import application.domain.enums.OperationType;
import application.domain.models.Order;
import application.domain.models.OrderItem;
import application.domain.models.ReturnRequest;
import application.domain.models.User;
import application.domain.ports.out.ReturnRequestRepository;
import application.domain.services.DomainService;
import application.domain.services.audit.RegisterAuditEventService;
import application.domain.services.authorization.AuthorizeLogisticOperationService;
import application.domain.services.inventory.RestockReturnedItemService;
import application.domain.services.order.ConsultOrderService;
import application.domain.valueobjects.ReturnId;

import java.util.Map;

/**
 * El Operador Logístico recibe en bodega el producto de una devolución
 * aprobada ({@code APPROVED} -> {@code RECEIVED}); si es físico, las unidades
 * reingresan a la bodega de origen con un movimiento {@code RETURN}.
 */
@DomainService
public class ReceiveReturnService {

    private final ReturnRequestRepository returnRequestRepository;
    private final ConsultReturnService consultReturnService;
    private final ConsultOrderService consultOrderService;
    private final RestockReturnedItemService restockReturnedItemService;
    private final AuthorizeLogisticOperationService authorizeLogisticOperationService;
    private final RegisterAuditEventService registerAuditEventService;

    public ReceiveReturnService(ReturnRequestRepository returnRequestRepository,
                                ConsultReturnService consultReturnService,
                                ConsultOrderService consultOrderService,
                                RestockReturnedItemService restockReturnedItemService,
                                AuthorizeLogisticOperationService authorizeLogisticOperationService,
                                RegisterAuditEventService registerAuditEventService) {
        this.returnRequestRepository = returnRequestRepository;
        this.consultReturnService = consultReturnService;
        this.consultOrderService = consultOrderService;
        this.restockReturnedItemService = restockReturnedItemService;
        this.authorizeLogisticOperationService = authorizeLogisticOperationService;
        this.registerAuditEventService = registerAuditEventService;
    }

    public ReturnRequest execute(User operator, ReturnId returnId) {
        authorizeLogisticOperationService.execute(operator);
        ReturnRequest returnRequest = consultReturnService.require(returnId);
        returnRequest.markAsReceived();
        Order order = consultOrderService.require(returnRequest.getOrderId());
        order.findItem(returnRequest.getProductId())
            .filter(OrderItem::hasWarehouse)
            .ifPresent(item -> restockReturnedItemService.execute(operator,
                item.getProductId(), item.getWarehouseId(), returnRequest.getQuantity()));
        returnRequestRepository.save(returnRequest);
        registerAuditEventService.execute(OperationType.RETURN_RECEIVED, operator, returnId,
            Map.of("orderId", order.getId().toString(),
                "quantity", String.valueOf(returnRequest.getQuantity().getValue())));
        return returnRequest;
    }
}
