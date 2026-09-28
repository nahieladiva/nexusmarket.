package application.domain.valueobjects;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador de devolución (UUID).
 */
public final class ReturnId {

    private final UUID value;

    public ReturnId(UUID value) {
        this.value = Objects.requireNonNull(value, "El id de devolución no puede ser nulo");
    }

    public static ReturnId of(UUID value) {
        return new ReturnId(value);
    }

    public static ReturnId of(String value) {
        return new ReturnId(UUID.fromString(value));
    }

    public static ReturnId random() {
        return new ReturnId(UUID.randomUUID());
    }

    public UUID getValue() {
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
        ReturnId idValue = (ReturnId) o;
        return value.equals(idValue.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}