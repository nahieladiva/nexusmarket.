package application.domain.services.product;

import application.domain.exceptions.ResourceNotFoundException;
import application.domain.models.Product;
import application.domain.models.User;
import application.domain.ports.out.ProductRepository;
import application.domain.services.DomainService;
import application.domain.services.authorization.AuthorizeSellerOperationService;
import application.domain.valueobjects.ProductId;

import java.util.List;

/**
 * Consultas del catálogo. El catálogo público solo muestra productos {@code PUBLISHED}.
 */
@DomainService
public class ConsultProductService {

    private final ProductRepository productRepository;
    private final AuthorizeSellerOperationService authorizeSellerOperationService;

    public ConsultProductService(ProductRepository productRepository,
                                 AuthorizeSellerOperationService authorizeSellerOperationService) {
        this.productRepository = productRepository;
        this.authorizeSellerOperationService = authorizeSellerOperationService;
    }

    public Product findById(ProductId id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Producto", id.toString()));
    }

    /** Producto visible en el catálogo público: solo si está {@code PUBLISHED}. */
    public Product findPublished(ProductId id) {
        Product product = findById(id);
        if (!product.isSellable()) {
            throw new ResourceNotFoundException("Producto publicado", id.toString());
        }
        return product;
    }

    /** Catálogo público: solo productos vendibles, filtrados por palabra clave. */
    public List<Product> searchCatalog(String keyword) {
        List<Product> products = (keyword == null || keyword.isBlank())
            ? productRepository.findAll()
            : productRepository.findByNameContaining(keyword.trim());
        return products.stream().filter(Product::isSellable).toList();
    }

    /** Productos del vendedor que consulta, en cualquier estado. */
    public List<Product> findBySeller(User seller) {
        authorizeSellerOperationService.execute(seller);
        return productRepository.findBySellerId(seller.getId());
    }
}
