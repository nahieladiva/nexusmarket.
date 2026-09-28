package application.domain.models;

import application.domain.enums.UserRole;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.IdentificationNumber;
import application.domain.valueobjects.PhoneNumber;

/**
 * Datos de prueba reutilizables para las pruebas del dominio.
 */
public final class TestData {

    private TestData() {
    }

    public static Address address() {
        return Address.of("Calle 10 # 20-30", "Medellín", "Antioquia", "050001", "CO");
    }

    public static Buyer buyer(String id, String email) {
        return Buyer.create(IdentificationNumber.of(id), "Comprador Prueba", Email.of(email),
            PhoneNumber.of("+573001112233"), "hash", address());
    }

    public static Seller seller(String id, String email) {
        return Seller.create(IdentificationNumber.of(id), "Vendedor Prueba", Email.of(email),
            PhoneNumber.of("+573004445566"), "hash", "Tienda Prueba");
    }

    public static User staff(String id, String email, UserRole role) {
        return User.createStaff(IdentificationNumber.of(id), "Staff Prueba", Email.of(email),
            PhoneNumber.of("+573007778899"), role, "hash");
    }
}
