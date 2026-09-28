package application.domain.models;

import application.domain.enums.WarehouseStatus;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.WarehouseId;
import application.domain.valueobjects.WarehouseLocation;

import java.util.Objects;

/**
 * Bodega física donde se almacena el inventario distribuido.
 * Solo las bodegas {@code ACTIVE} participan en la asignación de pedidos.
 */
public final class Warehouse {

    private final WarehouseId id;
    private String name;
    private Address address;
    private WarehouseLocation location;
    private WarehouseStatus status;

    public Warehouse(WarehouseId id, String name, Address address,
                     WarehouseLocation location, WarehouseStatus status) {
        this.id = Objects.requireNonNull(id, "El id de almacén es obligatorio");
        this.name = requireNotBlank(name, "El nombre del almacén es obligatorio");
        this.address = Objects.requireNonNull(address, "La dirección es obligatoria");
        this.location = Objects.requireNonNull(location, "La ubicación es obligatoria");
        this.status = status == null ? WarehouseStatus.ACTIVE : status;
    }

    public static Warehouse create(String name, Address address, WarehouseLocation location) {
        return new Warehouse(WarehouseId.random(), name, address, location, WarehouseStatus.ACTIVE);
    }

    private static String requireNotBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    public void changeName(String newName) {
        this.name = requireNotBlank(newName, "El nombre del almacén es obligatorio");
    }

    public void changeAddress(Address newAddress) {
        this.address = Objects.requireNonNull(newAddress, "La dirección es obligatoria");
    }

    public void changeLocation(WarehouseLocation newLocation) {
        this.location = Objects.requireNonNull(newLocation, "La ubicación es obligatoria");
    }

    public void activate() {
        this.status = WarehouseStatus.ACTIVE;
    }

    public void deactivate() {
        this.status = WarehouseStatus.INACTIVE;
    }

    public boolean isActive() {
        return status == WarehouseStatus.ACTIVE;
    }

    public WarehouseId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Address getAddress() {
        return address;
    }

    public WarehouseLocation getLocation() {
        return location;
    }

    public WarehouseStatus getStatus() {
        return status;
    }
}
