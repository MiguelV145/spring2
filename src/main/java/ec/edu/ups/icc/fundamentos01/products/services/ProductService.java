package ec.edu.ups.icc.fundamentos01.products.services;

import java.util.List;

import org.springframework.data.domain.Page;

import org.springframework.data.domain.Slice;

import ec.edu.ups.icc.fundamentos01.products.dtos.CreateProductDto;

import ec.edu.ups.icc.fundamentos01.products.dtos.ProductResponseDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.UpdateProductDto;
import ec.edu.ups.icc.fundamentos01.security.service.UserDetailsImpl;

public interface ProductService {

    // ============== MÉTODOS BÁSICOS EXISTENTES ==============
    List<ProductResponseDto> findAll();

    ProductResponseDto create(CreateProductDto dto);
    ProductResponseDto findById(Long id);
   
    
    List<ProductResponseDto> findByUserId(Long id);
    List<ProductResponseDto> findByCategoryId(Long id);

    ProductResponseDto update(Long id, UpdateProductDto dto, UserDetailsImpl currentUser);

    /**
     * Eliminar producto con validación de ownership
     * @param id ID del producto a eliminar
     * @param currentUser Usuario autenticado (del JWT)
     * @throws AccessDeniedException si no eres dueño ni tienes rol privilegiado
     */
    void delete(Long id, UserDetailsImpl currentUser);

    // ============== MÉTODOS CON PAGINACIÓN ==============

    /**
     * Obtiene todos los productos con paginación completa (Page)
     */
    Page<ProductResponseDto> findAllaginado(int page, int size, String[] sort);

    /**
     * Obtiene todos los productos con paginación ligera (Slice)
     */
    Slice<ProductResponseDto> findAllSlice(int page, int size, String[] sort);

    /**
     * Busca productos con filtros y paginación
     */
    Page<ProductResponseDto> findWithFilters(
        String name, 
        Double minPrice, 
        Double maxPrice, 
        Long categoryId,
        int page, 
        int size, 
        String[] sort
    );


    
    /**
     * Productos de un usuario con filtros y paginación
     */
    Page<ProductResponseDto> findByUserIdWithFilters(
        Long userId,
        String name,
        Double minPrice,
        Double maxPrice,
        Long categoryId,
        int page,
        int size,
        String[] sort
    );


    
}


