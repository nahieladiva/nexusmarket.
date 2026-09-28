package application.domain.services.user;

import application.domain.exceptions.UserAlreadyExistsException;
import application.domain.ports.out.UserRepository;
import application.domain.services.DomainService;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.IdentificationNumber;

/**
 * Regla R1: el correo y la identificación son únicos en toda la plataforma.
 */
@DomainService
public class ValidateUserUniquenessService {

    private final UserRepository userRepository;

    public ValidateUserUniquenessService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void execute(Email email, IdentificationNumber identification) {
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("el correo", email.getValue());
        }
        if (userRepository.existsByIdentification(identification)) {
            throw new UserAlreadyExistsException("la identificación", identification.getValue());
        }
    }
}
