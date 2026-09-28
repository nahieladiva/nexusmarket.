package application.adapters.in.rest.responses;

public record WarehouseResponse(String id, String name, AddressResponse address,
                                String aisle, String shelf, String bin, String status) {
}
