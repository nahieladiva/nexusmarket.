package application.domain.services;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca un servicio de dominio (un caso de uso de negocio).
 *
 * <p>Es una anotación propia del dominio, en Java puro: así el dominio no
 * depende de Spring. La infraestructura ({@code ApplicationConfig}) escanea
 * esta anotación y registra cada servicio como bean.</p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DomainService {
}
