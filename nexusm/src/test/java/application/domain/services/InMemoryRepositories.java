package application.domain.services;

import application.domain.events.DomainEvent;
import application.domain.models.Cart;
import application.domain.models.Inventory;
import application.domain.models.Invoice;
import application.domain.models.Order;
import application.domain.models.Product;
import application.domain.models.Refund;
import application.domain.models.ReturnRequest;
import application.domain.models.Shipment;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.ports.out.AuditLogPort;
import application.domain.ports.out.CartRepository;
import application.domain.ports.out.InventoryRepository;
import application.domain.ports.out.InvoiceRepository;
import application.domain.ports.out.OrderRepository;
import application.domain.ports.out.ProductRepository;
import application.domain.ports.out.RefundRepository;
import application.domain.ports.out.ReturnRequestRepository;
import application.domain.ports.out.ShipmentRepository;
import application.domain.ports.out.UserRepository;
import application.domain.ports.out.WarehouseRepository;
import application.domain.valueobjects.CartId;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.IdentificationNumber;
import application.domain.valueobjects.InventoryId;
import application.domain.valueobjects.InvoiceId;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.ProductCode;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.RefundId;
import application.domain.valueobjects.ReturnId;
import application.domain.valueobjects.ShipmentId;
import application.domain.valueobjects.UserId;
import application.domain.valueobjects.WarehouseId;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementaciones en memoria de todos los puertos de salida, para probar
 * los servicios de dominio sin base de datos.
 */
public final class InMemoryRepositories {

    public final List<DomainEvent> auditLog = new ArrayList<>();

    public final UserRepository users = new UserRepository() {
        private final Map<UserId, User> store = new LinkedHashMap<>();
        public User save(User u) { store.put(u.getId(), u); return u; }
        public Optional<User> findById(UserId id) { return Optional.ofNullable(store.get(id)); }
        public Optional<User> findByEmail(Email e) {
            return store.values().stream().filter(u -> u.getEmail().equals(e)).findFirst(); }
        public boolean existsByEmail(Email e) { return findByEmail(e).isPresent(); }
        public boolean existsByIdentification(IdentificationNumber i) {
            return store.values().stream().anyMatch(u -> u.getIdentification().equals(i)); }
        public List<User> findAll() { return new ArrayList<>(store.values()); }
    };

    public final ProductRepository products = new ProductRepository() {
        private final Map<ProductId, Product> store = new LinkedHashMap<>();
        public Product save(Product p) { store.put(p.getId(), p); return p; }
        public Optional<Product> findById(ProductId id) { return Optional.ofNullable(store.get(id)); }
        public Optional<Product> findByCode(ProductCode c) {
            return store.values().stream().filter(p -> p.getCode().equals(c)).findFirst(); }
        public List<Product> findByNameContaining(String k) {
            return store.values().stream().filter(p -> p.getName().contains(k)).toList(); }
        public List<Product> findBySellerId(UserId s) {
            return store.values().stream().filter(p -> p.getSellerId().equals(s)).toList(); }
        public List<Product> findAll() { return new ArrayList<>(store.values()); }
    };

    public final WarehouseRepository warehouses = new WarehouseRepository() {
        private final Map<WarehouseId, Warehouse> store = new LinkedHashMap<>();
        public Warehouse save(Warehouse w) { store.put(w.getId(), w); return w; }
        public Optional<Warehouse> findById(WarehouseId id) { return Optional.ofNullable(store.get(id)); }
        public List<Warehouse> findByNameContaining(String k) {
            return store.values().stream().filter(w -> w.getName().contains(k)).toList(); }
        public List<Warehouse> findAll() { return new ArrayList<>(store.values()); }
    };

    public final InventoryRepository inventories = new InventoryRepository() {
        private final Map<InventoryId, Inventory> store = new LinkedHashMap<>();
        public Inventory save(Inventory i) { store.put(i.getId(), i); return i; }
        public Optional<Inventory> findById(InventoryId id) { return Optional.ofNullable(store.get(id)); }
        public Optional<Inventory> findByProductIdAndWarehouseId(ProductId p, WarehouseId w) {
            return store.values().stream()
                .filter(i -> i.getProductId().equals(p) && i.getWarehouseId().equals(w)).findFirst(); }
        public List<Inventory> findByWarehouseId(WarehouseId w) {
            return store.values().stream().filter(i -> i.getWarehouseId().equals(w)).toList(); }
        public List<Inventory> findByProductId(ProductId p) {
            return store.values().stream().filter(i -> i.getProductId().equals(p)).toList(); }
        public List<Inventory> findAll() { return new ArrayList<>(store.values()); }
    };

    public final OrderRepository orders = new OrderRepository() {
        private final Map<OrderId, Order> store = new LinkedHashMap<>();
        public Order save(Order o) { store.put(o.getId(), o); return o; }
        public Optional<Order> findById(OrderId id) { return Optional.ofNullable(store.get(id)); }
        public List<Order> findByBuyerId(UserId b) {
            return store.values().stream().filter(o -> o.getBuyerId().equals(b)).toList(); }
        public List<Order> findAll() { return new ArrayList<>(store.values()); }
    };

    public final CartRepository carts = new CartRepository() {
        private final Map<CartId, Cart> store = new LinkedHashMap<>();
        public Cart save(Cart c) { store.put(c.getId(), c); return c; }
        public Optional<Cart> findById(CartId id) { return Optional.ofNullable(store.get(id)); }
        public Optional<Cart> findByBuyerId(UserId b) {
            return store.values().stream().filter(c -> c.getBuyerId().equals(b)).findFirst(); }
    };

    public final InvoiceRepository invoices = new InvoiceRepository() {
        private final Map<InvoiceId, Invoice> store = new LinkedHashMap<>();
        public Invoice save(Invoice i) { store.put(i.getId(), i); return i; }
        public Optional<Invoice> findById(InvoiceId id) { return Optional.ofNullable(store.get(id)); }
        public Optional<Invoice> findByOrderId(OrderId o) {
            return store.values().stream().filter(i -> i.getOrderId().equals(o)).findFirst(); }
    };

    public final ShipmentRepository shipments = new ShipmentRepository() {
        private final Map<ShipmentId, Shipment> store = new LinkedHashMap<>();
        public Shipment save(Shipment s) { store.put(s.getId(), s); return s; }
        public Optional<Shipment> findById(ShipmentId id) { return Optional.ofNullable(store.get(id)); }
        public Optional<Shipment> findByOrderId(OrderId o) {
            return store.values().stream().filter(s -> s.getOrderId().equals(o)).findFirst(); }
        public List<Shipment> findAll() { return new ArrayList<>(store.values()); }
    };

    public final ReturnRequestRepository returns = new ReturnRequestRepository() {
        private final Map<ReturnId, ReturnRequest> store = new LinkedHashMap<>();
        public ReturnRequest save(ReturnRequest r) { store.put(r.getId(), r); return r; }
        public Optional<ReturnRequest> findById(ReturnId id) { return Optional.ofNullable(store.get(id)); }
        public List<ReturnRequest> findByOrderId(OrderId o) {
            return store.values().stream().filter(r -> r.getOrderId().equals(o)).toList(); }
        public List<ReturnRequest> findByBuyerId(UserId b) {
            return store.values().stream().filter(r -> r.getBuyerId().equals(b)).toList(); }
        public List<ReturnRequest> findAll() { return new ArrayList<>(store.values()); }
    };

    public final RefundRepository refunds = new RefundRepository() {
        private final Map<RefundId, Refund> store = new LinkedHashMap<>();
        public Refund save(Refund r) { store.put(r.getId(), r); return r; }
        public Optional<Refund> findById(RefundId id) { return Optional.ofNullable(store.get(id)); }
        public Optional<Refund> findByReturnId(ReturnId r) {
            return store.values().stream().filter(x -> x.getReturnId().equals(r)).findFirst(); }
    };

    public final AuditLogPort audit = auditLog::add;
}
