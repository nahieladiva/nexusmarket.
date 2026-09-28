package application.adapters.in.rest.mappers;

import application.adapters.in.rest.requests.AddressRequest;
import application.adapters.in.rest.responses.AddressResponse;
import application.adapters.in.rest.responses.UserResponse;
import application.domain.models.Buyer;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.valueobjects.Address;

import java.util.List;

import org.springframework.stereotype.Component;

/**
 * Traduce entre los DTOs REST y el modelo de dominio de usuarios.
 */
@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        String businessName = null;
        String sellerStatus = null;
        AddressResponse defaultShipping = null;
        if (user instanceof Seller seller) {
            businessName = seller.getBusinessName();
            sellerStatus = seller.getSellerStatus().name();
        } else if (user instanceof Buyer buyer) {
            defaultShipping = toResponse(buyer.getDefaultShippingAddress());
        }
        List<AddressResponse> addresses = user.getAddresses().stream()
            .map(this::toResponse)
            .toList();
        return new UserResponse(
            user.getId().toString(),
            user.getIdentification().getValue(),
            user.getFullName(),
            user.getEmail().getValue(),
            user.getPhone().getValue(),
            user.getRole().name(),
            user.getStatus().name(),
            businessName,
            sellerStatus,
            defaultShipping,
            addresses,
            user.getCreatedAt());
    }

    public AddressResponse toResponse(Address address) {
        return new AddressResponse(address.getStreet(), address.getCity(),
            address.getState(), address.getZipCode(), address.getCountry());
    }

    public Address toDomain(AddressRequest address) {
        return Address.of(address.street(), address.city(), address.state(),
            address.zipCode(), address.country());
    }
}
