package application.adapters.in.rest.controllers;

import application.adapters.in.rest.mappers.ProductMapper;
import application.adapters.in.rest.mappers.UserMapper;
import application.adapters.in.rest.requests.CreateBuyerRequest;
import application.adapters.in.rest.responses.ProductResponse;
import application.adapters.in.rest.responses.UserResponse;
import application.domain.models.User;
import application.domain.ports.in.PublicAccessPort;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.IdentificationNumber;
import application.domain.valueobjects.PhoneNumber;
import application.domain.valueobjects.ProductId;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints públicos: registro de compradores y catálogo.
 */
@RestController
@RequestMapping("/api/public")
public class PublicController {

    private final PublicAccessPort publicAccessPort;
    private final UserMapper userMapper;
    private final ProductMapper productMapper;
    private final PasswordEncoder passwordEncoder;

    public PublicController(PublicAccessPort publicAccessPort, UserMapper userMapper,
                            ProductMapper productMapper, PasswordEncoder passwordEncoder) {
        this.publicAccessPort = publicAccessPort;
        this.userMapper = userMapper;
        this.productMapper = productMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/buyers")
    public ResponseEntity<UserResponse> registerBuyer(@RequestBody CreateBuyerRequest request) {
        User user = publicAccessPort.registerBuyer(
            IdentificationNumber.of(request.identification()),
            request.fullName(),
            Email.of(request.email()),
            PhoneNumber.of(request.phone()),
            passwordEncoder.encode(request.password()),
            userMapper.toDomain(request.defaultShippingAddress()));
        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toResponse(user));
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductResponse>> searchCatalog(
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(publicAccessPort.searchCatalog(keyword).stream()
            .map(productMapper::toResponse).toList());
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ProductResponse> findProduct(@PathVariable String id) {
        return ResponseEntity.ok(productMapper.toResponse(
            publicAccessPort.findProduct(ProductId.of(id))));
    }
}
