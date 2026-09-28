package application.domain.ports.in;

import application.domain.models.Order;
import application.domain.models.Product;
import application.domain.models.Refund;
import application.domain.models.ReturnRequest;
import application.domain.models.User;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.ReturnId;
import application.domain.valueobjects.UserId;

import java.util.List;

/**
 * Casos de uso de supervisión (Supervisor o Administrador): usuarios,
 * pedidos, moderación de productos, devoluciones y reembolsos.
 */
public interface SupervisorPort {

    List<User> findAllUsers(UserId actorId);

    User findUser(UserId actorId, UserId userId);

    User blockUser(UserId actorId, UserId userId);

    User activateUser(UserId actorId, UserId userId);

    List<Order> findAllOrders(UserId actorId);

    Order findOrder(UserId actorId, OrderId orderId);

    Order cancelOrder(UserId actorId, OrderId orderId);

    Product suspendProduct(UserId actorId, ProductId productId);

    List<ReturnRequest> findAllReturns(UserId actorId);

    ReturnRequest approveReturn(UserId actorId, ReturnId returnId);

    ReturnRequest rejectReturn(UserId actorId, ReturnId returnId);

    Refund processRefund(UserId actorId, ReturnId returnId);
}
