package application.domain.services.shipment;

import application.domain.enums.OperationType;
import application.domain.models.Order;
import application.domain.models.Shipment;
import application.domain.models.User;
import application.domain.ports.out.OrderRepository;
import application.domain.ports.out.ShipmentRepository;
import application.domain.services.DomainService;
import application.domain.services.audit.RegisterAuditEventService;
import application.domain.services.authorization.AuthorizeLogisticOperationService;
import application.domain.services.order.ConsultOrderService;
import application.domain.valueobjects.ShipmentId;

import java.util.Map;

/**
 * El Operador Logístico confirma la entrega: el envío pasa a {@code DELIVERED}
 * y el pedido a {@code DELIVERED}, que es un estado final e inmutable (regla R8).
 */
@DomainService
public class ConfirmDeliveryService {

    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;
    private final ConsultShipmentService consultShipmentService;
    private final ConsultOrderService consultOrderService;
    private final AuthorizeLogisticOperationService authorizeLogisticOperationService;
    private final RegisterAuditEventService registerAuditEventService;

    public ConfirmDeliveryService(ShipmentRepository shipmentRepository,
                                  OrderRepository orderRepository,
                                  ConsultShipmentService consultShipmentService,
                                  ConsultOrderService consultOrderService,
                                  AuthorizeLogisticOperationService authorizeLogisticOperationService,
                                  RegisterAuditEventService registerAuditEventService) {
        this.shipmentRepository = shipmentRepository;
        this.orderRepository = orderRepository;
        this.consultShipmentService = consultShipmentService;
        this.consultOrderService = consultOrderService;
        this.authorizeLogisticOperationService = authorizeLogisticOperationService;
        this.registerAuditEventService = registerAuditEventService;
    }

    public Shipment execute(User operator, ShipmentId shipmentId) {
        authorizeLogisticOperationService.execute(operator);
        Shipment shipment = consultShipmentService.require(shipmentId);
        Order order = consultOrderService.require(shipment.getOrderId());
        shipment.confirmDelivery();
        order.deliver();
        shipmentRepository.save(shipment);
        orderRepository.save(order);
        registerAuditEventService.execute(OperationType.SHIPMENT_DELIVERED, operator, shipmentId,
            Map.of("orderId", order.getId().toString()));
        return shipment;
    }
}
