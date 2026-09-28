package application.domain.services.order;

import application.domain.enums.UserRole;
import application.domain.exceptions.ResourceNotFoundException;
import application.domain.models.Order;
import application.domain.models.User;
import application.domain.ports.out.OrderRepository;
import application.domain.services.DomainService;
import application.domain.services.authorization.AuthorizeBuyerOperationService;
import application.domain.services.authorization.AuthorizeSupervisionOperationService;
import application.domain.services.authorization.ValidateActiveUserService;
import application.domain.services.authorization.ValidateOrderOwnershipService;
import application.domain.services.authorization.ValidateRoleService;
import application.domain.valueobjects.OrderId;

import java.util.List;

/**
 * Consulta de pedidos: el comprador solo ve los suyos; el personal interno
 * (Operador Logístico, Administrador, Supervisor) puede ver cualquiera.
 */
@DomainService
public class ConsultOrderService {

    private final OrderRepository orderRepository;
    private final ValidateActiveUserService validateActiveUserService;
    private final ValidateRoleService validateRoleService;
    private final ValidateOrderOwnershipService validateOrderOwnershipService;
    private final AuthorizeBuyerOperationService authorizeBuyerOperationService;
    private final AuthorizeSupervisionOperationService authorizeSupervisionOperationService;

    public ConsultOrderService(OrderRepository orderRepository,
                               ValidateActiveUserService validateActiveUserService,
                               ValidateRoleService validateRoleService,
                               ValidateOrderOwnershipService validateOrderOwnershipService,
                               AuthorizeBuyerOperationService authorizeBuyerOperationService,
                               AuthorizeSupervisionOperationService authorizeSupervisionOperationService) {
        this.orderRepository = orderRepository;
        this.validateActiveUserService = validateActiveUserService;
        this.validateRoleService = validateRoleService;
        this.validateOrderOwnershipService = validateOrderOwnershipService;
        this.authorizeBuyerOperationService = authorizeBuyerOperationService;
        this.authorizeSupervisionOperationService = authorizeSupervisionOperationService;
    }

    /** Carga el pedido sin validar permisos (uso interno de otros servicios). */
    public Order require(OrderId id) {
        return orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Pedido", id.toString()));
    }

    public Order findById(User actor, OrderId id) {
        validateActiveUserService.execute(actor);
        Order order = require(id);
        if (actor.isBuyer()) {
            validateOrderOwnershipService.execute(actor, order);
        } else {
            validateRoleService.execute(actor, UserRole.LOGISTIC_OPERATOR, UserRole.ADMIN,
                UserRole.SUPERVISOR);
        }
        return order;
    }

    public List<Order> findMine(User buyer) {
        authorizeBuyerOperationService.execute(buyer);
        return orderRepository.findByBuyerId(buyer.getId());
    }

    public List<Order> findAll(User actor) {
        authorizeSupervisionOperationService.execute(actor);
        return orderRepository.findAll();
    }
}
