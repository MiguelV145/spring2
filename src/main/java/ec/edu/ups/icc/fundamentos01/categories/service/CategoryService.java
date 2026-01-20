package ec.edu.ups.icc.fundamentos01.categories.service;

import java.util.List;

import ec.edu.ups.icc.fundamentos01.categories.dtos.CategoryCreateDto;
import ec.edu.ups.icc.fundamentos01.categories.dtos.CategoryResponseDto;

public interface CategoryService {

    List<CategoryResponseDto> findAll();

    void save(CategoryCreateDto createDto);

}
