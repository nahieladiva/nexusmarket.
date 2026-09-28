package application.domain.services.refund;

import application.domain.exceptions.ResourceNotFoundException;
import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.Refund;
import application.domain.models.ReturnRequest;
import application.domain.models.User;
import application.domain.ports.out.RefundRepository;
import application.domain.services.DomainService;
import application.domain.services.authorization.ValidateActiveUserService;
import application.domain.services.returns.ConsultReturnService;
import application.domain.valueobjects.ReturnId;

/**
 * Consulta el reembolso de una devolución. Un comprador solo ve los
 * reembolsos de sus propias devoluciones.
 */
@DomainService
public class ConsultRefundService {

    private final RefundRepository refundRepository;
    private final ConsultReturnService consultReturnService;
    private final ValidateActiveUserService validateActiveUserService;

    public ConsultRefundService(RefundRepository refundRepository,
                                ConsultReturnService consultReturnService,
                                ValidateActiveUserService validateActiveUserService) {
        this.refundRepository = refundRepository;
        this.consultReturnService = consultReturnService;
        this.validateActiveUserService = validateActiveUserService;
    }

    public Refund findByReturn(User actor, ReturnId returnId) {
        validateActiveUserService.execute(actor);
        ReturnRequest returnRequest = consultReturnService.require(returnId);
        if (actor.isBuyer() && !returnRequest.getBuyerId().equals(actor.getId())) {
            throw new UnauthorizedOperationException(
                "La devolución " + returnId + " no pertenece al usuario " + actor.getId());
        }
        return refundRepository.findByReturnId(returnId)
            .orElseThrow(() -> new ResourceNotFoundException("Reembolso de la devolución", returnId.toString()));
    }
}
