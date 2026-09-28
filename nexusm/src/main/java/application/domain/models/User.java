package application.domain.models;

import application.domain.enums.UserRole;
import application.domain.enums.UserStatus;
import application.domain.exceptions.InvalidStatusTransitionException;
import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.IdentificationNumber;
import application.domain.valueobjects.PhoneNumber;
import application.domain.valueobjects.UserId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Entidad base de usuario del sistema.
 *
 * <p>Invariantes:</p>
 * <ul>
 *   <li>El id, la identificación, el email y el rol son inmutables.</li>
 *   <li>La identificación y el email son únicos en la plataforma
 *       (se valida en el caso de uso contra el repositorio).</li>
 *   <li>Cada usuario tiene un único rol.</li>
 *   <li>Un usuario {@code BLOCKED} no puede operar.</li>
 * </ul>
 *
 * <p>Compradores y vendedores se modelan con las subclases {@link Buyer} y
 * {@link Seller}. El personal interno (operador logístico, administrador y
 * supervisor) se modela con esta clase usando {@link #createStaff}.</p>
 */
public class User {

    private final UserId id;
    private final IdentificationNumber identification;
    private String fullName;
    private final Email email;
    private PhoneNumber phone;
    private final UserRole role;
    private UserStatus status;
    private String passwordHash;
    private final List<Address> addresses;
    private final LocalDateTime createdAt;

    public User(UserId id, IdentificationNumber identification, String fullName, Email email,
                PhoneNumber phone, UserRole role, UserStatus status, String passwordHash,
                List<Address> addresses, LocalDateTime createdAt) {
        this.id = Objects.requireNonNull(id, "El id de usuario es obligatorio");
        this.identification =
            Objects.requireNonNull(identification, "El número de identificación es obligatorio");
        this.fullName = requireNotBlank(fullName, "El nombre completo es obligatorio");
        this.email = Objects.requireNonNull(email, "El email es obligatorio");
        this.phone = Objects.requireNonNull(phone, "El teléfono es obligatorio");
        this.role = Objects.requireNonNull(role, "El rol es obligatorio");
        this.status = status == null ? UserStatus.ACTIVE : status;
        this.passwordHash = passwordHash;
        this.addresses = new ArrayList<>(addresses == null ? List.of() : addresses);
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
    }

    /**
     * Crea un usuario interno de la plataforma (operador logístico,
     * administrador o supervisor).
     */
    public static User createStaff(IdentificationNumber identification, String fullName,
                                   Email email, PhoneNumber phone, UserRole role,
                                   String passwordHash) {
        Objects.requireNonNull(role, "El rol es obligatorio");
        if (!role.isStaff()) {
            throw new IllegalArgumentException(
                "Los compradores y vendedores se crean con Buyer.create o Seller.create");
        }
        return new User(UserId.random(), identification, fullName, email, phone, role,
            UserStatus.ACTIVE, passwordHash, List.of(), LocalDateTime.now());
    }

    protected static String requireNotBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    public void changeName(String newName) {
        this.fullName = requireNotBlank(newName, "El nombre completo es obligatorio");
    }

    public void changePhone(PhoneNumber newPhone) {
        this.phone = Objects.requireNonNull(newPhone, "El teléfono es obligatorio");
    }

    public void changePasswordHash(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
    }

    public void addAddress(Address address) {
        this.addresses.add(Objects.requireNonNull(address, "La dirección es obligatoria"));
    }

    public void block() {
        if (status == UserStatus.BLOCKED) {
            throw new InvalidStatusTransitionException("Usuario", status, UserStatus.BLOCKED);
        }
        this.status = UserStatus.BLOCKED;
    }

    public void activate() {
        if (status == UserStatus.ACTIVE) {
            throw new InvalidStatusTransitionException("Usuario", status, UserStatus.ACTIVE);
        }
        this.status = UserStatus.ACTIVE;
    }

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    /**
     * Garantiza que el usuario pueda operar en la plataforma.
     */
    public void requireActive() {
        if (!isActive()) {
            throw new UnauthorizedOperationException(
                "El usuario " + id + " está bloqueado y no puede operar");
        }
    }

    public boolean isBuyer() {
        return role == UserRole.BUYER;
    }

    public boolean isSeller() {
        return role == UserRole.SELLER;
    }

    public boolean isLogisticOperator() {
        return role == UserRole.LOGISTIC_OPERATOR;
    }

    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }

    public boolean isSupervisor() {
        return role == UserRole.SUPERVISOR;
    }

    public UserId getId() {
        return id;
    }

    public IdentificationNumber getIdentification() {
        return identification;
    }

    public String getFullName() {
        return fullName;
    }

    public Email getEmail() {
        return email;
    }

    public PhoneNumber getPhone() {
        return phone;
    }

    public UserRole getRole() {
        return role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public List<Address> getAddresses() {
        return Collections.unmodifiableList(addresses);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof User other)) {
            return false;
        }
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
