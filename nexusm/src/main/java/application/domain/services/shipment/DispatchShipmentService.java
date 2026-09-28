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
 * El Operador Logístico despacha el envío con su número de guía (regla R11):
 * el envío pasa a {@code IN_TRANSIT} y el pedido a {@code SHIPPED}.
 */
@DomainService
public class DispatchShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;
    private final ConsultShipmentService consultShipmentService;
    private final ConsultOrderService consultOrderService;
    private final AuthorizeLogisticOperationService authorizeLogisticOperationService;
    private final RegisterAuditEventService registerAuditEventService;

    public DispatchShipmentService(ShipmentRepository shipmentRepository,
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

    public Shipment execute(User operator, ShipmentId shipmentId, String trackingNumber) {
        authorizeLogisticOperationService.execute(operator);
        Shipment shipment = consultShipmentService.require(shipmentId);
        Order order = consultOrderService.require(shipment.getOrderId());
        shipment.dispatch(operator, trackingNumber);
        order.ship();
        shipmentRepository.save(shipment);
        orderRepository.save(order);
        registerAuditEventService.execute(OperationType.SHIPMENT_DISPATCHED, operator, shipmentId,
            Map.of("orderId", order.getId().toString(), "trackingNumber", shipment.getTrackingNumber()));
        return shipment;
    }
}
