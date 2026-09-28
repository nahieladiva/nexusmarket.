package application.infrastructure.config;

import application.domain.enums.UserRole;
import application.domain.models.User;
import application.domain.ports.out.UserRepository;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.IdentificationNumber;
import application.domain.valueobjects.PhoneNumber;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Crea el Administrador inicial al arrancar la aplicación si todavía no
 * existe ningún usuario con rol {@code ADMIN}. Sin él no se podrían registrar
 * vendedores ni personal interno, porque esas operaciones exigen un Administrador.
 */
@Component
public class AdminSeeder implements ApplicationRunner {

    private static final System.Logger LOGGER = System.getLogger(AdminSeeder.class.getName());

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String identification;
    private final String fullName;
    private final String email;
    private final String phone;
    private final String password;

    public AdminSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       @Value("${nexusmarket.admin.identification}") String identification,
                       @Value("${nexusmarket.admin.full-name}") String fullName,
                       @Value("${nexusmarket.admin.email}") String email,
                       @Value("${nexusmarket.admin.phone}") String phone,
                       @Value("${nexusmarket.admin.password}") String password) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.identification = identification;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        User admin = userRepository.findAll().stream()
            .filter(User::isAdmin)
            .findFirst()
            .orElseGet(this::createInitialAdmin);
        // El id se imprime en la consola para usarlo en la cabecera X-User-Id.
        LOGGER.log(System.Logger.Level.INFO,
            "Administrador inicial: id=" + admin.getId() + " email=" + admin.getEmail().getValue());
    }

    private User createInitialAdmin() {
        return userRepository.findByEmail(Email.of(email)).orElseGet(() -> userRepository.save(
            User.createStaff(
                IdentificationNumber.of(identification),
                fullName,
                Email.of(email),
                PhoneNumber.of(phone),
                UserRole.ADMIN,
                passwordEncoder.encode(password))));
    }
}
