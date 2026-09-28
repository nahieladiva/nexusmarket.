package application.adapters.in.rest.responses;

import java.time.LocalDateTime;
import java.util.List;

public record UserResponse(String id, String identification, String fullName, String email,
                           String phone, String role, String status, String businessName,
                           String sellerStatus, AddressResponse defaultShippingAddress,
                           List<AddressResponse> addresses, LocalDateTime createdAt) {
}
