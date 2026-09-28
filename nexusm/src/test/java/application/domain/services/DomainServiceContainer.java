package application.domain.services;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Mini contenedor de inyección de dependencias para pruebas: construye
 * cualquier servicio de dominio resolviendo sus dependencias por
 * constructor, igual que lo hace Spring con {@code @DomainService}.
 * Los puertos de salida se toman de {@link InMemoryRepositories}.
 */
public final class DomainServiceContainer {

    private final Map<Class<?>, Object> instances = new HashMap<>();

    public DomainServiceContainer(InMemoryRepositories repositories) {
        for (Field field : InMemoryRepositories.class.getFields()) {
            try {
                Object value = field.get(repositories);
                instances.put(field.getType(), value);
            } catch (IllegalAccessException ex) {
                throw new IllegalStateException(ex);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> type) {
        Object existing = instances.get(type);
        if (existing != null) {
            return (T) existing;
        }
        if (!type.isAnnotationPresent(DomainService.class)) {
            throw new IllegalStateException("No hay implementación registrada para " + type.getName());
        }
        Constructor<?>[] constructors = type.getConstructors();
        if (constructors.length != 1) {
            throw new IllegalStateException(type.getSimpleName() + " debe tener un único constructor público");
        }
        Constructor<?> constructor = constructors[0];
        Object[] args = new Object[constructor.getParameterCount()];
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        for (int i = 0; i < args.length; i++) {
            args[i] = get(parameterTypes[i]);
        }
        try {
            T instance = (T) constructor.newInstance(args);
            instances.put(type, instance);
            return instance;
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("No se pudo crear " + type.getSimpleName(), ex);
        }
    }
}
