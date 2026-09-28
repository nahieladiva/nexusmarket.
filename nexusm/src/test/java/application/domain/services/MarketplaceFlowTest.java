package application.domain.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import application.domain.enums.InvoiceStatus;
import application.domain.enums.OperationType;
import application.domain.enums.OrderStatus;
import application.domain.enums.ProductType;
import application.domain.enums.RefundStatus;
import application.domain.enums.ReturnStatus;
import application.domain.enums.SellerStatus;
import application.domain.enums.ShipmentStatus;
import application.domain.enums.UserRole;
import application.domain.events.BusinessOperationEvent;
import application.domain.exceptions.InsufficientStockException;
import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.exceptions.UserAlreadyExistsException;
import application.domain.models.Order;
import application.domain.models.Product;
import application.domain.models.Refund;
import application.domain.models.ReturnRequest;
import application.domain.models.Seller;
import application.domain.models.Shipment;
import application.domain.models.TestData;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.services.cart.AddItemToCartService;
import application.domain.services.inventory.ConsultInventoryService;
import application.domain.services.inventory.CreateInventoryService;
import application.domain.services.order.CancelOrderService;
import application.domain.services.order.PayOrderService;
import application.domain.services.order.PlaceOrderService;
import application.domain.services.product.CreateProductService;
import application.domain.services.refund.ProcessRefundService;
import application.domain.services.returns.ApproveReturnService;
import application.domain.services.returns.ReceiveReturnService;
import application.domain.services.returns.RequestReturnService;
import application.domain.services.shipment.ConfirmDeliveryService;
import application.domain.services.shipment.CreateShipmentService;
import application.domain.services.shipment.DispatchShipmentService;
import application.domain.services.user.ApproveSellerService;
import application.domain.services.user.BlockUserService;
import application.domain.services.user.RegisterBuyerService;
import application.domain.services.user.RegisterSellerService;
import application.domain.services.user.RegisterStaffUserService;
import application.domain.services.warehouse.CreateWarehouseService;
import application.domain.services.warehouse.DeactivateWarehouseService;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.IdentificationNumber;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.PhoneNumber;
import application.domain.valueobjects.ProductCode;
import application.domain.valueobjects.Quantity;
import application.domain.valueobjects.WarehouseLocation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de los servicios de dominio encadenados, con repositorios en memoria:
 * del registro de usuarios al reembolso de una devolución.
 */
class MarketplaceFlowTest {

    private InMemoryRepositories repos;
    private DomainServiceContainer services;
    private User admin;
    private User supervisor;
    private User operator;
    private Seller seller;
    private User buyer;
    private Product product;
    private Warehouse warehouse;

    @BeforeEach
    void setUp() {
        repos = new InMemoryRepositories();
        services = new DomainServiceContainer(repos);
        admin = repos.users.save(TestData.staff("1000000001", "admin@nx.com", UserRole.ADMIN));

        supervisor = services.get(RegisterStaffUserService.class).execute(admin,
            IdentificationNumber.of("1000000002"), "Supervisora", Email.of("sup@nx.com"),
            PhoneNumber.of("+573000000002"), UserRole.SUPERVISOR, "hash");
        operator = services.get(RegisterStaffUserService.class).execute(admin,
            IdentificationNumber.of("1000000003"), "Operador", Email.of("op@nx.com"),
            PhoneNumber.of("+573000000003"), UserRole.LOGISTIC_OPERATOR, "hash");
        User registered = services.get(RegisterSellerService.class).execute(admin,
            IdentificationNumber.of("900123456-7"), "Vendedora", Email.of("seller@nx.com"),
            PhoneNumber.of("+573000000004"), "hash", "Tienda SAS");
        seller = services.get(ApproveSellerService.class).execute(admin, registered.getId());
        buyer = services.get(RegisterBuyerService.class).execute(
            IdentificationNumber.of("1000000005"), "Comprador", Email.of("buyer@nx.com"),
            PhoneNumber.of("+573000000005"), "hash", TestData.address());

        product = services.get(CreateProductService.class).execute(seller,
            ProductCode.of("TEC-0001"), "Teclado", "Mecánico", Money.of("100.00", "USD"),
            ProductType.PHYSICAL);
        warehouse = services.get(CreateWarehouseService.class).execute(admin, "Bodega Norte",
            TestData.address(), WarehouseLocation.of("A", "1", "1"));
        services.get(CreateInventoryService.class).execute(operator, product.getId(),
            warehouse.getId(), Quantity.of(10), Quantity.of(2), WarehouseLocation.of("A", "1", "1"));
    }

    private Order placeOrderOf(int units) {
        services.get(AddItemToCartService.class).execute(buyer, product.getId(), Quantity.of(units));
        return services.get(PlaceOrderService.class).execute(buyer);
    }

    private int stock() {
        return services.get(ConsultInventoryService.class)
            .findByProductAndWarehouse(product.getId(), warehouse.getId()).getOnHand().getValue();
    }

    @Test
    void fullFlowFromCartToRefund() {
        Order order = placeOrderOf(3);
        assertEquals(OrderStatus.PENDING_PAYMENT, order.getStatus());
        assertEquals(7, stock());
        assertEquals(warehouse.getId(), order.getItems().get(0).getWarehouseId());
        assertEquals(InvoiceStatus.ISSUED, repos.invoices.findByOrderId(order.getId()).get().getStatus());
        assertTrue(repos.carts.findByBuyerId(buyer.getId()).get().isEmpty());

        services.get(PayOrderService.class).execute(buyer, order.getId());
        assertEquals(InvoiceStatus.PAID, repos.invoices.findByOrderId(order.getId()).get().getStatus());

        Shipment shipment = services.get(CreateShipmentService.class).execute(operator, order.getId());
        services.get(DispatchShipmentService.class).execute(operator, shipment.getId(), "GUIA-123");
        services.get(ConfirmDeliveryService.class).execute(operator, shipment.getId());
        assertEquals(ShipmentStatus.DELIVERED, repos.shipments.findById(shipment.getId()).get().getStatus());
        assertEquals(OrderStatus.DELIVERED, repos.orders.findById(order.getId()).get().getStatus());

        ReturnRequest returnRequest = services.get(RequestReturnService.class).execute(buyer,
            order.getId(), product.getId(), Quantity.of(1), "Tecla defectuosa");
        services.get(ApproveReturnService.class).execute(supervisor, returnRequest.getId());
        services.get(ReceiveReturnService.class).execute(operator, returnRequest.getId());
        assertEquals(ReturnStatus.RECEIVED, repos.returns.findById(returnRequest.getId()).get().getStatus());
        assertEquals(8, stock());

        Refund refund = services.get(ProcessRefundService.class).execute(supervisor, returnRequest.getId());
        assertEquals(RefundStatus.COMPLETED, refund.getStatus());
        assertEquals("100.00", refund.getAmount().getAmount().toPlainString());
        assertThrows(IllegalStateException.class,
            () -> services.get(ProcessRefundService.class).execute(supervisor, returnRequest.getId()));

        boolean audited = repos.auditLog.stream()
            .filter(BusinessOperationEvent.class::isInstance)
            .map(e -> ((BusinessOperationEvent) e).operationType())
            .toList().containsAll(java.util.List.of(OperationType.ORDER_PLACED, OperationType.ORDER_PAID,
                OperationType.SHIPMENT_DELIVERED, OperationType.REFUND_COMPLETED));
        assertTrue(audited);
    }

    @Test
    void onlyAdminCanRegisterSellersAndEmailMustBeUnique() {
        assertThrows(UnauthorizedOperationException.class, () ->
            services.get(RegisterSellerService.class).execute(buyer,
                IdentificationNumber.of("900999999-1"), "X", Email.of("x@nx.com"),
                PhoneNumber.of("+573000000009"), "hash", "X SAS"));
        assertThrows(UserAlreadyExistsException.class, () ->
            services.get(RegisterBuyerService.class).execute(IdentificationNumber.of("1000000099"),
                "Otro", Email.of("buyer@nx.com"), PhoneNumber.of("+573000000099"), "hash",
                TestData.address()));
        assertEquals(SellerStatus.APPROVED, seller.getSellerStatus());
    }

    @Test
    void blockedUsersCannotOperate() {
        services.get(BlockUserService.class).execute(supervisor, buyer.getId());
        assertThrows(UnauthorizedOperationException.class, () ->
            services.get(AddItemToCartService.class).execute(buyer, product.getId(), Quantity.of(1)));
        assertThrows(UnauthorizedOperationException.class, () ->
            services.get(BlockUserService.class).execute(supervisor, supervisor.getId()));
    }

    @Test
    void stockCannotGoNegativeAndInactiveWarehousesAreSkipped() {
        services.get(AddItemToCartService.class).execute(buyer, product.getId(), Quantity.of(11));
        assertThrows(InsufficientStockException.class,
            () -> services.get(PlaceOrderService.class).execute(buyer));

        services.get(DeactivateWarehouseService.class).execute(admin, warehouse.getId());
        assertThrows(InsufficientStockException.class, () -> placeOrderOf(1));
        assertEquals(10, stock());
    }

    @Test
    void cancellingAnOrderReleasesStockAndVoidsInvoice() {
        Order order = placeOrderOf(4);
        assertEquals(6, stock());
        services.get(CancelOrderService.class).execute(buyer, order.getId());
        assertEquals(10, stock());
        assertEquals(InvoiceStatus.VOIDED, repos.invoices.findByOrderId(order.getId()).get().getStatus());
    }

    @Test
    void buyersOnlyOperateOnTheirOwnOrdersAndCannotReturnMoreThanPurchased() {
        Order order = placeOrderOf(1);
        User otherBuyer = services.get(RegisterBuyerService.class).execute(
            IdentificationNumber.of("1000000077"), "Otro", Email.of("otro@nx.com"),
            PhoneNumber.of("+573000000077"), "hash", TestData.address());
        assertThrows(UnauthorizedOperationException.class,
            () -> services.get(PayOrderService.class).execute(otherBuyer, order.getId()));

        services.get(PayOrderService.class).execute(buyer, order.getId());
        Shipment shipment = services.get(CreateShipmentService.class).execute(operator, order.getId());
        services.get(DispatchShipmentService.class).execute(operator, shipment.getId(), "GUIA-9");
        services.get(ConfirmDeliveryService.class).execute(operator, shipment.getId());
        services.get(RequestReturnService.class).execute(buyer, order.getId(), product.getId(),
            Quantity.of(1), "No me gustó");
        assertThrows(IllegalArgumentException.class, () ->
            services.get(RequestReturnService.class).execute(buyer, order.getId(), product.getId(),
                Quantity.of(1), "Otra vez"));
    }

    @Test
    void onlyApprovedSellersPublishAndOnlyLogisticOperatorsShip() {
        User pending = services.get(RegisterSellerService.class).execute(admin,
            IdentificationNumber.of("900555555-5"), "Nueva", Email.of("nueva@nx.com"),
            PhoneNumber.of("+573000000055"), "hash", "Nueva SAS");
        assertThrows(UnauthorizedOperationException.class, () ->
            services.get(CreateProductService.class).execute(pending, ProductCode.of("NEW-0001"),
                "Algo", null, Money.of("5.00", "USD"), ProductType.DIGITAL));

        Order order = placeOrderOf(1);
        services.get(PayOrderService.class).execute(buyer, order.getId());
        assertThrows(UnauthorizedOperationException.class,
            () -> services.get(CreateShipmentService.class).execute(admin, order.getId()));
    }
}
