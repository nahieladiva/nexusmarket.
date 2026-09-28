package application.domain.services.refund;

import application.domain.enums.OperationType;
import application.domain.models.Order;
import application.domain.models.OrderItem;
import application.domain.models.Refund;
import application.domain.models.ReturnRequest;
import application.domain.models.User;
import application.domain.ports.out.RefundRepository;
import application.domain.services.DomainService;
import application.domain.services.audit.RegisterAuditEventService;
import application.domain.services.authorization.AuthorizeSupervisionOperationService;
import application.domain.services.order.ConsultOrderService;
import application.domain.services.returns.ConsultReturnService;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.ReturnId;

import java.util.Map;

/**
 * Un Administrador o Supervisor reembolsa una devolución recibida (regla R13).
 * El monto es precio unitario pagado × cantidad devuelta. Una devolución
 * solo se reembolsa una vez.
 */
@DomainService
public class ProcessRefundService {

    private final RefundRepository refundRepository;
    private final ConsultReturnService consultReturnService;
    private final ConsultOrderService consultOrderService;
    private final AuthorizeSupervisionOperationService authorizeSupervisionOperationService;
    private final RegisterAuditEventService registerAuditEventService;

    public ProcessRefundService(RefundRepository refundRepository,
                                ConsultReturnService consultReturnService,
                                ConsultOrderService consultOrderService,
                                AuthorizeSupervisionOperationService authorizeSupervisionOperationService,
                                RegisterAuditEventService registerAuditEventService) {
        this.refundRepository = refundRepository;
        this.consultReturnService = consultReturnService;
        this.consultOrderService = consultOrderService;
        this.authorizeSupervisionOperationService = authorizeSupervisionOperationService;
        this.registerAuditEventService = registerAuditEventService;
    }

    public Refund execute(User actor, ReturnId returnId) {
        authorizeSupervisionOperationService.execute(actor);
        ReturnRequest returnRequest = consultReturnService.require(returnId);
        if (refundRepository.findByReturnId(returnId).isPresent()) {
            throw new IllegalStateException("La devolución " + returnId + " ya fue reembolsada");
        }
        Order order = consultOrderService.require(returnRequest.getOrderId());
        OrderItem item = order.findItem(returnRequest.getProductId())
            .orElseThrow(() -> new IllegalStateException("El producto ya no está en el pedido"));
        Money amount = item.getUnitPrice().multiply(returnRequest.getQuantity().getValue());
        Refund refund = Refund.createFor(returnRequest, amount);
        refund.complete();
        Refund saved = refundRepository.save(refund);
        registerAuditEventService.execute(OperationType.REFUND_COMPLETED, actor, saved.getId(),
            Map.of("returnId", returnId.toString(), "orderId", order.getId().toString(),
                "amount", amount.getAmount().toPlainString()));
        return saved;
    }
}
