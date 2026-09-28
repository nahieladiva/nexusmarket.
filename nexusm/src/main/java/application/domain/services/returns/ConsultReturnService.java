package application.domain.services.returns;

import application.domain.exceptions.ResourceNotFoundException;
import application.domain.models.ReturnRequest;
import application.domain.models.User;
import application.domain.ports.out.ReturnRequestRepository;
import application.domain.services.DomainService;
import application.domain.services.authorization.AuthorizeBuyerOperationService;
import application.domain.services.authorization.AuthorizeSupervisionOperationService;
import application.domain.valueobjects.ReturnId;

import java.util.List;

/**
 * Consulta de devoluciones: el comprador ve las suyas; Administrador y
 * Supervisor ven todas.
 */
@DomainService
public class ConsultReturnService {

    private final ReturnRequestRepository returnRequestRepository;
    private final AuthorizeBuyerOperationService authorizeBuyerOperationService;
    private final AuthorizeSupervisionOperationService authorizeSupervisionOperationService;

    public ConsultReturnService(ReturnRequestRepository returnRequestRepository,
                                AuthorizeBuyerOperationService authorizeBuyerOperationService,
                                AuthorizeSupervisionOperationService authorizeSupervisionOperationService) {
        this.returnRequestRepository = returnRequestRepository;
        this.authorizeBuyerOperationService = authorizeBuyerOperationService;
        this.authorizeSupervisionOperationService = authorizeSupervisionOperationService;
    }

    /** Carga la devolución sin validar permisos (uso interno). */
    public ReturnRequest require(ReturnId id) {
        return returnRequestRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Devolución", id.toString()));
    }

    public List<ReturnRequest> findMine(User buyer) {
        authorizeBuyerOperationService.execute(buyer);
        return returnRequestRepository.findByBuyerId(buyer.getId());
    }

    public List<ReturnRequest> findAll(User actor) {
        authorizeSupervisionOperationService.execute(actor);
        return returnRequestRepository.findAll();
    }
}
