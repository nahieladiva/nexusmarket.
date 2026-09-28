package application.domain.ports.in;

import application.domain.models.Product;
import application.domain.models.User;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.IdentificationNumber;
import application.domain.valueobjects.PhoneNumber;
import application.domain.valueobjects.ProductId;

import java.util.List;

/**
 * Casos de uso de acceso público (sin usuario autenticado).
 */
public interface PublicAccessPort {

    User registerBuyer(IdentificationNumber identification, String fullName, Email email,
                       PhoneNumber phone, String passwordHash, Address defaultShippingAddress);

    List<Product> searchCatalog(String keyword);

    Product findProduct(ProductId productId);
}
