package application.domain.models;

import application.domain.enums.UserRole;
import application.domain.enums.UserStatus;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.IdentificationNumber;
import application.domain.valueobjects.PhoneNumber;
import application.domain.valueobjects.UserId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Comprador de la plataforma. Siempre tiene rol {@code BUYER} y una
 * dirección de envío por defecto.
 */
public class Buyer extends User {

    private Address defaultShippingAddress;

    public Buyer(UserId id, IdentificationNumber identification, String fullName, Email email,
                 PhoneNumber phone, String passwordHash, Address defaultShippingAddress,
                 List<Address> addresses, UserStatus status, LocalDateTime createdAt) {
        super(id, identification, fullName, email, phone, UserRole.BUYER, status,
            passwordHash, addresses, createdAt);
        this.defaultShippingAddress =
            Objects.requireNonNull(defaultShippingAddress, "La dirección de envío es obligatoria");
    }

    public static Buyer create(IdentificationNumber identification, String fullName, Email email,
                               PhoneNumber phone, String passwordHash,
                               Address defaultShippingAddress) {
        return new Buyer(UserId.random(), identification, fullName, email, phone, passwordHash,
            defaultShippingAddress, List.of(), UserStatus.ACTIVE, LocalDateTime.now());
    }

    public void changeDefaultShippingAddress(Address newAddress) {
        this.defaultShippingAddress =
            Objects.requireNonNull(newAddress, "La dirección de envío es obligatoria");
    }

    public Address getDefaultShippingAddress() {
        return defaultShippingAddress;
    }
}
