package application.domain.services.product;

import application.domain.enums.OperationType;
import application.domain.enums.ProductType;
import application.domain.models.Product;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.ports.out.ProductRepository;
import application.domain.services.DomainService;
import application.domain.services.audit.RegisterAuditEventService;
import application.domain.services.authorization.AuthorizeSellerOperationService;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.ProductCode;

import java.util.Map;

/**
 * Regla R5: solo un vendedor activo y aprobado publica productos.
 * El código (SKU) debe ser único.
 */
@DomainService
public class CreateProductService {

    private final ProductRepository productRepository;
    private final AuthorizeSellerOperationService authorizeSellerOperationService;
    private final RegisterAuditEventService registerAuditEventService;

    public CreateProductService(ProductRepository productRepository,
                                AuthorizeSellerOperationService authorizeSellerOperationService,
                                RegisterAuditEventService registerAuditEventService) {
        this.productRepository = productRepository;
        this.authorizeSellerOperationService = authorizeSellerOperationService;
        this.registerAuditEventService = registerAuditEventService;
    }

    public Product execute(User actor, ProductCode code, String name, String description,
                           Money price, ProductType type) {
        Seller seller = authorizeSellerOperationService.execute(actor);
        if (productRepository.findByCode(code).isPresent()) {
            throw new IllegalArgumentException("Ya existe un producto con el código " + code);
        }
        Product product = productRepository.save(
            Product.create(code, name, description, price, seller.getId(), type));
        registerAuditEventService.execute(OperationType.PRODUCT_CREATED, seller, product.getId(),
            Map.of("code", code.getValue(), "type", type.name(),
                "price", price.getAmount().toPlainString() + " " + price.getCurrency()));
        return product;
    }
}
