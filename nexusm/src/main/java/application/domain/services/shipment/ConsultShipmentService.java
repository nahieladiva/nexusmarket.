package application.domain.services.shipment;

import application.domain.exceptions.ResourceNotFoundException;
import application.domain.models.Shipment;
import application.domain.models.User;
import application.domain.ports.out.ShipmentRepository;
import application.domain.services.DomainService;
import application.domain.services.order.ConsultOrderService;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.ShipmentId;

/**
 * Consulta de envíos (mismos permisos que ver el pedido).
 */
@DomainService
public class ConsultShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final ConsultOrderService consultOrderService;

    public ConsultShipmentService(ShipmentRepository shipmentRepository,
                                  ConsultOrderService consultOrderService) {
        this.shipmentRepository = shipmentRepository;
        this.consultOrderService = consultOrderService;
    }

    /** Carga el envío sin validar permisos (uso interno). */
    public Shipment require(ShipmentId id) {
        return shipmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Envío", id.toString()));
    }

    public Shipment findByOrder(User actor, OrderId orderId) {
        consultOrderService.findById(actor, orderId);
        return shipmentRepository.findByOrderId(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Envío del pedido", orderId.toString()));
    }
}
