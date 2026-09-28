package application.adapters.in.rest.requests;

/**
 * DTO de registro de un vendedor (solo lo usa un Administrador).
 */
public record CreateSellerRequest(String identification, String fullName, String email,
                                  String phone, String password, String businessName) {
}
