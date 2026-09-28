package application.domain.enums;

/**
 * Tipo de movimiento de inventario.
 *
 * <ul>
 *   <li>{@code INFLOW}: entrada de mercancía a la bodega (suma).</li>
 *   <li>{@code RESERVATION}: reserva de stock para un pedido (resta).</li>
 *   <li>{@code SALE}: salida definitiva por venta (resta).</li>
 *   <li>{@code ADJUSTMENT}: ajuste manual por conteo físico (suma o resta).</li>
 *   <li>{@code RETURN}: reingreso por devolución (suma).</li>
 * </ul>
 */
public enum MovementType {

    INFLOW,
    RESERVATION,
    SALE,
    ADJUSTMENT,
    RETURN;

    /**
     * Indica si el movimiento incrementa el stock disponible.
     * {@code ADJUSTMENT} puede ir en ambos sentidos, por eso se trata aparte.
     */
    public boolean increasesStock() {
        return this == INFLOW || this == RETURN;
    }

    public boolean decreasesStock() {
        return this == RESERVATION || this == SALE;
    }
}
