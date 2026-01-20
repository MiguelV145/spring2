package ec.edu.ups.icc.fundamentos01.products.mappers;

import ec.edu.ups.icc.fundamentos01.products.dtos.CreateProductDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.UpdateProductDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.ProductResponseDto;
import ec.edu.ups.icc.fundamentos01.products.models.Product;

public class ProductMapper {

    public static Product toModel(int id, String name, Double price, String description) {
        return new Product(id, name, price, description);
    }

    // DTO -> Model
    public static Product fromCreateDto(CreateProductDto dto) {
        return new Product(0, dto.name, dto.price, dto.description);
    }

    public static Product fromUpdateDto(UpdateProductDto dto) {
        return new Product(0, dto.name, dto.price, dto.description);
    }

    public static ProductResponseDto toResponse(Product product) {
        ProductResponseDto dto = new ProductResponseDto();
        dto.id = product.getId();
        dto.name = product.getName();
        dto.price = product.getPrice();
        dto.description = product.getDescription();

        // dto.user = new ProductResponseDto.UserSummaryDto();
        // dto.user.id = product.getOwner().getId();
        // dto.user.username = product.getOwner().getUsername();

        return dto;
    }
}
