package application.adapters.useCases;

import application.domain.enums.ProductType;
import application.domain.models.Product;
import application.domain.models.User;
import application.domain.ports.in.SellerPort;
import application.domain.services.product.ChangeProductPriceService;
import application.domain.services.product.ConsultProductService;
import application.domain.services.product.CreateProductService;
import application.domain.services.product.DiscontinueProductService;
import application.domain.services.product.PublishProductService;
import application.domain.services.product.SuspendProductService;
import application.domain.services.user.ConsultUserService;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.ProductCode;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.UserId;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementa {@link SellerPort}: carga al vendedor y delega en los servicios de dominio.
 */
@Service
@Transactional
public class SellerUseCaseImpl implements SellerPort {

    private final ConsultUserService consultUserService;
    private final CreateProductService createProductService;
    private final ConsultProductService consultProductService;
    private final ChangeProductPriceService changeProductPriceService;
    private final SuspendProductService suspendProductService;
    private final PublishProductService publishProductService;
    private final DiscontinueProductService discontinueProductService;

    public SellerUseCaseImpl(ConsultUserService consultUserService,
                             CreateProductService createProductService,
                             ConsultProductService consultProductService,
                             ChangeProductPriceService changeProductPriceService,
                             SuspendProductService suspendProductService,
                             PublishProductService publishProductService,
                             DiscontinueProductService discontinueProductService) {
        this.consultUserService = consultUserService;
        this.createProductService = createProductService;
        this.consultProductService = consultProductService;
        this.changeProductPriceService = changeProductPriceService;
        this.suspendProductService = suspendProductService;
        this.publishProductService = publishProductService;
        this.discontinueProductService = discontinueProductService;
    }

    private User seller(UserId sellerId) {
        return consultUserService.findById(sellerId);
    }

    @Override
    public Product createProduct(UserId sellerId, ProductCode code, String name,
                                 String description, Money price, ProductType type) {
        return createProductService.execute(seller(sellerId), code, name, description, price, type);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findMyProducts(UserId sellerId) {
        return consultProductService.findBySeller(seller(sellerId));
    }

    @Override
    public Product changePrice(UserId sellerId, ProductId productId, Money newPrice) {
        return changeProductPriceService.execute(seller(sellerId), productId, newPrice);
    }

    @Override
    public Product suspendProduct(UserId sellerId, ProductId productId) {
        return suspendProductService.execute(seller(sellerId), productId);
    }

    @Override
    public Product publishProduct(UserId sellerId, ProductId productId) {
        return publishProductService.execute(seller(sellerId), productId);
    }

    @Override
    public Product discontinueProduct(UserId sellerId, ProductId productId) {
        return discontinueProductService.execute(seller(sellerId), productId);
    }
}
