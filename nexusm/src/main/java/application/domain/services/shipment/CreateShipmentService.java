package application.domain.services.shipment;

import application.domain.enums.OperationType;
import application.domain.models.Buyer;
import application.domain.models.Order;
import application.domain.models.OrderItem;
import application.domain.models.Shipment;
import application.domain.models.User;
import application.domain.ports.out.ShipmentRepository;
import application.domain.services.DomainService;
import application.domain.services.audit.RegisterAuditEventService;
import application.domain.services.authorization.AuthorizeLogisticOperationService;
import application.domain.services.order.ConsultOrderService;
import application.domain.services.user.ConsultUserService;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.WarehouseId;

import java.util.Map;

/**
 * El Operador Logístico prepara el envío de un pedido pagado (regla R10).
 * La bodega de origen es la asignada al reservar el stock y la dirección es
 * la dirección de envío por defecto del comprador. Un pedido tiene un solo envío.
 */
@DomainService
public class CreateShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final ConsultOrderService consultOrderService;
    private final ConsultUserService consultUserService;
    private final AuthorizeLogisticOperationService authorizeLogisticOperationService;
    private final RegisterAuditEventService registerAuditEventService;

    public CreateShipmentService(ShipmentRepository shipmentRepository,
                                 ConsultOrderService consultOrderService,
                                 ConsultUserService consultUserService,
                                 AuthorizeLogisticOperationService authorizeLogisticOperationService,
                                 RegisterAuditEventService registerAuditEventService) {
        this.shipmentRepository = shipmentRepository;
        this.consultOrderService = consultOrderService;
        this.consultUserService = consultUserService;
        this.authorizeLogisticOperationService = authorizeLogisticOperationService;
        this.registerAuditEventService = registerAuditEventService;
    }

    public Shipment execute(User operator, OrderId orderId) {
        authorizeLogisticOperationService.execute(operator);
        Order order = consultOrderService.require(orderId);
        if (shipmentRepository.findByOrderId(orderId).isPresent()) {
            throw new IllegalStateException("El pedido " + orderId + " ya tiene un envío");
        }
        WarehouseId origin = order.getItems().stream()
            .filter(OrderItem::hasWarehouse)
            .map(OrderItem::getWarehouseId)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(
                "El pedido " + orderId + " no tiene productos físicos para enviar"));
        User buyer = consultUserService.findById(order.getBuyerId());
        if (!(buyer instanceof Buyer buyerProfile)) {
            throw new IllegalStateException("El dueño del pedido no es un comprador");
        }
        Shipment shipment = shipmentRepository.save(
            Shipment.prepareFor(order, origin, buyerProfile.getDefaultShippingAddress()));
        registerAuditEventService.execute(OperationType.SHIPMENT_CREATED, operator, shipment.getId(),
            Map.of("orderId", orderId.toString(), "warehouseId", origin.toString()));
        return shipment;
    }
}
