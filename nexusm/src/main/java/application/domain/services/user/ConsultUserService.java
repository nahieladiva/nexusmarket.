package application.domain.services.user;

import application.domain.exceptions.ResourceNotFoundException;
import application.domain.models.User;
import application.domain.ports.out.UserRepository;
import application.domain.services.DomainService;
import application.domain.services.authorization.AuthorizeSupervisionOperationService;
import application.domain.valueobjects.UserId;

import java.util.List;

/**
 * Consulta de usuarios. El listado completo solo lo ven Administrador y Supervisor.
 */
@DomainService
public class ConsultUserService {

    private final UserRepository userRepository;
    private final AuthorizeSupervisionOperationService authorizeSupervisionOperationService;

    public ConsultUserService(UserRepository userRepository,
                              AuthorizeSupervisionOperationService authorizeSupervisionOperationService) {
        this.userRepository = userRepository;
        this.authorizeSupervisionOperationService = authorizeSupervisionOperationService;
    }

    public User findById(UserId id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario", id.toString()));
    }

    /** Consulta de un usuario por parte de un Administrador o Supervisor. */
    public User findById(User actor, UserId id) {
        authorizeSupervisionOperationService.execute(actor);
        return findById(id);
    }

    public List<User> findAll(User actor) {
        authorizeSupervisionOperationService.execute(actor);
        return userRepository.findAll();
    }
}
