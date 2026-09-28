package application.domain.services.invoice;

import application.domain.enums.OperationType;
import application.domain.models.Invoice;
import application.domain.models.Order;
import application.domain.models.User;
import application.domain.ports.out.InvoiceRepository;
import application.domain.services.DomainService;
import application.domain.services.audit.RegisterAuditEventService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Emite la factura de un pedido pendiente de pago. Número de factura:
 * {@code FAC-yyyyMMdd-<8 primeros caracteres del id del pedido>}.
 */
@DomainService
public class IssueInvoiceService {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final InvoiceRepository invoiceRepository;
    private final RegisterAuditEventService registerAuditEventService;

    public IssueInvoiceService(InvoiceRepository invoiceRepository,
                               RegisterAuditEventService registerAuditEventService) {
        this.invoiceRepository = invoiceRepository;
        this.registerAuditEventService = registerAuditEventService;
    }

    public Invoice execute(User actor, Order order) {
        if (invoiceRepository.findByOrderId(order.getId()).isPresent()) {
            throw new IllegalStateException("El pedido " + order.getId() + " ya tiene factura");
        }
        String number = "FAC-" + LocalDate.now().format(DATE) + "-"
            + order.getId().toString().substring(0, 8).toUpperCase();
        Invoice invoice = invoiceRepository.save(Invoice.issueFor(order, number));
        registerAuditEventService.execute(OperationType.INVOICE_ISSUED, actor, invoice.getId(),
            Map.of("orderId", order.getId().toString(), "invoiceNumber", number,
                "total", order.getTotal().getAmount().toPlainString()));
        return invoice;
    }
}
