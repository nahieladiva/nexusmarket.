package application.domain.enums;

/**
 * Estado de un usuario en la plataforma.
 */
public enum UserStatus {

    ACTIVE,
    BLOCKED;

    public static UserStatus fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El estado del usuario es obligatorio");
        }
        try {
            return UserStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Estado de usuario inválido: " + value);
        }
    }
}
