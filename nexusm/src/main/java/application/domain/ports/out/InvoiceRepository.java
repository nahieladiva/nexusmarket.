package application.domain.ports.out;

import application.domain.models.Invoice;
import application.domain.valueobjects.InvoiceId;
import application.domain.valueobjects.OrderId;

import java.util.Optional;

/**
 * Puerto de salida para la persistencia de facturas.
 */
public interface InvoiceRepository {

    Invoice save(Invoice invoice);

    Optional<Invoice> findById(InvoiceId id);

    Optional<Invoice> findByOrderId(OrderId orderId);
}
