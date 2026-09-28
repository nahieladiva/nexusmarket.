package application.domain.ports.in;

import application.domain.models.Cart;
import application.domain.models.Invoice;
import application.domain.models.Order;
import application.domain.models.Refund;
import application.domain.models.ReturnRequest;
import application.domain.models.Shipment;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;
import application.domain.valueobjects.ReturnId;
import application.domain.valueobjects.UserId;

import java.util.List;

/**
 * Casos de uso del Comprador. {@code buyerId} identifica al usuario que opera.
 */
public interface BuyerPort {

    Cart getCart(UserId buyerId);

    Cart addToCart(UserId buyerId, ProductId productId, Quantity quantity);

    Cart changeCartItemQuantity(UserId buyerId, ProductId productId, Quantity quantity);

    Cart removeFromCart(UserId buyerId, ProductId productId);

    Order placeOrder(UserId buyerId);

    Order payOrder(UserId buyerId, OrderId orderId);

    Order cancelOrder(UserId buyerId, OrderId orderId);

    List<Order> findMyOrders(UserId buyerId);

    Order findOrder(UserId buyerId, OrderId orderId);

    Invoice findInvoice(UserId buyerId, OrderId orderId);

    Shipment findShipment(UserId buyerId, OrderId orderId);

    ReturnRequest requestReturn(UserId buyerId, OrderId orderId, ProductId productId,
                                Quantity quantity, String reason);

    List<ReturnRequest> findMyReturns(UserId buyerId);

    Refund findRefund(UserId buyerId, ReturnId returnId);
}
