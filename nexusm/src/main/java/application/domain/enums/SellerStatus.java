package application.domain.enums;

/**
 * Estado comercial de un vendedor. Un vendedor solo puede publicar
 * productos cuando está {@code APPROVED}.
 */
public enum SellerStatus {

    PENDING_APPROVAL,
    APPROVED,
    SUSPENDED;

    public boolean canPublishProducts() {
        return this == APPROVED;
    }
}
