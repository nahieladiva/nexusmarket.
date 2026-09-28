package application.domain.services.user;

import application.domain.enums.OperationType;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.ports.out.UserRepository;
import application.domain.services.DomainService;
import application.domain.services.audit.RegisterAuditEventService;
import application.domain.services.authorization.AuthorizeAdminOperationService;
import application.domain.valueobjects.UserId;

import java.util.Map;

/**
 * Aprueba un vendedor ({@code PENDING_APPROVAL} o {@code SUSPENDED} -> {@code APPROVED}). Solo Administrador.
 */
@DomainService
public class ApproveSellerService {

    private final UserRepository userRepository;
    private final ConsultUserService consultUserService;
    private final AuthorizeAdminOperationService authorizeAdminOperationService;
    private final RegisterAuditEventService registerAuditEventService;

    public ApproveSellerService(UserRepository userRepository, ConsultUserService consultUserService,
                                AuthorizeAdminOperationService authorizeAdminOperationService,
                                RegisterAuditEventService registerAuditEventService) {
        this.userRepository = userRepository;
        this.consultUserService = consultUserService;
        this.authorizeAdminOperationService = authorizeAdminOperationService;
        this.registerAuditEventService = registerAuditEventService;
    }

    public Seller execute(User admin, UserId sellerId) {
        authorizeAdminOperationService.execute(admin);
        User user = consultUserService.findById(sellerId);
        if (!(user instanceof Seller seller)) {
            throw new IllegalArgumentException("El usuario " + sellerId + " no es un vendedor");
        }
        String previous = seller.getSellerStatus().name();
        seller.approve();
        userRepository.save(seller);
        registerAuditEventService.execute(OperationType.SELLER_APPROVED, admin, seller.getId(),
            Map.of("previousStatus", previous, "newStatus", seller.getSellerStatus().name()));
        return seller;
    }
}
