package application.domain.services.returns;

import application.domain.enums.OperationType;
import application.domain.models.ReturnRequest;
import application.domain.models.User;
import application.domain.ports.out.ReturnRequestRepository;
import application.domain.services.DomainService;
import application.domain.services.audit.RegisterAuditEventService;
import application.domain.services.authorization.AuthorizeSupervisionOperationService;
import application.domain.valueobjects.ReturnId;

import java.util.Map;

/**
 * Un Administrador o Supervisor aprueba una devolución ({@code REQUESTED} -> {@code APPROVED}).
 */
@DomainService
public class ApproveReturnService {

    private final ReturnRequestRepository returnRequestRepository;
    private final ConsultReturnService consultReturnService;
    private final AuthorizeSupervisionOperationService authorizeSupervisionOperationService;
    private final RegisterAuditEventService registerAuditEventService;

    public ApproveReturnService(ReturnRequestRepository returnRequestRepository,
                                ConsultReturnService consultReturnService,
                                AuthorizeSupervisionOperationService authorizeSupervisionOperationService,
                                RegisterAuditEventService registerAuditEventService) {
        this.returnRequestRepository = returnRequestRepository;
        this.consultReturnService = consultReturnService;
        this.authorizeSupervisionOperationService = authorizeSupervisionOperationService;
        this.registerAuditEventService = registerAuditEventService;
    }

    public ReturnRequest execute(User actor, ReturnId returnId) {
        authorizeSupervisionOperationService.execute(actor);
        ReturnRequest returnRequest = consultReturnService.require(returnId);
        returnRequest.approve();
        returnRequestRepository.save(returnRequest);
        registerAuditEventService.execute(OperationType.RETURN_APPROVED, actor, returnId,
            Map.of("orderId", returnRequest.getOrderId().toString()));
        return returnRequest;
    }
}
