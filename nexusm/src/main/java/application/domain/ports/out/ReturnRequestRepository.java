package application.domain.ports.out;

import application.domain.models.ReturnRequest;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.ReturnId;
import application.domain.valueobjects.UserId;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para la persistencia de devoluciones.
 */
public interface ReturnRequestRepository {

    ReturnRequest save(ReturnRequest returnRequest);

    Optional<ReturnRequest> findById(ReturnId id);

    List<ReturnRequest> findByOrderId(OrderId orderId);

    List<ReturnRequest> findByBuyerId(UserId buyerId);

    List<ReturnRequest> findAll();
}
