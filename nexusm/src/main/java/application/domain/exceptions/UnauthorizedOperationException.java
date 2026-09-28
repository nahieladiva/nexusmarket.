package application.domain.exceptions;

/**
 * Se lanza cuando un usuario intenta ejecutar una operación que su rol
 * o su estado no le permiten.
 */
public class UnauthorizedOperationException extends DomainException {

    public UnauthorizedOperationException(String message) {
        super(message);
    }
}
