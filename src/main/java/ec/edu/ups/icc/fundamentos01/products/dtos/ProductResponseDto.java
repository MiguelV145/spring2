package ec.edu.ups.icc.fundamentos01.products.dtos;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import ec.edu.ups.icc.fundamentos01.categories.dtos.CategoryResponseDto;

@JsonPropertyOrder({
        "id",
        "name",
        "price",
        "description",
        "user",
        "categories",
        "createdAt",
        "updatedAt"
})
public class ProductResponseDto {
    public Long id;
    public String name;
    public Double price;
    public String description;

    // ============== OBJETOS ANIDADOS ==============

    public UserSummaryDto user;

    // public CategoryResponseDto category;
    // ============== CATEGORÍAS (N:N) - Lista de objetos ==============
    public List<CategoryResponseDto> categories;

    // ============== AUDITORÍA ==============

    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;

    // ============== DTOs INTERNOS ==============

    public static class UserSummaryDto {
        public Long id;
        public String name;
        public String email;
    }

}
