package application.adapters.in.rest.requests;

/**
 * DTO de registro de personal interno: {@code role} debe ser
 * LOGISTIC_OPERATOR, ADMIN o SUPERVISOR.
 */
public record CreateStaffUserRequest(String identification, String fullName, String email,
                                     String phone, String password, String role) {
}
