package application.domain.enums;

/**
 * Roles de usuario del sistema. Cada usuario tiene exactamente un rol.
 */
public enum UserRole {

    BUYER,
    SELLER,
    LOGISTIC_OPERATOR,
    ADMIN,
    SUPERVISOR;

    /**
     * Indica si el rol corresponde a personal interno de la plataforma
     * (no es comprador ni vendedor).
     */
    public boolean isStaff() {
        return this == LOGISTIC_OPERATOR || this == ADMIN || this == SUPERVISOR;
    }

    public static UserRole fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El rol es obligatorio");
        }
        try {
            return UserRole.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Rol inválido: " + value);
        }
    }
}
