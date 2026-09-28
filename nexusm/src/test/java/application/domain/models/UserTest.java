package application.domain.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import application.domain.enums.SellerStatus;
import application.domain.enums.UserRole;
import application.domain.enums.UserStatus;
import application.domain.exceptions.InvalidStatusTransitionException;
import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.IdentificationNumber;
import application.domain.valueobjects.PhoneNumber;

import org.junit.jupiter.api.Test;

/**
 * Pruebas de {@link User}, {@link Buyer} y {@link Seller}.
 */
class UserTest {

    @Test
    void newUsersAreActiveAndHaveASingleRole() {
        Buyer buyer = TestData.buyer("1017000001", "buyer@test.com");
        assertEquals(UserStatus.ACTIVE, buyer.getStatus());
        assertEquals(UserRole.BUYER, buyer.getRole());
        assertTrue(buyer.isBuyer());
        assertFalse(buyer.isSeller());
    }

    @Test
    void blockedUserCannotOperate() {
        Buyer buyer = TestData.buyer("1017000002", "b2@test.com");
        buyer.block();
        assertEquals(UserStatus.BLOCKED, buyer.getStatus());
        assertThrows(UnauthorizedOperationException.class, buyer::requireActive);
        assertThrows(InvalidStatusTransitionException.class, buyer::block);
        buyer.activate();
        assertTrue(buyer.isActive());
    }

    @Test
    void sellerStartsPendingAndCannotPublishUntilApproved() {
        Seller seller = TestData.seller("900123456-7", "s@test.com");
        assertEquals(SellerStatus.PENDING_APPROVAL, seller.getSellerStatus());
        assertThrows(UnauthorizedOperationException.class, seller::requireApprovedToPublish);
        seller.approve();
        seller.requireApprovedToPublish();
        seller.suspend();
        assertThrows(UnauthorizedOperationException.class, seller::requireApprovedToPublish);
    }

    @Test
    void staffUsersCanOnlyHaveInternalRoles() {
        User operator = TestData.staff("1017000003", "op@test.com", UserRole.LOGISTIC_OPERATOR);
        assertTrue(operator.isLogisticOperator());
        assertThrows(IllegalArgumentException.class, () -> User.createStaff(
            IdentificationNumber.of("1017000004"), "X", Email.of("x@test.com"),
            PhoneNumber.of("+573001234567"), UserRole.BUYER, "hash"));
    }

    @Test
    void identificationIsValidated() {
        assertThrows(IllegalArgumentException.class, () -> IdentificationNumber.of("12"));
        assertThrows(IllegalArgumentException.class, () -> IdentificationNumber.of("   "));
        assertEquals("900123456-7", IdentificationNumber.of(" 900123456-7 ").getValue());
    }
}
