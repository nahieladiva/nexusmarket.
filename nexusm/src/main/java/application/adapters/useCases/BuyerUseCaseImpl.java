package application.adapters.useCases;

import application.domain.models.Cart;
import application.domain.models.Invoice;
import application.domain.models.Order;
import application.domain.models.Refund;
import application.domain.models.ReturnRequest;
import application.domain.models.Shipment;
import application.domain.models.User;
import application.domain.ports.in.BuyerPort;
import application.domain.services.cart.AddItemToCartService;
import application.domain.services.cart.ChangeCartItemQuantityService;
import application.domain.services.cart.ConsultCartService;
import application.domain.services.cart.RemoveItemFromCartService;
import application.domain.services.invoice.ConsultInvoiceService;
import application.domain.services.order.CancelOrderService;
import application.domain.services.order.ConsultOrderService;
import application.domain.services.order.PayOrderService;
import application.domain.services.order.PlaceOrderService;
import application.domain.services.refund.ConsultRefundService;
import application.domain.services.returns.ConsultReturnService;
import application.domain.services.returns.RequestReturnService;
import application.domain.services.shipment.ConsultShipmentService;
import application.domain.services.user.ConsultUserService;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;
import application.domain.valueobjects.ReturnId;
import application.domain.valueobjects.UserId;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementa {@link BuyerPort}: carga al comprador y delega en los servicios de dominio.
 */
@Service
@Transactional
public class BuyerUseCaseImpl implements BuyerPort {

    private final ConsultUserService consultUserService;
    private final ConsultCartService consultCartService;
    private final AddItemToCartService addItemToCartService;
    private final ChangeCartItemQuantityService changeCartItemQuantityService;
    private final RemoveItemFromCartService removeItemFromCartService;
    private final PlaceOrderService placeOrderService;
    private final PayOrderService payOrderService;
    private final CancelOrderService cancelOrderService;
    private final ConsultOrderService consultOrderService;
    private final ConsultInvoiceService consultInvoiceService;
    private final ConsultShipmentService consultShipmentService;
    private final RequestReturnService requestReturnService;
    private final ConsultReturnService consultReturnService;
    private final ConsultRefundService consultRefundService;

    public BuyerUseCaseImpl(ConsultUserService consultUserService,
                            ConsultCartService consultCartService,
                            AddItemToCartService addItemToCartService,
                            ChangeCartItemQuantityService changeCartItemQuantityService,
                            RemoveItemFromCartService removeItemFromCartService,
                            PlaceOrderService placeOrderService,
                            PayOrderService payOrderService,
                            CancelOrderService cancelOrderService,
                            ConsultOrderService consultOrderService,
                            ConsultInvoiceService consultInvoiceService,
                            ConsultShipmentService consultShipmentService,
                            RequestReturnService requestReturnService,
                            ConsultReturnService consultReturnService,
                            ConsultRefundService consultRefundService) {
        this.consultUserService = consultUserService;
        this.consultCartService = consultCartService;
        this.addItemToCartService = addItemToCartService;
        this.changeCartItemQuantityService = changeCartItemQuantityService;
        this.removeItemFromCartService = removeItemFromCartService;
        this.placeOrderService = placeOrderService;
        this.payOrderService = payOrderService;
        this.cancelOrderService = cancelOrderService;
        this.consultOrderService = consultOrderService;
        this.consultInvoiceService = consultInvoiceService;
        this.consultShipmentService = consultShipmentService;
        this.requestReturnService = requestReturnService;
        this.consultReturnService = consultReturnService;
        this.consultRefundService = consultRefundService;
    }

    private User buyer(UserId buyerId) {
        return consultUserService.findById(buyerId);
    }

    @Override
    public Cart getCart(UserId buyerId) {
        return consultCartService.execute(buyer(buyerId));
    }

    @Override
    public Cart addToCart(UserId buyerId, ProductId productId, Quantity quantity) {
        return addItemToCartService.execute(buyer(buyerId), productId, quantity);
    }

    @Override
    public Cart changeCartItemQuantity(UserId buyerId, ProductId productId, Quantity quantity) {
        return changeCartItemQuantityService.execute(buyer(buyerId), productId, quantity);
    }

    @Override
    public Cart removeFromCart(UserId buyerId, ProductId productId) {
        return removeItemFromCartService.execute(buyer(buyerId), productId);
    }

    @Override
    public Order placeOrder(UserId buyerId) {
        return placeOrderService.execute(buyer(buyerId));
    }

    @Override
    public Order payOrder(UserId buyerId, OrderId orderId) {
        return payOrderService.execute(buyer(buyerId), orderId);
    }

    @Override
    public Order cancelOrder(UserId buyerId, OrderId orderId) {
        return cancelOrderService.execute(buyer(buyerId), orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findMyOrders(UserId buyerId) {
        return consultOrderService.findMine(buyer(buyerId));
    }

    @Override
    @Transactional(readOnly = true)
    public Order findOrder(UserId buyerId, OrderId orderId) {
        return consultOrderService.findById(buyer(buyerId), orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public Invoice findInvoice(UserId buyerId, OrderId orderId) {
        return consultInvoiceService.findByOrder(buyer(buyerId), orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public Shipment findShipment(UserId buyerId, OrderId orderId) {
        return consultShipmentService.findByOrder(buyer(buyerId), orderId);
    }

    @Override
    public ReturnRequest requestReturn(UserId buyerId, OrderId orderId, ProductId productId,
                                       Quantity quantity, String reason) {
        return requestReturnService.execute(buyer(buyerId), orderId, productId, quantity, reason);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReturnRequest> findMyReturns(UserId buyerId) {
        return consultReturnService.findMine(buyer(buyerId));
    }

    @Override
    @Transactional(readOnly = true)
    public Refund findRefund(UserId buyerId, ReturnId returnId) {
        return consultRefundService.findByReturn(buyer(buyerId), returnId);
    }
}
