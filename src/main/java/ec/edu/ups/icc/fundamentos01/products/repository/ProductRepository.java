package ec.edu.ups.icc.fundamentos01.products.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ec.edu.ups.icc.fundamentos01.products.models.ProductEntity;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

        Optional<ProductEntity> findByName(String name);

        List<ProductEntity> findByOwnerId(Long userId);

        /**
         * Encuentra productos por nombre de usuario
         * Genera JOIN automáticamente:
         * SELECT p.* FROM products p JOIN users u ON p.user_id = u.id WHERE u.name = ?
         */
        List<ProductEntity> findByOwnerName(String ownerName);

        /**
         * Encuentra productos que tienen UNA categoría específica
         * Útil para filtros de categoría
         */
        List<ProductEntity> findByCategoriesId(Long categoryId);


        /**
         * Encuentra productos que tienen una categoría con nombre específico
         */
        List<ProductEntity> findByCategoriesName(String categoryName);

        /**
         * Consulta personalizada: productos con TODAS las categorías especificadas
         */
        @Query("SELECT p FROM ProductEntity p " +
                        "WHERE SIZE(p.categories) >= :categoryCount " +
                        "AND :categoryCount = " +
                        "(SELECT COUNT(c) FROM p.categories c WHERE c.id IN :categoryIds)")
        List<ProductEntity> findByAllCategories(@Param("categoryIds") List<Long> categoryIds,
                        @Param("categoryCount") long categoryCount);

        /**
         * Consulta personalizada: productos de un usuario con filtros opcionales
         * Permite filtrar por nombre, precio mínimo/máximo y categoría
         */
        @Query("SELECT DISTINCT p FROM ProductEntity p " +
                        "LEFT JOIN p.categories c " +
                        "WHERE p.owner.id = :userId " +
                        "AND (COALESCE(:name, '') = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
                        "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
                        "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
                        "AND (:categoryId IS NULL OR c.id = :categoryId)")
        List<ProductEntity> findByOwnerIdWithFilters(
                        @Param("userId") Long userId,
                        @Param("name") String name,
                        @Param("minPrice") Double minPrice,
                        @Param("maxPrice") Double maxPrice,
                        @Param("categoryId") Long categoryId);

/**
     * Busca productos por nombre de usuario con paginación
     */
    @Query("SELECT p FROM ProductEntity p " +
           "JOIN p.owner o WHERE LOWER(o.name) LIKE LOWER(CONCAT('%', :ownerName, '%'))")
    Page<ProductEntity> findByOwnerNameContaining(@Param("ownerName") String ownerName, Pageable pageable);

 /**
     * Busca productos por categoría con paginación
     * Usa LEFT JOIN porque la relación es Many-to-Many
     */
    @Query("SELECT DISTINCT p FROM ProductEntity p " +
           "LEFT JOIN p.categories c " +
           "WHERE c.id = :categoryId")
    Page<ProductEntity> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);

    /**
     * Busca productos en rango de precio con paginación
     */
    @Query("SELECT p FROM ProductEntity p " +
           "WHERE p.price BETWEEN :minPrice AND :maxPrice")
    Page<ProductEntity> findByPriceBetween(@Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice, Pageable pageable);

    // ============== CONSULTA COMPLEJA CON FILTROS Y PAGINACIÓN ==============

    /**
     * Busca productos con filtros opcionales y paginación
     * Todos los parámetros son opcionales excepto el Pageable
     */
    @Query("SELECT DISTINCT p FROM ProductEntity p " +
           "LEFT JOIN p.categories c " +
           "WHERE (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
           "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
           "AND (:categoryId IS NULL OR c.id = :categoryId)")
    Page<ProductEntity> findWithFilters(
        @Param("name") String name,
        @Param("minPrice") Double minPrice,
        @Param("maxPrice") Double maxPrice,
        @Param("categoryId") Long categoryId,
        Pageable pageable
    );

    /**
     * Busca productos de un usuario con filtros opcionales y paginación
     */
    @Query("SELECT DISTINCT p FROM ProductEntity p " +
           "LEFT JOIN p.categories c " +
           "WHERE p.owner.id = :userId " +
           "AND (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
           "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
           "AND (:categoryId IS NULL OR c.id = :categoryId)")
    Page<ProductEntity> findByUserIdWithFilters(
        @Param("userId") Long userId,
        @Param("name") String name,
        @Param("minPrice") Double minPrice,
        @Param("maxPrice") Double maxPrice,
        @Param("categoryId") Long categoryId,
        Pageable pageable);

    // ============== CONSULTAS CON SLICE PARA PERFORMANCE ==============

    /**
     * Productos de una categoría usando Slice
     */
    @Query("SELECT DISTINCT p FROM ProductEntity p " +
           "LEFT JOIN p.categories c " +
           "WHERE c.id = :categoryId " +
           "ORDER BY p.createdAt DESC")
    Slice<ProductEntity> findByCategoryIdOrderByCreatedAtDesc(@Param("categoryId") Long categoryId, Pageable pageable);

    /**
     * Productos creados después de una fecha usando Slice
     */
    @Query("SELECT p FROM ProductEntity p WHERE p.createdAt > :date ORDER BY p.createdAt DESC")
    Slice<ProductEntity> findCreatedAfter(@Param("date") LocalDateTime date, Pageable pageable);

    // ============== CONSULTAS DE CONTEO (PARA METADATOS) ==============

    /**
     * Cuenta productos con filtros (útil para estadísticas)
     */
    @Query("SELECT COUNT(DISTINCT p.id) FROM ProductEntity p " +
           "LEFT JOIN p.categories c " +
           "WHERE (COALESCE(:name, '') = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
           "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
           "AND (:categoryId IS NULL OR c.id = :categoryId)")
    long countWithFilters(
        @Param("name") String name,
        @Param("minPrice") Double minPrice,
        @Param("maxPrice") Double maxPrice,
        @Param("categoryId") Long categoryId
    );

}