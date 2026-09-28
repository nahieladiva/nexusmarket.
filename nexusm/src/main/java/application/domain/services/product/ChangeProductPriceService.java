package application.domain.services.product;

import application.domain.enums.OperationType;
import application.domain.models.Product;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.ports.out.ProductRepository;
import application.domain.services.DomainService;
import application.domain.services.audit.RegisterAuditEventService;
import application.domain.services.authorization.AuthorizeSellerOperationService;
import application.domain.services.authorization.ValidateProductOwnershipService;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.ProductId;

import java.util.Map;

/**
 * El vendedor dueño cambia el precio de su producto (no aplica a descontinuados).
 */
@DomainService
public class ChangeProductPriceService {

    private final ProductRepository productRepository;
    private final ConsultProductService consultProductService;
    private final AuthorizeSellerOperationService authorizeSellerOperationService;
    private final ValidateProductOwnershipService validateProductOwnershipService;
    private final RegisterAuditEventService registerAuditEventService;

    public ChangeProductPriceService(ProductRepository productRepository,
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

    public Product execute(User actor, ProductId productId, Money newPrice) {
        Seller seller = authorizeSellerOperationService.execute(actor);
        Product product = consultProductService.findById(productId);
        validateProductOwnershipService.execute(seller, product);
        String previous = product.getPrice().getAmount().toPlainString();
        product.changePrice(newPrice);
        productRepository.save(product);
        registerAuditEventService.execute(OperationType.PRODUCT_PRICE_CHANGED, seller, productId,
            Map.of("previousPrice", previous, "newPrice", newPrice.getAmount().toPlainString()));
        return product;
    }
}
