package application.adapters.out.persistence.mysql.mappers;

import application.adapters.out.persistence.mysql.entities.UserJpaEntity;
import application.domain.enums.SellerStatus;
import application.domain.enums.UserRole;
import application.domain.enums.UserStatus;
import application.domain.models.Buyer;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.IdentificationNumber;
import application.domain.valueobjects.PhoneNumber;
import application.domain.valueobjects.UserId;

import java.util.List;

import org.springframework.stereotype.Component;

/**
 * Traduce entre el modelo de dominio {@link User} (y sus subclases) y la
 * entidad JPA {@link UserJpaEntity}. Los enums se guardan como texto.
 */
@Component
public class UserEntityMapper {

    public UserJpaEntity toEntity(User user) {
        UserJpaEntity entity = new UserJpaEntity(
            user.getId().toString(),
            user.getIdentification().getValue(),
            user.getFullName(),
            user.getEmail().getValue(),
            user.getPhone().getValue(),
            user.getRole().name(),
            user.getStatus().name(),
            user.getPasswordHash(),
            user.getCreatedAt());
        if (user instanceof Seller seller) {
            entity.setBusinessName(seller.getBusinessName());
            entity.setSellerStatus(seller.getSellerStatus().name());
        } else if (user instanceof Buyer buyer) {
            Address address = buyer.getDefaultShippingAddress();
            entity.setShippingStreet(address.getStreet());
            entity.setShippingCity(address.getCity());
            entity.setShippingState(address.getState());
            entity.setShippingZip(address.getZipCode());
            entity.setShippingCountry(address.getCountry());
        }
        return entity;
    }

    public User toDomain(UserJpaEntity entity) {
        UserRole role = UserRole.valueOf(entity.getRole());
        UserStatus status = UserStatus.valueOf(entity.getStatus());
        UserId id = UserId.of(entity.getId());
        IdentificationNumber identification = IdentificationNumber.of(entity.getIdentification());
        return switch (role) {
            case BUYER -> new Buyer(
                id,
                identification,
                entity.getFullName(),
                Email.of(entity.getEmail()),
                PhoneNumber.of(entity.getPhone()),
                entity.getPasswordHash(),
                Address.of(entity.getShippingStreet(), entity.getShippingCity(),
                    entity.getShippingState(), entity.getShippingZip(),
                    entity.getShippingCountry()),
                List.of(),
                status,
                entity.getCreatedAt());
            case SELLER -> new Seller(
                id,
                identification,
                entity.getFullName(),
                Email.of(entity.getEmail()),
                PhoneNumber.of(entity.getPhone()),
                entity.getPasswordHash(),
                entity.getBusinessName(),
                List.of(),
                entity.getSellerStatus() == null
                    ? SellerStatus.PENDING_APPROVAL
                    : SellerStatus.valueOf(entity.getSellerStatus()),
                status,
                entity.getCreatedAt());
            default -> new User(
                id,
                identification,
                entity.getFullName(),
                Email.of(entity.getEmail()),
                PhoneNumber.of(entity.getPhone()),
                role,
                status,
                entity.getPasswordHash(),
                List.of(),
                entity.getCreatedAt());
        };
    }
}
