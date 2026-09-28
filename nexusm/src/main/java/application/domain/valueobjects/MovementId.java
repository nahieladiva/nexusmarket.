package application.domain.valueobjects;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador de movimiento de inventario (UUID).
 */
public final class MovementId {

    private final UUID value;

    public MovementId(UUID value) {
        this.value = Objects.requireNonNull(value, "El id de movimiento de inventario no puede ser nulo");
    }

    public static MovementId of(UUID value) {
        return new MovementId(value);
    }

    public static MovementId of(String value) {
        return new MovementId(UUID.fromString(value));
    }

    public static MovementId random() {
        return new MovementId(UUID.randomUUID());
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
        MovementId idValue = (MovementId) o;
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