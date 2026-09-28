package application.domain.enums;

/**
 * Estado operativo de una bodega. Solo las bodegas activas
 * participan en la asignación de pedidos.
 */
public enum WarehouseStatus {

    ACTIVE,
    INACTIVE
}
