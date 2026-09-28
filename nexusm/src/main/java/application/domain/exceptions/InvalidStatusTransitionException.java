package application.domain.exceptions;

/**
 * Se lanza cuando una entidad intenta pasar a un estado no permitido
 * (producto, envío, factura, devolución, reembolso, usuario, vendedor).
 */
public class InvalidStatusTransitionException extends DomainException {

    private final String entity;
    private final String current;
    private final String target;

    public InvalidStatusTransitionException(String entity, Enum<?> current, Enum<?> target) {
        super("Transición de estado no permitida para " + entity + ": "
            + current + " -> " + target);
        this.entity = entity;
        this.current = String.valueOf(current);
        this.target = String.valueOf(target);
    }

    public String getEntity() {
        return entity;
    }

    public String getCurrent() {
        return current;
    }

    public String getTarget() {
        return target;
    }
}
