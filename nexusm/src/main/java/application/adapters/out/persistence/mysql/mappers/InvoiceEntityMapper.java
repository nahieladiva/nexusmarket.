package application.adapters.out.persistence.mysql.mappers;

import application.adapters.out.persistence.mysql.entities.InvoiceJpaEntity;
import application.domain.enums.InvoiceStatus;
import application.domain.models.Invoice;
import application.domain.valueobjects.InvoiceId;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.UserId;

import org.springframework.stereotype.Component;

/**
 * Traduce entre {@link Invoice} y {@link InvoiceJpaEntity}.
 */
@Component
public class InvoiceEntityMapper {

    public InvoiceJpaEntity toEntity(Invoice invoice) {
        InvoiceJpaEntity entity = new InvoiceJpaEntity();
        entity.setId(invoice.getId().toString());
        entity.setInvoiceNumber(invoice.getInvoiceNumber());
        entity.setOrderId(invoice.getOrderId().toString());
        entity.setBuyerId(invoice.getBuyerId().toString());
        entity.setTotalAmount(invoice.getTotal().getAmount());
        entity.setTotalCurrency(invoice.getTotal().getCurrency().getCurrencyCode());
        entity.setStatus(invoice.getStatus().name());
        entity.setIssuedAt(invoice.getIssuedAt());
        entity.setPaidAt(invoice.getPaidAt());
        return entity;
    }

    public Invoice toDomain(InvoiceJpaEntity entity) {
        return new Invoice(InvoiceId.of(entity.getId()), entity.getInvoiceNumber(),
            OrderId.of(entity.getOrderId()), UserId.of(entity.getBuyerId()),
            Money.of(entity.getTotalAmount().toPlainString(), entity.getTotalCurrency()),
            InvoiceStatus.valueOf(entity.getStatus()), entity.getIssuedAt(), entity.getPaidAt());
    }
}
