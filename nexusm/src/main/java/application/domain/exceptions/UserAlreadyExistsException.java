package application.domain.exceptions;

/**
 * Se lanza cuando se intenta registrar un usuario con un correo
 * o número de identificación que ya existe en la plataforma.
 */
public class UserAlreadyExistsException extends DomainException {

    public UserAlreadyExistsException(String field, String value) {
        super("Ya existe un usuario registrado con " + field + ": " + value);
    }
}
