package application.domain.ports.out;

import application.domain.models.Shipment;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.ShipmentId;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para la persistencia de envíos.
 */
public interface ShipmentRepository {

    Shipment save(Shipment shipment);

    Optional<Shipment> findById(ShipmentId id);

    Optional<Shipment> findByOrderId(OrderId orderId);

    List<Shipment> findAll();
}
