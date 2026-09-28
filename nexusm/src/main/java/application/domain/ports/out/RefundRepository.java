package application.domain.ports.out;

import application.domain.models.Refund;
import application.domain.valueobjects.RefundId;
import application.domain.valueobjects.ReturnId;

import java.util.Optional;

/**
 * Puerto de salida para la persistencia de reembolsos.
 */
public interface RefundRepository {

    Refund save(Refund refund);

    Optional<Refund> findById(RefundId id);

    Optional<Refund> findByReturnId(ReturnId returnId);
}
