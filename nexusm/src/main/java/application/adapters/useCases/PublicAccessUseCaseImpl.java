package application.adapters.useCases;

import application.domain.models.Product;
import application.domain.models.User;
import application.domain.ports.in.PublicAccessPort;
import application.domain.services.product.ConsultProductService;
import application.domain.services.user.RegisterBuyerService;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.IdentificationNumber;
import application.domain.valueobjects.PhoneNumber;
import application.domain.valueobjects.ProductId;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementa {@link PublicAccessPort} delegando en los servicios de dominio.
 */
@Service
@Transactional
public class PublicAccessUseCaseImpl implements PublicAccessPort {

    private final RegisterBuyerService registerBuyerService;
    private final ConsultProductService consultProductService;

    public PublicAccessUseCaseImpl(RegisterBuyerService registerBuyerService,
                                   ConsultProductService consultProductService) {
        this.registerBuyerService = registerBuyerService;
        this.consultProductService = consultProductService;
    }

    @Override
    public User registerBuyer(IdentificationNumber identification, String fullName, Email email,
                              PhoneNumber phone, String passwordHash, Address defaultShippingAddress) {
        return registerBuyerService.execute(identification, fullName, email, phone, passwordHash,
            defaultShippingAddress);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> searchCatalog(String keyword) {
        return consultProductService.searchCatalog(keyword);
    }

    @Override
    @Transactional(readOnly = true)
    public Product findProduct(ProductId productId) {
        return consultProductService.findPublished(productId);
    }
}
