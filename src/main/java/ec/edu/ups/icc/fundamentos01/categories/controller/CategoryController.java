package ec.edu.ups.icc.fundamentos01.categories.controller;

import org.springframework.web.bind.annotation.RestController;

import ec.edu.ups.icc.fundamentos01.categories.dtos.CategoryCreateDto;
import ec.edu.ups.icc.fundamentos01.categories.dtos.CategoryResponseDto;
import ec.edu.ups.icc.fundamentos01.categories.service.CategoryService;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;

    }

    @PostMapping()
    public ResponseEntity<String> create(@RequestBody CategoryCreateDto entity) {

        categoryService.save(entity);
        return ResponseEntity.ok("Categoría creada exitosamente");
    }

    @GetMapping()
    public ResponseEntity<List<CategoryResponseDto>> getAll() {

        return ResponseEntity.ok(categoryService.findAll());
    }

}
