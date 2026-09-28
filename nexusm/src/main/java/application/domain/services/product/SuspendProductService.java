package application.domain.services.product;

import application.domain.enums.OperationType;
import application.domain.models.Product;
import application.domain.models.User;
import application.domain.ports.out.ProductRepository;
import application.domain.services.DomainService;
import application.domain.services.audit.RegisterAuditEventService;
import application.domain.services.authorization.AuthorizeSellerOperationService;
import application.domain.services.authorization.AuthorizeSupervisionOperationService;
import application.domain.services.authorization.ValidateProductOwnershipService;
import application.domain.valueobjects.ProductId;

import java.util.Map;

/**
 * Suspende un producto ({@code PUBLISHED} -> {@code SUSPENDED}). Lo hace el vendedor dueño
 * o, como moderación, un Administrador o Supervisor.
 */
@DomainService
public class SuspendProductService {

    private final ProductRepository productRepository;
    private final ConsultProductService consultProductService;
    private final AuthorizeSellerOperationService authorizeSellerOperationService;
    private final AuthorizeSupervisionOperationService authorizeSupervisionOperationService;
    private final ValidateProductOwnershipService validateProductOwnershipService;
    private final RegisterAuditEventService registerAuditEventService;

    public SuspendProductService(ProductRepository productRepository,
                                 ConsultProductService consultProductService,
                                 AuthorizeSellerOperationService authorizeSellerOperationService,
                                 AuthorizeSupervisionOperationService authorizeSupervisionOperationService,
                                 ValidateProductOwnershipService validateProductOwnershipService,
                                 RegisterAuditEventService registerAuditEventService) {
        this.productRepository = productRepository;
        this.consultProductService = consultProductService;
        this.authorizeSellerOperationService = authorizeSellerOperationService;
        this.authorizeSupervisionOperationService = authorizeSupervisionOperationService;
        this.validateProductOwnershipService = validateProductOwnershipService;
        this.registerAuditEventService = registerAuditEventService;
    }

    public Product execute(User actor, ProductId productId) {
        Product product = consultProductService.findById(productId);
        if (actor != null && actor.isSeller()) {
            validateProductOwnershipService.execute(
                authorizeSellerOperationService.execute(actor), product);
        } else {
            // Moderación: Administrador o Supervisor pueden suspender cualquier producto.
            authorizeSupervisionOperationService.execute(actor);
        }
        String previous = product.getStatus().name();
        product.suspend();
        productRepository.save(product);
        registerAuditEventService.execute(OperationType.PRODUCT_STATUS_CHANGED, actor, productId,
            Map.of("previousStatus", previous, "newStatus", product.getStatus().name()));
        return product;
    }
}
