package application.domain.valueobjects;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Número de identificación de un usuario (cédula, NIT, pasaporte).
 * Es único en toda la plataforma.
 *
 * <p>Reglas: obligatorio, sin espacios, entre 5 y 20 caracteres
 * alfanuméricos (se permite el guion para NIT con dígito de verificación).</p>
 */
public final class IdentificationNumber {

    private static final Pattern FORMAT = Pattern.compile("^[A-Za-z0-9-]{5,20}$");

    private final String value;

    public IdentificationNumber(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El número de identificación es obligatorio");
        }
        String normalized = value.trim().toUpperCase();
        if (!FORMAT.matcher(normalized).matches()) {
            throw new IllegalArgumentException(
                "Número de identificación inválido (5 a 20 caracteres alfanuméricos): " + value);
        }
        this.value = normalized;
    }

    public static IdentificationNumber of(String value) {
        return new IdentificationNumber(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IdentificationNumber that = (IdentificationNumber) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
