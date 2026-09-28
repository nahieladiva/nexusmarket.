package application.adapters.in.rest.controllers;

import application.adapters.in.rest.mappers.CartMapper;
import application.adapters.in.rest.mappers.OrderMapper;
import application.adapters.in.rest.mappers.PostSaleMapper;
import application.adapters.in.rest.requests.CartItemRequest;
import application.adapters.in.rest.requests.CreateReturnRequest;
import application.adapters.in.rest.requests.QuantityRequest;
import application.adapters.in.rest.responses.CartResponse;
import application.adapters.in.rest.responses.InvoiceResponse;
import application.adapters.in.rest.responses.OrderResponse;
import application.adapters.in.rest.responses.RefundResponse;
import application.adapters.in.rest.responses.ReturnResponse;
import application.adapters.in.rest.responses.ShipmentResponse;
import application.domain.ports.in.BuyerPort;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;
import application.domain.valueobjects.ReturnId;
import application.domain.valueobjects.UserId;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints del Comprador. El comprador se identifica con la cabecera {@code X-User-Id}.
 */
@RestController
@RequestMapping("/api/buyer")
public class BuyerController {

    private static final String USER = "X-User-Id";

    private final BuyerPort buyerPort;
    private final CartMapper cartMapper;
    private final OrderMapper orderMapper;
    private final PostSaleMapper postSaleMapper;

    public BuyerController(BuyerPort buyerPort, CartMapper cartMapper, OrderMapper orderMapper,
                           PostSaleMapper postSaleMapper) {
        this.buyerPort = buyerPort;
        this.cartMapper = cartMapper;
        this.orderMapper = orderMapper;
        this.postSaleMapper = postSaleMapper;
    }

    @GetMapping("/cart")
    public ResponseEntity<CartResponse> getCart(@RequestHeader(USER) String buyerId) {
        return ResponseEntity.ok(cartMapper.toResponse(buyerPort.getCart(UserId.of(buyerId))));
    }

    @PostMapping("/cart/items")
    public ResponseEntity<CartResponse> addToCart(@RequestHeader(USER) String buyerId,
                                                  @RequestBody CartItemRequest request) {
        return ResponseEntity.ok(cartMapper.toResponse(buyerPort.addToCart(UserId.of(buyerId),
            ProductId.of(request.productId()), Quantity.of(request.quantity()))));
    }

    @PatchMapping("/cart/items/{productId}")
    public ResponseEntity<CartResponse> changeQuantity(@RequestHeader(USER) String buyerId,
                                                       @PathVariable String productId,
                                                       @RequestBody QuantityRequest request) {
        return ResponseEntity.ok(cartMapper.toResponse(buyerPort.changeCartItemQuantity(
            UserId.of(buyerId), ProductId.of(productId), Quantity.of(request.quantity()))));
    }

    @DeleteMapping("/cart/items/{productId}")
    public ResponseEntity<CartResponse> removeFromCart(@RequestHeader(USER) String buyerId,
                                                       @PathVariable String productId) {
        return ResponseEntity.ok(cartMapper.toResponse(
            buyerPort.removeFromCart(UserId.of(buyerId), ProductId.of(productId))));
    }

    @PostMapping("/orders")
    public ResponseEntity<OrderResponse> placeOrder(@RequestHeader(USER) String buyerId) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(orderMapper.toResponse(buyerPort.placeOrder(UserId.of(buyerId))));
    }

    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponse>> findMyOrders(@RequestHeader(USER) String buyerId) {
        return ResponseEntity.ok(buyerPort.findMyOrders(UserId.of(buyerId)).stream()
            .map(orderMapper::toResponse).toList());
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<OrderResponse> findOrder(@RequestHeader(USER) String buyerId,
                                                   @PathVariable String id) {
        return ResponseEntity.ok(orderMapper.toResponse(
            buyerPort.findOrder(UserId.of(buyerId), OrderId.of(id))));
    }

    @PatchMapping("/orders/{id}/pay")
    public ResponseEntity<OrderResponse> payOrder(@RequestHeader(USER) String buyerId,
                                                  @PathVariable String id) {
        return ResponseEntity.ok(orderMapper.toResponse(
            buyerPort.payOrder(UserId.of(buyerId), OrderId.of(id))));
    }

    @PatchMapping("/orders/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@RequestHeader(USER) String buyerId,
                                                     @PathVariable String id) {
        return ResponseEntity.ok(orderMapper.toResponse(
            buyerPort.cancelOrder(UserId.of(buyerId), OrderId.of(id))));
    }

    @GetMapping("/orders/{id}/invoice")
    public ResponseEntity<InvoiceResponse> findInvoice(@RequestHeader(USER) String buyerId,
                                                       @PathVariable String id) {
        return ResponseEntity.ok(postSaleMapper.toResponse(
            buyerPort.findInvoice(UserId.of(buyerId), OrderId.of(id))));
    }

    @GetMapping("/orders/{id}/shipment")
    public ResponseEntity<ShipmentResponse> findShipment(@RequestHeader(USER) String buyerId,
                                                         @PathVariable String id) {
        return ResponseEntity.ok(postSaleMapper.toResponse(
            buyerPort.findShipment(UserId.of(buyerId), OrderId.of(id))));
    }

    @PostMapping("/returns")
    public ResponseEntity<ReturnResponse> requestReturn(@RequestHeader(USER) String buyerId,
                                                        @RequestBody CreateReturnRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postSaleMapper.toResponse(
            buyerPort.requestReturn(UserId.of(buyerId), OrderId.of(request.orderId()),
                ProductId.of(request.productId()), Quantity.of(request.quantity()),
                request.reason())));
    }

    @GetMapping("/returns")
    public ResponseEntity<List<ReturnResponse>> findMyReturns(@RequestHeader(USER) String buyerId) {
        return ResponseEntity.ok(buyerPort.findMyReturns(UserId.of(buyerId)).stream()
            .map(postSaleMapper::toResponse).toList());
    }

    @GetMapping("/returns/{id}/refund")
    public ResponseEntity<RefundResponse> findRefund(@RequestHeader(USER) String buyerId,
                                                     @PathVariable String id) {
        return ResponseEntity.ok(postSaleMapper.toResponse(
            buyerPort.findRefund(UserId.of(buyerId), ReturnId.of(id))));
    }
}
