package application.adapters.useCases;

import application.domain.models.Order;
import application.domain.models.Product;
import application.domain.models.Refund;
import application.domain.models.ReturnRequest;
import application.domain.models.User;
import application.domain.ports.in.SupervisorPort;
import application.domain.services.order.CancelOrderService;
import application.domain.services.order.ConsultOrderService;
import application.domain.services.product.SuspendProductService;
import application.domain.services.refund.ProcessRefundService;
import application.domain.services.returns.ApproveReturnService;
import application.domain.services.returns.ConsultReturnService;
import application.domain.services.returns.RejectReturnService;
import application.domain.services.user.ActivateUserService;
import application.domain.services.user.BlockUserService;
import application.domain.services.user.ConsultUserService;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.ReturnId;
import application.domain.valueobjects.UserId;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementa {@link SupervisorPort}. Los servicios de dominio validan que el
 * usuario sea Supervisor o Administrador activo.
 */
@Service
@Transactional
public class SupervisorUseCaseImpl implements SupervisorPort {

    private final ConsultUserService consultUserService;
    private final BlockUserService blockUserService;
    private final ActivateUserService activateUserService;
    private final ConsultOrderService consultOrderService;
    private final CancelOrderService cancelOrderService;
    private final SuspendProductService suspendProductService;
    private final ConsultReturnService consultReturnService;
    private final ApproveReturnService approveReturnService;
    private final RejectReturnService rejectReturnService;
    private final ProcessRefundService processRefundService;

    public SupervisorUseCaseImpl(ConsultUserService consultUserService,
                                 BlockUserService blockUserService,
                                 ActivateUserService activateUserService,
                                 ConsultOrderService consultOrderService,
                                 CancelOrderService cancelOrderService,
                                 SuspendProductService suspendProductService,
                                 ConsultReturnService consultReturnService,
                                 ApproveReturnService approveReturnService,
                                 RejectReturnService rejectReturnService,
                                 ProcessRefundService processRefundService) {
        this.consultUserService = consultUserService;
        this.blockUserService = blockUserService;
        this.activateUserService = activateUserService;
        this.consultOrderService = consultOrderService;
        this.cancelOrderService = cancelOrderService;
        this.suspendProductService = suspendProductService;
        this.consultReturnService = consultReturnService;
        this.approveReturnService = approveReturnService;
        this.rejectReturnService = rejectReturnService;
        this.processRefundService = processRefundService;
    }

    private User actor(UserId actorId) {
        return consultUserService.findById(actorId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAllUsers(UserId actorId) {
        return consultUserService.findAll(actor(actorId));
    }

    @Override
    @Transactional(readOnly = true)
    public User findUser(UserId actorId, UserId userId) {
        return consultUserService.findById(actor(actorId), userId);
    }

    @Override
    public User blockUser(UserId actorId, UserId userId) {
        return blockUserService.execute(actor(actorId), userId);
    }

    @Override
    public User activateUser(UserId actorId, UserId userId) {
        return activateUserService.execute(actor(actorId), userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findAllOrders(UserId actorId) {
        return consultOrderService.findAll(actor(actorId));
    }

    @Override
    @Transactional(readOnly = true)
    public Order findOrder(UserId actorId, OrderId orderId) {
        return consultOrderService.findById(actor(actorId), orderId);
    }

    @Override
    public Order cancelOrder(UserId actorId, OrderId orderId) {
        return cancelOrderService.execute(actor(actorId), orderId);
    }

    @Override
    public Product suspendProduct(UserId actorId, ProductId productId) {
        return suspendProductService.execute(actor(actorId), productId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReturnRequest> findAllReturns(UserId actorId) {
        return consultReturnService.findAll(actor(actorId));
    }

    @Override
    public ReturnRequest approveReturn(UserId actorId, ReturnId returnId) {
        return approveReturnService.execute(actor(actorId), returnId);
    }

    @Override
    public ReturnRequest rejectReturn(UserId actorId, ReturnId returnId) {
        return rejectReturnService.execute(actor(actorId), returnId);
    }

    @Override
    public Refund processRefund(UserId actorId, ReturnId returnId) {
        return processRefundService.execute(actor(actorId), returnId);
    }
}
