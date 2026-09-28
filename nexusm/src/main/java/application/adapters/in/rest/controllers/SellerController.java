package application.adapters.in.rest.controllers;

import application.adapters.in.rest.mappers.ProductMapper;
import application.adapters.in.rest.requests.CreateProductRequest;
import application.adapters.in.rest.requests.PriceChangeRequest;
import application.adapters.in.rest.responses.ProductResponse;
import application.domain.enums.ProductType;
import application.domain.ports.in.SellerPort;
import application.domain.valueobjects.ProductCode;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.UserId;

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
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints del Vendedor. El vendedor se identifica con la cabecera {@code X-User-Id}.
 */
@RestController
@RequestMapping("/api/seller")
public class SellerController {

    private static final String USER = "X-User-Id";

    private final SellerPort sellerPort;
    private final ProductMapper productMapper;

    public SellerController(SellerPort sellerPort, ProductMapper productMapper) {
        this.sellerPort = sellerPort;
        this.productMapper = productMapper;
    }

    @PostMapping("/products")
    public ResponseEntity<ProductResponse> createProduct(@RequestHeader(USER) String sellerId,
                                                         @RequestBody CreateProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productMapper.toResponse(
            sellerPort.createProduct(UserId.of(sellerId), ProductCode.of(request.code()),
                request.name(), request.description(), productMapper.toDomain(request.price()),
                ProductType.fromString(request.type()))));
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductResponse>> findMyProducts(@RequestHeader(USER) String sellerId) {
        return ResponseEntity.ok(sellerPort.findMyProducts(UserId.of(sellerId)).stream()
            .map(productMapper::toResponse).toList());
    }

    @PatchMapping("/products/{id}/price")
    public ResponseEntity<ProductResponse> changePrice(@RequestHeader(USER) String sellerId,
                                                       @PathVariable String id,
                                                       @RequestBody PriceChangeRequest request) {
        return ResponseEntity.ok(productMapper.toResponse(sellerPort.changePrice(
            UserId.of(sellerId), ProductId.of(id), productMapper.toDomain(request))));
    }

    @PatchMapping("/products/{id}/suspend")
    public ResponseEntity<ProductResponse> suspend(@RequestHeader(USER) String sellerId,
                                                   @PathVariable String id) {
        return ResponseEntity.ok(productMapper.toResponse(
            sellerPort.suspendProduct(UserId.of(sellerId), ProductId.of(id))));
    }

    @PatchMapping("/products/{id}/publish")
    public ResponseEntity<ProductResponse> publish(@RequestHeader(USER) String sellerId,
                                                   @PathVariable String id) {
        return ResponseEntity.ok(productMapper.toResponse(
            sellerPort.publishProduct(UserId.of(sellerId), ProductId.of(id))));
    }

    @PatchMapping("/products/{id}/discontinue")
    public ResponseEntity<ProductResponse> discontinue(@RequestHeader(USER) String sellerId,
                                                       @PathVariable String id) {
        return ResponseEntity.ok(productMapper.toResponse(
            sellerPort.discontinueProduct(UserId.of(sellerId), ProductId.of(id))));
    }
}
