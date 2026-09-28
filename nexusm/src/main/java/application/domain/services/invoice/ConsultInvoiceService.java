package application.domain.services.invoice;

import application.domain.exceptions.ResourceNotFoundException;
import application.domain.models.Invoice;
import application.domain.models.User;
import application.domain.ports.out.InvoiceRepository;
import application.domain.services.DomainService;
import application.domain.services.order.ConsultOrderService;
import application.domain.valueobjects.OrderId;

/**
 * Consulta la factura de un pedido (mismos permisos que ver el pedido).
 */
@DomainService
public class ConsultInvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final ConsultOrderService consultOrderService;

    public ConsultInvoiceService(InvoiceRepository invoiceRepository,
                                 ConsultOrderService consultOrderService) {
        this.invoiceRepository = invoiceRepository;
        this.consultOrderService = consultOrderService;
    }

    /** Carga la factura sin validar permisos (uso interno). */
    public Invoice require(OrderId orderId) {
        return invoiceRepository.findByOrderId(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Factura del pedido", orderId.toString()));
    }

    public Invoice findByOrder(User actor, OrderId orderId) {
        consultOrderService.findById(actor, orderId);
        return require(orderId);
    }
}
