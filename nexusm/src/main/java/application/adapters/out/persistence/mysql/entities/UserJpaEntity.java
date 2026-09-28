package application.adapters.out.persistence.mysql.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad JPA de usuario. Los roles Buyer/Seller se modelan en la misma tabla
 * (SINGLE_TABLE) mediante la columna {@code role}.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class UserJpaEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "identification", nullable = false, unique = true, length = 20)
    private String identification;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "role", nullable = false, length = 20)
    private String role;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    /** Solo aplica a vendedores. */
    @Column(name = "business_name")
    private String businessName;

    /** Solo aplica a vendedores. */
    @Column(name = "seller_status", length = 20)
    private String sellerStatus;

    /** Solo aplica a compradores. */
    @Column(name = "shipping_street")
    private String shippingStreet;

    /** Solo aplica a compradores. */
    @Column(name = "shipping_city")
    private String shippingCity;

    /** Solo aplica a compradores. */
    @Column(name = "shipping_state")
    private String shippingState;

    /** Solo aplica a compradores. */
    @Column(name = "shipping_zip")
    private String shippingZip;

    /** Solo aplica a compradores. */
    @Column(name = "shipping_country")
    private String shippingCountry;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public UserJpaEntity(String id, String identification, String fullName, String email,
                         String phone, String role, String status, String passwordHash,
                         LocalDateTime createdAt) {
        this.id = id;
        this.identification = identification;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.status = status;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
    }
}