package ec.edu.ups.icc.fundamentos01.users.services;

import java.util.List;

import ec.edu.ups.icc.fundamentos01.products.dtos.ProductResponseDto;
import ec.edu.ups.icc.fundamentos01.users.dtos.CreateUserDto;
import ec.edu.ups.icc.fundamentos01.users.dtos.PartialUpdateUserDto;
import ec.edu.ups.icc.fundamentos01.users.dtos.UpdateUserDto;
import ec.edu.ups.icc.fundamentos01.users.dtos.UserResponseDto;

public interface UserService {

    List<UserResponseDto> findAll();

    UserResponseDto findOne(int id);

    UserResponseDto create(CreateUserDto dto);

    UserResponseDto update(int id, UpdateUserDto dto);

    UserResponseDto partialUpdate(int id, PartialUpdateUserDto dto);

    void delete(int id);

    List<ProductResponseDto> getProductsByUserId(Long userId);

    List<ProductResponseDto> getProductsByUserIdWithFilters(
            Long userId,
            String name,
            Double minPrice,
            Double maxPrice,
            Long categoryId);
}
