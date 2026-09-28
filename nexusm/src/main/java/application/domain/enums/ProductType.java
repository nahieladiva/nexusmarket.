package application.domain.enums;

/**
 * Tipo de producto del catálogo.
 */
public enum ProductType {

    PHYSICAL,
    DIGITAL;

    /**
     * Solo los productos físicos manejan inventario en bodega y envío.
     */
    public boolean requiresInventory() {
        return this == PHYSICAL;
    }

    public static ProductType fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El tipo de producto es obligatorio");
        }
        try {
            return ProductType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Tipo de producto inválido: " + value);
        }
    }
}
