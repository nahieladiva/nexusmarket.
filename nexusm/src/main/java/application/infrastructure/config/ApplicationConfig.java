package application.infrastructure.config;

import application.domain.services.DomainService;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

/**
 * Configuración de la aplicación.
 *
 * <p>Los servicios de dominio no usan anotaciones de Spring (el dominio queda
 * libre de frameworks). Están marcados con la anotación propia
 * {@link DomainService}, y este escaneo los registra como beans de Spring.</p>
 */
@Configuration
@ComponentScan(
    basePackages = "application.domain.services",
    includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = DomainService.class))
public class ApplicationConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
