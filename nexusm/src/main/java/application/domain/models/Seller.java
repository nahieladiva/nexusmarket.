package application.domain.models;

import application.domain.enums.SellerStatus;
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
import java.util.List;

/**
 * Vendedor de la plataforma.
 *
 * <p>Reglas:</p>
 * <ul>
 *   <li>Solo un Administrador puede registrarlo (se valida en el caso de uso).</li>
 *   <li>Nace en {@code PENDING_APPROVAL} y solo publica productos cuando está {@code APPROVED}.</li>
 * </ul>
 */
public class Seller extends User {

    private String businessName;
    private SellerStatus sellerStatus;

    public Seller(UserId id, IdentificationNumber identification, String fullName, Email email,
                  PhoneNumber phone, String passwordHash, String businessName,
                  List<Address> addresses, SellerStatus sellerStatus, UserStatus status,
                  LocalDateTime createdAt) {
        super(id, identification, fullName, email, phone, UserRole.SELLER, status,
            passwordHash, addresses, createdAt);
        this.businessName = requireNotBlank(businessName, "El nombre comercial es obligatorio");
        this.sellerStatus = sellerStatus == null ? SellerStatus.PENDING_APPROVAL : sellerStatus;
    }

    public static Seller create(IdentificationNumber identification, String fullName, Email email,
                                PhoneNumber phone, String passwordHash, String businessName) {
        return new Seller(UserId.random(), identification, fullName, email, phone, passwordHash,
            businessName, List.of(), SellerStatus.PENDING_APPROVAL, UserStatus.ACTIVE,
            LocalDateTime.now());
    }

    public void approve() {
        if (sellerStatus == SellerStatus.APPROVED) {
            throw new InvalidStatusTransitionException("Vendedor", sellerStatus, SellerStatus.APPROVED);
        }
        this.sellerStatus = SellerStatus.APPROVED;
    }

    public void suspend() {
        if (sellerStatus != SellerStatus.APPROVED) {
            throw new InvalidStatusTransitionException("Vendedor", sellerStatus, SellerStatus.SUSPENDED);
        }
        this.sellerStatus = SellerStatus.SUSPENDED;
    }

    public void changeBusinessName(String newBusinessName) {
        this.businessName = requireNotBlank(newBusinessName, "El nombre comercial es obligatorio");
    }

    public String getBusinessName() {
        return businessName;
    }

    public SellerStatus getSellerStatus() {
        return sellerStatus;
    }

    public boolean isApproved() {
        return sellerStatus == SellerStatus.APPROVED;
    }

    /**
     * Un vendedor solo puede publicar si está activo y aprobado.
     */
    public void requireApprovedToPublish() {
        requireActive();
        if (!sellerStatus.canPublishProducts()) {
            throw new UnauthorizedOperationException(
                "El vendedor debe estar aprobado para publicar productos (estado actual: "
                    + sellerStatus + ")");
        }
    }
}
