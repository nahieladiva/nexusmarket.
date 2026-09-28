package application.domain.ports.in;

import application.domain.models.Inventory;
import application.domain.models.Order;
import application.domain.models.ReturnRequest;
import application.domain.models.Shipment;
import application.domain.models.Warehouse;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;
import application.domain.valueobjects.ReturnId;
import application.domain.valueobjects.ShipmentId;
import application.domain.valueobjects.UserId;
import application.domain.valueobjects.WarehouseId;
import application.domain.valueobjects.WarehouseLocation;

import java.util.List;

/**
 * Casos de uso del Operador Logístico: inventario, envíos y recepción de devoluciones.
 */
public interface LogisticOperatorPort {

    Inventory createInventory(UserId operatorId, ProductId productId, WarehouseId warehouseId,
                              Quantity initialStock, Quantity reorderThreshold,
                              WarehouseLocation location);

    Inventory receiveStock(UserId operatorId, ProductId productId, WarehouseId warehouseId,
                           Quantity quantity);

    Inventory adjustStock(UserId operatorId, ProductId productId, WarehouseId warehouseId,
                          int delta, String reason);

    List<Inventory> findInventoryByWarehouse(UserId operatorId, WarehouseId warehouseId);

    List<Inventory> findInventoryByProduct(UserId operatorId, ProductId productId);

    List<Warehouse> findWarehouses(UserId operatorId);

    Order findOrder(UserId operatorId, OrderId orderId);

    Shipment createShipment(UserId operatorId, OrderId orderId);

    Shipment dispatchShipment(UserId operatorId, ShipmentId shipmentId, String trackingNumber);

    Shipment confirmDelivery(UserId operatorId, ShipmentId shipmentId);

    ReturnRequest receiveReturn(UserId operatorId, ReturnId returnId);
}
