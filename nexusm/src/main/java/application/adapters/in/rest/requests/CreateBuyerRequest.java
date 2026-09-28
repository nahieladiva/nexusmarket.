package application.adapters.in.rest.requests;

/**
 * DTO de registro de un comprador. {@code password} llega en texto plano
 * y se cifra con BCrypt antes de llegar al dominio.
 */
public record CreateBuyerRequest(String identification, String fullName, String email,
                                 String phone, String password,
                                 AddressRequest defaultShippingAddress) {
}
