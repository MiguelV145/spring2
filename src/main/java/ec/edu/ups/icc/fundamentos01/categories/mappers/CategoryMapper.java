package ec.edu.ups.icc.fundamentos01.categories.mappers;

import java.util.ArrayList;
import java.util.List;

import ec.edu.ups.icc.fundamentos01.categories.dtos.CategoryResponseDto;
import ec.edu.ups.icc.fundamentos01.categories.entity.CategoryEntity;
import ec.edu.ups.icc.fundamentos01.products.dtos.ProductResponseDto;

public class CategoryMapper {

    public static CategoryResponseDto toResponseDto(CategoryEntity categoryEntity

    ) {
        List<ProductResponseDto> productsDTO = new ArrayList<>();
        for (var productEntity : categoryEntity.getProducts()) {
            var productDto = new ProductResponseDto();
            productDto.id = productEntity.getId();
            productDto.name = productEntity.getName();
            productDto.description = productEntity.getDescription();
            productDto.price = productEntity.getPrice();
            productsDTO.add(productDto);
        }
        return new CategoryResponseDto() {

            {
                id = categoryEntity.getId();
                name = categoryEntity.getName();
                description = categoryEntity.getDescription();
                // products = productsDTO;

            }
        };

    }

}
