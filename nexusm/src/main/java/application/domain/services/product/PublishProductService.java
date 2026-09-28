package application.domain.services.product;

import application.domain.enums.OperationType;
import application.domain.models.Product;
import application.domain.models.User;
import application.domain.ports.out.ProductRepository;
import application.domain.services.DomainService;
import application.domain.services.audit.RegisterAuditEventService;
import application.domain.services.authorization.AuthorizeSellerOperationService;
import application.domain.services.authorization.ValidateProductOwnershipService;
import application.domain.valueobjects.ProductId;

import java.util.Map;

/**
 * Vuelve a publicar un producto suspendido ({@code SUSPENDED} -> {@code PUBLISHED}).
 * Solo el vendedor dueño, activo y aprobado.
 */
@DomainService
public class PublishProductService {

    private final ProductRepository productRepository;
    private final ConsultProductService consultProductService;
    private final AuthorizeSellerOperationService authorizeSellerOperationService;
    private final ValidateProductOwnershipService validateProductOwnershipService;
    private final RegisterAuditEventService registerAuditEventService;

    public PublishProductService(ProductRepository productRepository,
                                 ConsultProductService consultProductService,
                                 AuthorizeSellerOperationService authorizeSellerOperationService,
                                 ValidateProductOwnershipService validateProductOwnershipService,
                                 RegisterAuditEventService registerAuditEventService) {
        this.productRepository = productRepository;
        this.consultProductService = consultProductService;
        this.authorizeSellerOperationService = authorizeSellerOperationService;
        this.validateProductOwnershipService = validateProductOwnershipService;
        this.registerAuditEventService = registerAuditEventService;
    }

    public Product execute(User actor, ProductId productId) {
        Product product = consultProductService.findById(productId);
        validateProductOwnershipService.execute(
            authorizeSellerOperationService.execute(actor), product);
        String previous = product.getStatus().name();
        product.publish();
        productRepository.save(product);
        registerAuditEventService.execute(OperationType.PRODUCT_STATUS_CHANGED, actor, productId,
            Map.of("previousStatus", previous, "newStatus", product.getStatus().name()));
        return product;
    }
}
