package application.adapters.in.rest.controllers;

import application.adapters.in.rest.mappers.InventoryMapper;
import application.adapters.in.rest.mappers.OrderMapper;
import application.adapters.in.rest.mappers.PostSaleMapper;
import application.adapters.in.rest.mappers.WarehouseMapper;
import application.adapters.in.rest.requests.AdjustInventoryRequest;
import application.adapters.in.rest.requests.CreateInventoryRequest;
import application.adapters.in.rest.requests.CreateShipmentRequest;
import application.adapters.in.rest.requests.DispatchShipmentRequest;
import application.adapters.in.rest.requests.ReceiveStockRequest;
import application.adapters.in.rest.responses.InventoryResponse;
import application.adapters.in.rest.responses.OrderResponse;
import application.adapters.in.rest.responses.ReturnResponse;
import application.adapters.in.rest.responses.ShipmentResponse;
import application.adapters.in.rest.responses.WarehouseResponse;
import application.domain.ports.in.LogisticOperatorPort;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;
import application.domain.valueobjects.ReturnId;
import application.domain.valueobjects.ShipmentId;
import application.domain.valueobjects.UserId;
import application.domain.valueobjects.WarehouseId;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints del Operador Logístico. Se identifica con la cabecera {@code X-User-Id}.
 */
@RestController
@RequestMapping("/api/logistics")
public class LogisticsController {

    private static final String USER = "X-User-Id";

    private final LogisticOperatorPort logisticOperatorPort;
    private final InventoryMapper inventoryMapper;
    private final WarehouseMapper warehouseMapper;
    private final OrderMapper orderMapper;
    private final PostSaleMapper postSaleMapper;

    public LogisticsController(LogisticOperatorPort logisticOperatorPort,
                               InventoryMapper inventoryMapper, WarehouseMapper warehouseMapper,
                               OrderMapper orderMapper, PostSaleMapper postSaleMapper) {
        this.logisticOperatorPort = logisticOperatorPort;
        this.inventoryMapper = inventoryMapper;
        this.warehouseMapper = warehouseMapper;
        this.orderMapper = orderMapper;
        this.postSaleMapper = postSaleMapper;
    }

    @PostMapping("/inventory")
    public ResponseEntity<InventoryResponse> createInventory(@RequestHeader(USER) String operatorId,
                                                             @RequestBody CreateInventoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryMapper.toResponse(
            logisticOperatorPort.createInventory(UserId.of(operatorId),
                ProductId.of(request.productId()), WarehouseId.of(request.warehouseId()),
                Quantity.of(request.onHand()), Quantity.of(request.reorderThreshold()),
                inventoryMapper.toDomain(request.location()))));
    }

    @PatchMapping("/inventory/receive")
    public ResponseEntity<InventoryResponse> receiveStock(@RequestHeader(USER) String operatorId,
                                                          @RequestBody ReceiveStockRequest request) {
        return ResponseEntity.ok(inventoryMapper.toResponse(logisticOperatorPort.receiveStock(
            UserId.of(operatorId), ProductId.of(request.productId()),
            WarehouseId.of(request.warehouseId()), Quantity.of(request.quantity()))));
    }

    @PatchMapping("/inventory/adjust")
    public ResponseEntity<InventoryResponse> adjustStock(@RequestHeader(USER) String operatorId,
                                                         @RequestBody AdjustInventoryRequest request) {
        return ResponseEntity.ok(inventoryMapper.toResponse(logisticOperatorPort.adjustStock(
            UserId.of(operatorId), ProductId.of(request.productId()),
            WarehouseId.of(request.warehouseId()), request.deltaQuantity(), request.reason())));
    }

    @GetMapping("/inventory")
    public ResponseEntity<List<InventoryResponse>> findInventory(
            @RequestHeader(USER) String operatorId,
            @RequestParam(required = false) String warehouseId,
            @RequestParam(required = false) String productId) {
        UserId operator = UserId.of(operatorId);
        if (warehouseId != null && !warehouseId.isBlank()) {
            return ResponseEntity.ok(logisticOperatorPort.findInventoryByWarehouse(operator,
                WarehouseId.of(warehouseId)).stream().map(inventoryMapper::toResponse).toList());
        }
        if (productId != null && !productId.isBlank()) {
            return ResponseEntity.ok(logisticOperatorPort.findInventoryByProduct(operator,
                ProductId.of(productId)).stream().map(inventoryMapper::toResponse).toList());
        }
        return ResponseEntity.badRequest().build();
    }

    @GetMapping("/warehouses")
    public ResponseEntity<List<WarehouseResponse>> findWarehouses(@RequestHeader(USER) String operatorId) {
        return ResponseEntity.ok(logisticOperatorPort.findWarehouses(UserId.of(operatorId)).stream()
            .map(warehouseMapper::toResponse).toList());
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<OrderResponse> findOrder(@RequestHeader(USER) String operatorId,
                                                   @PathVariable String id) {
        return ResponseEntity.ok(orderMapper.toResponse(
            logisticOperatorPort.findOrder(UserId.of(operatorId), OrderId.of(id))));
    }

    @PostMapping("/shipments")
    public ResponseEntity<ShipmentResponse> createShipment(@RequestHeader(USER) String operatorId,
                                                           @RequestBody CreateShipmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postSaleMapper.toResponse(
            logisticOperatorPort.createShipment(UserId.of(operatorId), OrderId.of(request.orderId()))));
    }

    @PatchMapping("/shipments/{id}/dispatch")
    public ResponseEntity<ShipmentResponse> dispatch(@RequestHeader(USER) String operatorId,
                                                     @PathVariable String id,
                                                     @RequestBody DispatchShipmentRequest request) {
        return ResponseEntity.ok(postSaleMapper.toResponse(logisticOperatorPort.dispatchShipment(
            UserId.of(operatorId), ShipmentId.of(id), request.trackingNumber())));
    }

    @PatchMapping("/shipments/{id}/deliver")
    public ResponseEntity<ShipmentResponse> deliver(@RequestHeader(USER) String operatorId,
                                                    @PathVariable String id) {
        return ResponseEntity.ok(postSaleMapper.toResponse(
            logisticOperatorPort.confirmDelivery(UserId.of(operatorId), ShipmentId.of(id))));
    }

    @PatchMapping("/returns/{id}/receive")
    public ResponseEntity<ReturnResponse> receiveReturn(@RequestHeader(USER) String operatorId,
                                                        @PathVariable String id) {
        return ResponseEntity.ok(postSaleMapper.toResponse(
            logisticOperatorPort.receiveReturn(UserId.of(operatorId), ReturnId.of(id))));
    }
}
