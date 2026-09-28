package application.adapters.in.rest.controllers;

import application.adapters.in.rest.mappers.OrderMapper;
import application.adapters.in.rest.mappers.PostSaleMapper;
import application.adapters.in.rest.mappers.ProductMapper;
import application.adapters.in.rest.mappers.UserMapper;
import application.adapters.in.rest.responses.OrderResponse;
import application.adapters.in.rest.responses.ProductResponse;
import application.adapters.in.rest.responses.RefundResponse;
import application.adapters.in.rest.responses.ReturnResponse;
import application.adapters.in.rest.responses.UserResponse;
import application.domain.ports.in.SupervisorPort;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.ReturnId;
import application.domain.valueobjects.UserId;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints de supervisión (Supervisor o Administrador). Se identifica con
 * la cabecera {@code X-User-Id}.
 */
@RestController
@RequestMapping("/api/supervisor")
public class SupervisorController {

    private static final String USER = "X-User-Id";

    private final SupervisorPort supervisorPort;
    private final UserMapper userMapper;
    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;
    private final PostSaleMapper postSaleMapper;

    public SupervisorController(SupervisorPort supervisorPort, UserMapper userMapper,
                                OrderMapper orderMapper, ProductMapper productMapper,
                                PostSaleMapper postSaleMapper) {
        this.supervisorPort = supervisorPort;
        this.userMapper = userMapper;
        this.orderMapper = orderMapper;
        this.productMapper = productMapper;
        this.postSaleMapper = postSaleMapper;
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> findAllUsers(@RequestHeader(USER) String actorId) {
        return ResponseEntity.ok(supervisorPort.findAllUsers(UserId.of(actorId)).stream()
            .map(userMapper::toResponse).toList());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> findUser(@RequestHeader(USER) String actorId,
                                                 @PathVariable String id) {
        return ResponseEntity.ok(userMapper.toResponse(
            supervisorPort.findUser(UserId.of(actorId), UserId.of(id))));
    }

    @PatchMapping("/users/{id}/block")
    public ResponseEntity<UserResponse> blockUser(@RequestHeader(USER) String actorId,
                                                  @PathVariable String id) {
        return ResponseEntity.ok(userMapper.toResponse(
            supervisorPort.blockUser(UserId.of(actorId), UserId.of(id))));
    }

    @PatchMapping("/users/{id}/activate")
    public ResponseEntity<UserResponse> activateUser(@RequestHeader(USER) String actorId,
                                                     @PathVariable String id) {
        return ResponseEntity.ok(userMapper.toResponse(
            supervisorPort.activateUser(UserId.of(actorId), UserId.of(id))));
    }

    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponse>> findAllOrders(@RequestHeader(USER) String actorId) {
        return ResponseEntity.ok(supervisorPort.findAllOrders(UserId.of(actorId)).stream()
            .map(orderMapper::toResponse).toList());
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<OrderResponse> findOrder(@RequestHeader(USER) String actorId,
                                                   @PathVariable String id) {
        return ResponseEntity.ok(orderMapper.toResponse(
            supervisorPort.findOrder(UserId.of(actorId), OrderId.of(id))));
    }

    @PatchMapping("/orders/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@RequestHeader(USER) String actorId,
                                                     @PathVariable String id) {
        return ResponseEntity.ok(orderMapper.toResponse(
            supervisorPort.cancelOrder(UserId.of(actorId), OrderId.of(id))));
    }

    @PatchMapping("/products/{id}/suspend")
    public ResponseEntity<ProductResponse> suspendProduct(@RequestHeader(USER) String actorId,
                                                          @PathVariable String id) {
        return ResponseEntity.ok(productMapper.toResponse(
            supervisorPort.suspendProduct(UserId.of(actorId), ProductId.of(id))));
    }

    @GetMapping("/returns")
    public ResponseEntity<List<ReturnResponse>> findAllReturns(@RequestHeader(USER) String actorId) {
        return ResponseEntity.ok(supervisorPort.findAllReturns(UserId.of(actorId)).stream()
            .map(postSaleMapper::toResponse).toList());
    }

    @PatchMapping("/returns/{id}/approve")
    public ResponseEntity<ReturnResponse> approveReturn(@RequestHeader(USER) String actorId,
                                                        @PathVariable String id) {
        return ResponseEntity.ok(postSaleMapper.toResponse(
            supervisorPort.approveReturn(UserId.of(actorId), ReturnId.of(id))));
    }

    @PatchMapping("/returns/{id}/reject")
    public ResponseEntity<ReturnResponse> rejectReturn(@RequestHeader(USER) String actorId,
                                                       @PathVariable String id) {
        return ResponseEntity.ok(postSaleMapper.toResponse(
            supervisorPort.rejectReturn(UserId.of(actorId), ReturnId.of(id))));
    }

    @PostMapping("/returns/{id}/refund")
    public ResponseEntity<RefundResponse> processRefund(@RequestHeader(USER) String actorId,
                                                        @PathVariable String id) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postSaleMapper.toResponse(
            supervisorPort.processRefund(UserId.of(actorId), ReturnId.of(id))));
    }
}
