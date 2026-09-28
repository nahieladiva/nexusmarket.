package application.adapters.useCases;

import application.domain.models.Inventory;
import application.domain.models.Order;
import application.domain.models.ReturnRequest;
import application.domain.models.Shipment;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.ports.in.LogisticOperatorPort;
import application.domain.services.authorization.AuthorizeLogisticOperationService;
import application.domain.services.inventory.AdjustStockService;
import application.domain.services.inventory.ConsultInventoryService;
import application.domain.services.inventory.CreateInventoryService;
import application.domain.services.inventory.ReceiveStockService;
import application.domain.services.order.ConsultOrderService;
import application.domain.services.returns.ReceiveReturnService;
import application.domain.services.shipment.ConfirmDeliveryService;
import application.domain.services.shipment.CreateShipmentService;
import application.domain.services.shipment.DispatchShipmentService;
import application.domain.services.user.ConsultUserService;
import application.domain.services.warehouse.ConsultWarehouseService;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;
import application.domain.valueobjects.ReturnId;
import application.domain.valueobjects.ShipmentId;
import application.domain.valueobjects.UserId;
import application.domain.valueobjects.WarehouseId;
import application.domain.valueobjects.WarehouseLocation;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementa {@link LogisticOperatorPort}: carga al operador y delega en los servicios de dominio.
 */
@Service
@Transactional
public class LogisticOperatorUseCaseImpl implements LogisticOperatorPort {

    private final ConsultUserService consultUserService;
    private final AuthorizeLogisticOperationService authorizeLogisticOperationService;
    private final CreateInventoryService createInventoryService;
    private final ReceiveStockService receiveStockService;
    private final AdjustStockService adjustStockService;
    private final ConsultInventoryService consultInventoryService;
    private final ConsultWarehouseService consultWarehouseService;
    private final ConsultOrderService consultOrderService;
    private final CreateShipmentService createShipmentService;
    private final DispatchShipmentService dispatchShipmentService;
    private final ConfirmDeliveryService confirmDeliveryService;
    private final ReceiveReturnService receiveReturnService;

    public LogisticOperatorUseCaseImpl(ConsultUserService consultUserService,
                                       AuthorizeLogisticOperationService authorizeLogisticOperationService,
                                       CreateInventoryService createInventoryService,
                                       ReceiveStockService receiveStockService,
                                       AdjustStockService adjustStockService,
                                       ConsultInventoryService consultInventoryService,
                                       ConsultWarehouseService consultWarehouseService,
                                       ConsultOrderService consultOrderService,
                                       CreateShipmentService createShipmentService,
                                       DispatchShipmentService dispatchShipmentService,
                                       ConfirmDeliveryService confirmDeliveryService,
                                       ReceiveReturnService receiveReturnService) {
        this.consultUserService = consultUserService;
        this.authorizeLogisticOperationService = authorizeLogisticOperationService;
        this.createInventoryService = createInventoryService;
        this.receiveStockService = receiveStockService;
        this.adjustStockService = adjustStockService;
        this.consultInventoryService = consultInventoryService;
        this.consultWarehouseService = consultWarehouseService;
        this.consultOrderService = consultOrderService;
        this.createShipmentService = createShipmentService;
        this.dispatchShipmentService = dispatchShipmentService;
        this.confirmDeliveryService = confirmDeliveryService;
        this.receiveReturnService = receiveReturnService;
    }

    /** Carga al usuario y verifica que sea un Operador Logístico activo. */
    private User operator(UserId operatorId) {
        User operator = consultUserService.findById(operatorId);
        authorizeLogisticOperationService.execute(operator);
        return operator;
    }

    @Override
    public Inventory createInventory(UserId operatorId, ProductId productId, WarehouseId warehouseId,
                                     Quantity initialStock, Quantity reorderThreshold,
                                     WarehouseLocation location) {
        return createInventoryService.execute(operator(operatorId), productId, warehouseId,
            initialStock, reorderThreshold, location);
    }

    @Override
    public Inventory receiveStock(UserId operatorId, ProductId productId, WarehouseId warehouseId,
                                  Quantity quantity) {
        return receiveStockService.execute(operator(operatorId), productId, warehouseId, quantity);
    }

    @Override
    public Inventory adjustStock(UserId operatorId, ProductId productId, WarehouseId warehouseId,
                                 int delta, String reason) {
        return adjustStockService.execute(operator(operatorId), productId, warehouseId, delta, reason);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Inventory> findInventoryByWarehouse(UserId operatorId, WarehouseId warehouseId) {
        return consultInventoryService.findByWarehouse(operator(operatorId), warehouseId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Inventory> findInventoryByProduct(UserId operatorId, ProductId productId) {
        return consultInventoryService.findByProduct(operator(operatorId), productId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Warehouse> findWarehouses(UserId operatorId) {
        return consultWarehouseService.findAll(operator(operatorId));
    }

    @Override
    @Transactional(readOnly = true)
    public Order findOrder(UserId operatorId, OrderId orderId) {
        return consultOrderService.findById(operator(operatorId), orderId);
    }

    @Override
    public Shipment createShipment(UserId operatorId, OrderId orderId) {
        return createShipmentService.execute(operator(operatorId), orderId);
    }

    @Override
    public Shipment dispatchShipment(UserId operatorId, ShipmentId shipmentId, String trackingNumber) {
        return dispatchShipmentService.execute(operator(operatorId), shipmentId, trackingNumber);
    }

    @Override
    public Shipment confirmDelivery(UserId operatorId, ShipmentId shipmentId) {
        return confirmDeliveryService.execute(operator(operatorId), shipmentId);
    }

    @Override
    public ReturnRequest receiveReturn(UserId operatorId, ReturnId returnId) {
        return receiveReturnService.execute(operator(operatorId), returnId);
    }
}
