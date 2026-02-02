# Programación y Plataformas Web

# **Spring Boot – Paginación de Datos con Spring Data JPA: Optimización y User Experience**

<div align="center">
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/spring/spring-original.svg" width="95">
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/postgresql/postgresql-original.svg" width="95">
</div>

## **Práctica 10 (Spring Boot): Paginación, Page y Slice con Request Parameters**

### **Autores**

**Pablo Torres**

📧 [mvanegasp@est.ups.edu.ec](mailto:mvanegasp@est.ups.edu.ec)

💻 GitHub: MiguelV145

# **1. Introducción a la Paginación en Spring Boot**

En el tema anterior implementamos **filtros con Request Parameters** en consultas relacionadas. Ahora necesitamos **paginar los resultados** para manejar grandes volúmenes de datos eficientemente.

Los principales problemas sin paginación son:

* **Consultas masivas**: Devolver 100,000 productos consume excesiva memoria
* **Tiempo de respuesta lento**: Transferir todos los datos a la vez
* **Sobrecarga de red**: Grandes payloads JSON
* **Experiencia de usuario deficiente**: Largos tiempos de espera
* **Problemas de escalabilidad**: El sistema no funciona con millones de registros

## **1.1. Spring Data JPA Pagination**

Spring Data JPA proporciona soporte nativo para paginación a través de:

* **Pageable**: Interface para especificar parámetros de paginación
* **Page**: Interface que encapsula resultados paginados con metadatos
* **Slice**: Interface ligera para navegación secuencial
* **PageRequest**: Implementación concreta de Pageable

### **Ejemplo conceptual**

```java
// Parámetros de entrada
Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());

// Resultado paginado
Page<ProductEntity> page = productRepository.findAll(pageable);
```

## **1.2. Ventajas de Spring Data JPA Pagination**

* **Automático**: No se escribe SQL de paginación manualmente
* **Type-safe**: Completamente tipado con generics
* **Flexible**: Se combina con consultas personalizadas
* **Optimizado**: Genera SQL eficiente con LIMIT y OFFSET
* **Integrado**: Funciona perfectamente con el ecosistema Spring

# **2. Tipos de Paginación en Spring Boot**

## **2.1. Page vs Slice**

### **Page (Paginación Completa)**

**Características**:
* Incluye **count total** de registros
* Permite **navegación a cualquier página**
* Proporciona **metadatos completos**
* **Más costosa** (requiere consulta COUNT adicional)

```java
Page<ProductEntity> page = productRepository.findAll(pageable);
// Genera: SELECT COUNT(*) FROM products + SELECT * FROM products LIMIT 10 OFFSET 0
```

### **Slice (Paginación Ligera)**

**Características**:
* **NO incluye count total**
* Solo navegación **anterior/siguiente**
* **Más eficiente** (una sola consulta)
* Ideal para **feeds infinitos**

```java
Slice<ProductEntity> slice = productRepository.findAll(pageable);
// Genera: SELECT * FROM products LIMIT 11 OFFSET 0 (trae uno extra para hasNext)
```

### **¿Cuándo usar cada tipo?**

| Escenario | Usar Page | Usar Slice |
|-----------|-----------|------------|
| **Navegación con números de página** | ✅ SÍ | ❌ |
| **Necesitas mostrar "Página X de Y"** | ✅ SÍ | ❌ |
| **Feeds de redes sociales** | ❌ | ✅ SÍ |
| **Performance crítica** | ⚠️ Depende | ✅ SÍ |
| **Scroll infinito** | ❌ | ✅ SÍ |
| **Reportes con totales** | ✅ SÍ | ❌ |

## **2.2. PageRequest - Construcción de Paginación**

```java
// Página 0, tamaño 10, sin ordenamiento
Pageable pageable = PageRequest.of(0, 10);

// Con ordenamiento ascendente por nombre
Pageable pageable = PageRequest.of(0, 10, Sort.by("name"));

// Con ordenamiento descendente por precio
Pageable pageable = PageRequest.of(0, 10, Sort.by("price").descending());

// Con múltiples criterios de ordenamiento
Pageable pageable = PageRequest.of(0, 10, 
    Sort.by("category.name").and(Sort.by("price").descending()));
```

# **3. Implementación de Paginación en ProductController**

Continuando con los endpoints del tema anterior, agregaremos paginación a los productos.

## **3.1. ProductController - Endpoints con Paginación**

Archivo: `products/controllers/ProductController.java`

```java
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // ============== PAGINACIÓN BÁSICA ==============

    /**
     * Lista todos los productos con paginación básica
     * Ejemplo: GET /api/products?page=0&size=10&sort=name,asc
     */
    @GetMapping
    public ResponseEntity<Page<ProductResponseDto>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String[] sort) {

        Page<ProductResponseDto> products = productService.findAll(page, size, sort);
        return ResponseEntity.ok(products);
    }

    // ============== PAGINACIÓN CON SLICE (PERFORMANCE) ==============

    /**
     * Lista productos usando Slice para mejor performance
     * Ejemplo: GET /api/products/slice?page=0&size=10&sort=createdAt,desc
     */
    @GetMapping("/slice")
    public ResponseEntity<Slice<ProductResponseDto>> findAllSlice(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String[] sort) {

        Slice<ProductResponseDto> products = productService.findAllSlice(page, size, sort);
        return ResponseEntity.ok(products);
    }

    // ============== PAGINACIÓN CON FILTROS (CONTINUANDO TEMA 09) ==============

    /**
     * Lista productos con filtros y paginación
     * Ejemplo: GET /api/products/search?name=laptop&minPrice=500&page=0&size=5
     */
    @GetMapping("/search")
    public ResponseEntity<Page<ProductResponseDto>> findWithFilters(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String[] sort) {

        Page<ProductResponseDto> products = productService.findWithFilters(
            name, minPrice, maxPrice, categoryId, page, size, sort);
        
        return ResponseEntity.ok(products);
    }

    // ============== USUARIOS CON SUS PRODUCTOS PAGINADOS ==============

    /**
     * Productos de un usuario específico con paginación
     * Ejemplo: GET /api/products/user/1?page=0&size=5&sort=price,desc
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<ProductResponseDto>> findByUserId(
            @PathVariable Long userId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String[] sort) {

        Page<ProductResponseDto> products = productService.findByUserIdWithFilters(
            userId, name, minPrice, maxPrice, categoryId, page, size, sort);
        
        return ResponseEntity.ok(products);
    }

    // ============== OTROS ENDPOINTS EXISTENTES ==============
    
   
}
```

### **Aspectos clave del controlador**

1. **Parámetros de paginación estándar**: `page`, `size`, `sort[]`
2. **Valores por defecto**: Página 0, tamaño 10, orden por ID
3. **Múltiples estrategias**: Page, Slice, filtros + paginación
4. **Flexibilidad**: Se pueden combinar filtros con paginación
5. **Convenciones REST**: Mantiene la semántica HTTP correcta

## **3.2. Validación avanzada de parámetros de paginación**

Para mayor robustez, podemos crear validaciones personalizadas:

Archivo: `shared/dto/PageableDto.java`

```java
import javax.validation.constraints.*;

public class PageableDto {

    @Min(value = 0, message = "La página debe ser mayor o igual a 0")
    private int page = 0;

    @Min(value = 1, message = "El tamaño debe ser mayor a 0")
    @Max(value = 100, message = "El tamaño no puede ser mayor a 100")
    private int size = 10;

    private String[] sort = {"id"};

    // Constructores
    public PageableDto() {
    }

    public PageableDto(int page, int size, String[] sort) {
        this.page = page;
        this.size = size;
        this.sort = sort;
    }

    // Getters y setters
    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String[] getSort() {
        return sort;
    }

    public void setSort(String[] sort) {
        this.sort = sort;
    }

    // ============== MÉTODO HELPER ==============

    /**
     * Convierte a PageRequest de Spring Data JPA
     */
    public Pageable toPageable() {
        return PageRequest.of(page, size, createSort());
    }

    private Sort createSort() {
        if (sort == null || sort.length == 0) {
            return Sort.by("id");
        }

        Sort.Order[] orders = new Sort.Order[sort.length];
        for (int i = 0; i < sort.length; i++) {
            String[] parts = sort[i].split(",");
            String property = parts[0];
            String direction = parts.length > 1 ? parts[1] : "asc";
            
            orders[i] = "desc".equalsIgnoreCase(direction) 
                ? Sort.Order.desc(property)
                : Sort.Order.asc(property);
        }
        
        return Sort.by(orders);
    }
}
```

# **4. Implementación del ProductService con Paginación**

## **4.1. Actualización de ProductService interface**

Archivo: `products/services/ProductService.java`

```java
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;

public interface ProductService {

    // ============== MÉTODOS BÁSICOS EXISTENTES ==============
    ProductResponseDto create(CreateProductDto createProductDto);
    ProductResponseDto findById(Long id);
    ProductResponseDto update(Long id, UpdateProductDto updateProductDto);
    void delete(Long id);

    // ============== MÉTODOS CON PAGINACIÓN ==============

    /**
     * Obtiene todos los productos con paginación completa (Page)
     */
    Page<ProductResponseDto> findAll(int page, int size, String[] sort);

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
```

## **4.2. Implementación de ProductServiceImpl**

Archivo: `products/services/ProductServiceImpl.java`

```java
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public ProductServiceImpl(ProductRepository productRepository, 
                            UserRepository userRepository,
                            CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    // ============== MÉTODOS BÁSICOS EXISTENTES ==============
    // (implementaciones previas del tema 08 y 09)

    // ============== MÉTODOS CON PAGINACIÓN ==============

    @Override
    public Page<ProductResponseDto> findAll(int page, int size, String[] sort) {
        Pageable pageable = createPageable(page, size, sort);
        Page<ProductEntity> productPage = productRepository.findAll(pageable);
        
        return productPage.map(this::toResponseDto);
    }

    @Override
    public Slice<ProductResponseDto> findAllSlice(int page, int size, String[] sort) {
        Pageable pageable = createPageable(page, size, sort);
        Slice<ProductEntity> productSlice = productRepository.findAll(pageable);
        
        return productSlice.map(this::toResponseDto);
    }

    @Override
    public Page<ProductResponseDto> findWithFilters(
            String name, Double minPrice, Double maxPrice, Long categoryId,
            int page, int size, String[] sort) {
        
        // Validaciones de filtros (del tema 09)
        validateFilterParameters(minPrice, maxPrice);
        
        // Crear Pageable
        Pageable pageable = createPageable(page, size, sort);
        
        // Consulta con filtros y paginación
        Page<ProductEntity> productPage = productRepository.findWithFilters(
            name, minPrice, maxPrice, categoryId, pageable);
        
        return productPage.map(this::toResponseDto);
    }

    @Override
    public Page<ProductResponseDto> findByUserIdWithFilters(
            Long userId, String name, Double minPrice, Double maxPrice, Long categoryId,
            int page, int size, String[] sort) {
        
        // 1. Validar que el usuario existe
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Usuario no encontrado con ID: " + userId);
        }
        
        // 2. Validar filtros
        validateFilterParameters(minPrice, maxPrice);
        
        // 3. Crear Pageable
        Pageable pageable = createPageable(page, size, sort);
        
        // 4. Consulta con filtros y paginación
        Page<ProductEntity> productPage = productRepository.findByUserIdWithFilters(
            userId, name, minPrice, maxPrice, categoryId, pageable);
        
        return productPage.map(this::toResponseDto);
    }

    // ============== MÉTODOS HELPER ==============

    private Pageable createPageable(int page, int size, String[] sort) {
        // Validar parámetros
        if (page < 0) {
            throw new BadRequestException("La página debe ser mayor o igual a 0");
        }
        if (size < 1 || size > 100) {
            throw new BadRequestException("El tamaño debe estar entre 1 y 100");
        }
        
        // Crear Sort
        Sort sortObj = createSort(sort);
        
        return PageRequest.of(page, size, sortObj);
    }

    private Sort createSort(String[] sort) {
        if (sort == null || sort.length == 0) {
            return Sort.by("id");
        }

        List<Sort.Order> orders = new ArrayList<>();
        for (String sortParam : sort) {
            String[] parts = sortParam.split(",");
            String property = parts[0];
            String direction = parts.length > 1 ? parts[1] : "asc";
            
            // Validar propiedades permitidas para evitar inyección SQL
            if (!isValidSortProperty(property)) {
                throw new BadRequestException("Propiedad de ordenamiento no válida: " + property);
            }
            
            Sort.Order order = "desc".equalsIgnoreCase(direction) 
                ? Sort.Order.desc(property)
                : Sort.Order.asc(property);
            
            orders.add(order);
        }
        
        return Sort.by(orders);
    }

    private boolean isValidSortProperty(String property) {
        // Lista blanca de propiedades permitidas para ordenamiento
        Set<String> allowedProperties = Set.of(
            "id", "name", "price", "createdAt", "updatedAt",
            "owner.name", "owner.email", "category.name"
        );
        return allowedProperties.contains(property);
    }

    private void validateFilterParameters(Double minPrice, Double maxPrice) {
        if (minPrice != null && minPrice < 0) {
            throw new BadRequestException("El precio mínimo no puede ser negativo");
        }
        
        if (maxPrice != null && maxPrice < 0) {
            throw new BadRequestException("El precio máximo no puede ser negativo");
        }
        
        if (minPrice != null && maxPrice != null && maxPrice < minPrice) {
            throw new BadRequestException("El precio máximo debe ser mayor o igual al precio mínimo");
        }
    }

    private ProductResponseDto toResponseDto(ProductEntity product) {
        ProductResponseDto dto = new ProductResponseDto();
        
        dto.id = product.getId();
        dto.name = product.getName();
        dto.price = product.getPrice();
        dto.description = product.getDescription();
        dto.createdAt = product.getCreatedAt();
        dto.updatedAt = product.getUpdatedAt();
        
        // Información del usuario (owner)
        dto.user = new ProductResponseDto.UserSummaryDto();
        dto.user.id = product.getOwner().getId();
        dto.user.name = product.getOwner().getName();
        dto.user.email = product.getOwner().getEmail();
        
        // Información de las categorías (relación Many-to-Many)
        List<ProductResponseDto.CategoryResponseDto> categoryDtos = new ArrayList<>();
        for (CategoryEntity categoryEntity : product.getCategories()) {
            ProductResponseDto.CategoryResponseDto categoryDto = new ProductResponseDto.CategoryResponseDto();
            categoryDto.id = categoryEntity.getId();
            categoryDto.name = categoryEntity.getName();
            categoryDto.description = categoryEntity.getDescription();
            categoryDtos.add(categoryDto);
        }
        dto.categories = categoryDtos;
        
        return dto;
    }
}
```

### **Aspectos clave del servicio**

1. **Page.map()**: Convierte Page<Entity> a Page<DTO> automáticamente
2. **Validación de parámetros**: Página, tamaño y propiedades de ordenamiento
3. **Lista blanca**: Solo permite ordenamiento por propiedades seguras
4. **Combinación**: Filtros + paginación en la misma consulta
5. **Performance**: Usa las capacidades nativas de Spring Data JPA

# **5. Actualización del ProductRepository con Paginación**

## **5.1. ProductRepository - Consultas con Pageable**

Archivo: `products/repositories/ProductRepository.java`

```java
@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    // ============== CONSULTAS BÁSICAS (HEREDA AUTOMÁTICAMENTE) ==============
    // Page<ProductEntity> findAll(Pageable pageable) - Viene de JpaRepository
    // Slice<ProductEntity> findAll(Pageable pageable) - Viene de JpaRepository

    // ============== CONSULTAS PERSONALIZADAS CON PAGINACIÓN ==============

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
    Page<ProductEntity> findByPriceBetween(Double minPrice, Double maxPrice, Pageable pageable);

    // ============== CONSULTA COMPLEJA CON FILTROS Y PAGINACIÓN ==============

    /**
     * Busca productos con filtros opcionales y paginación
     * Todos los parámetros son opcionales excepto el Pageable
     * NOTA: Usa LEFT JOIN p.categories para relación Many-to-Many
     */
    @Query("SELECT DISTINCT p FROM ProductEntity p " +
           "LEFT JOIN p.categories c " +
           "WHERE (COALESCE(:name, '') = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
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
     * NOTA: Usa LEFT JOIN p.categories para relación Many-to-Many
     */
    @Query("SELECT DISTINCT p FROM ProductEntity p " +
           "LEFT JOIN p.categories c " +
           "WHERE p.owner.id = :userId " +
           "AND (COALESCE(:name, '') = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
           "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
           "AND (:categoryId IS NULL OR c.id = :categoryId)")
    Page<ProductEntity> findByUserIdWithFilters(
        @Param("userId") Long userId,
        @Param("name") String name,
        @Param("minPrice") Double minPrice,
        @Param("maxPrice") Double maxPrice,
        @Param("categoryId") Long categoryId,
        Pageable pageable
    );

    // ============== CONSULTAS CON SLICE PARA PERFORMANCE ==============

    /**
     * Productos de una categoría usando Slice
     * Usa LEFT JOIN para relación Many-to-Many
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
     * NOTA: Usa COUNT(DISTINCT p.id) por la relación Many-to-Many
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
```

### **Aspectos técnicos importantes**

1. **Automático**: JpaRepository ya proporciona `findAll(Pageable)`
2. **@Query + Pageable**: Se pueden combinar consultas personalizadas con paginación
3. **Slice vs Page**: Mismo método, diferente tipo de retorno
4. **Ordenamiento**: Se especifica en el Pageable, no en la consulta
5. **Performance**: Spring Data JPA genera SQL optimizado automáticamente

### **SQL generado por Spring Data JPA**

```sql
-- Para Page con filtros (consulta principal + count)
-- NOTA: Usa DISTINCT porque la relación Many-to-Many puede generar duplicados
SELECT DISTINCT p.*, o.* FROM products p 
JOIN users o ON p.user_id = o.id 
LEFT JOIN product_categories pc ON p.id = pc.product_id
LEFT JOIN categories c ON pc.category_id = c.id
WHERE (COALESCE(:name, '') = '' OR LOWER(p.name) LIKE LOWER('%' || :name || '%'))
  AND (:minPrice IS NULL OR p.price >= :minPrice)
  AND (:maxPrice IS NULL OR p.price <= :maxPrice)
  AND (:categoryId IS NULL OR c.id = :categoryId)
ORDER BY p.created_at DESC 
LIMIT 10 OFFSET 0;

-- COUNT query automática para Page
-- NOTA: Usa COUNT(DISTINCT p.id) para evitar contar duplicados
SELECT COUNT(DISTINCT p.id) FROM products p 
LEFT JOIN product_categories pc ON p.id = pc.product_id
LEFT JOIN categories c ON pc.category_id = c.id
WHERE [...same conditions...];

-- Para Slice (solo consulta principal, trae uno extra)
SELECT DISTINCT p.*, o.* FROM products p 
[...same query...] 
LIMIT 11 OFFSET 0;  -- Trae 11 para saber si hasNext
```

# **6. Respuestas JSON con Metadatos de Paginación**

## **6.1. Estructura de Page Response**

```json
{
  "content": [
    {
      "id": 1,
      "name": "Laptop Gaming",
      "price": 1200.00,
      "description": "High performance laptop",
      "user": {
        "id": 1,
        "name": "Juan Pérez",
        "email": "juan@email.com"
      },
      "categories": [
        {
          "id": 2,
          "name": "Electrónicos",
          "description": "Dispositivos electrónicos"
        },
        {
          "id": 3,
          "name": "Gaming",
          "description": "Productos para videojuegos"
        }
      ],
      },
      "createdAt": "2024-01-15T10:30:00",
      "updatedAt": "2024-01-15T10:30:00"
    }
    // ... más productos ...
  ],
  "pageable": {
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    },
    "pageNumber": 0,
    "pageSize": 10,
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "totalPages": 125,
  "totalElements": 1250,
  "last": false,
  "first": true,
  "numberOfElements": 10,
  "size": 10,
  "number": 0,
  "sort": {
    "sorted": true,
    "unsorted": false,
    "empty": false
  },
  "empty": false
}
```

## **6.2. Estructura de Slice Response**

```json
{
  "content": [
    // ... productos ...
  ],
  "pageable": {
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    },
    "pageNumber": 0,
    "pageSize": 10,
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "numberOfElements": 10,
  "size": 10,
  "number": 0,
  "first": true,
  "last": false,
  "hasNext": true,
  "hasPrevious": false,
  "sort": {
    "sorted": true,
    "unsorted": false,
    "empty": false
  },
  "empty": false
}
```

### **Diferencias clave**

| Metadato | Page | Slice |
|----------|------|-------|
| **totalElements** | ✅ Incluido | ❌ NO incluido |
| **totalPages** | ✅ Incluido | ❌ NO incluido |
| **hasNext** | ✅ Calculado | ✅ Verificado |
| **hasPrevious** | ✅ Calculado | ✅ Verificado |
| **Performance** | ⚠️ 2 consultas | ✅ 1 consulta |

# **7. Optimizaciones y Consideraciones de Performance**

## **7.1. Índices de Base de Datos**

Para consultas paginadas eficientes, crear índices en:

```sql
-- Índices básicos para ordenamiento
CREATE INDEX idx_products_id ON products(id);
CREATE INDEX idx_products_created_at ON products(created_at);
CREATE INDEX idx_products_updated_at ON products(updated_at);
CREATE INDEX idx_products_name ON products(name);
CREATE INDEX idx_products_price ON products(price);

-- Índices para filtros
CREATE INDEX idx_products_user_id ON products(user_id);
CREATE INDEX idx_products_category_id ON products(category_id);

-- Índices compuestos para consultas complejas
CREATE INDEX idx_products_user_created ON products(user_id, created_at DESC);
CREATE INDEX idx_products_category_price ON products(category_id, price);
CREATE INDEX idx_products_price_created ON products(price, created_at DESC);

-- Índice para búsqueda de texto (opcional)
CREATE INDEX idx_products_name_gin ON products USING gin(to_tsvector('spanish', name));
```

## **7.2. Límites y Validaciones**

```java
// Configuración de límites
public class PaginationConfig {
    public static final int MIN_PAGE_SIZE = 1;
    public static final int MAX_PAGE_SIZE = 100;
    public static final int DEFAULT_PAGE_SIZE = 10;
    
    public static final int MAX_PAGE_NUMBER = 1000; // Prevenir páginas muy altas
}
```

## **7.3. Estrategias según el Caso de Uso**

### **Para Listados Administrativos**
```java
// Usar Page con metadatos completos
@GetMapping("/admin/products")
public Page<ProductResponseDto> adminProducts(Pageable pageable) {
    return productService.findAll(pageable);
}
```

### **Para Feeds de Usuarios**
```java
// Usar Slice para mejor performance
@GetMapping("/feed")
public Slice<ProductResponseDto> feed(Pageable pageable) {
    return productService.findRecentProducts(pageable);
}
```

### **Para Búsquedas**
```java
// Combinar filtros con paginación
@GetMapping("/search")
public Page<ProductResponseDto> search(
    @RequestParam String query,
    Pageable pageable) {
    return productService.search(query, pageable);
}
```

## **7.4. Problemas Comunes y Soluciones**

### **Problema: Páginas muy altas**
```java
// Solución: Limitar número máximo de páginas
if (page > MAX_PAGE_NUMBER) {
    throw new BadRequestException("Página muy alta, usar búsqueda en su lugar");
}
```

### **Problema: Ordenamiento por campos no indexados**
```java
// Solución: Lista blanca de campos permitidos
private static final Set<String> ALLOWED_SORT_PROPERTIES = Set.of(
    "id", "name", "price", "createdAt", "updatedAt"
);
```

### **Problema: Consultas COUNT lentas**
```java
// Solución: Usar Slice cuando no se necesita count total
public Slice<ProductResponseDto> findForFeed(Pageable pageable) {
    return productRepository.findRecentProducts(pageable);
}
```

# **8. Actividad Práctica Completa**

## **8.1. Implementación requerida**

El estudiante debe implementar:

1. **Actualizar ProductController** con endpoints paginados:
   - `GET /api/products?page=0&size=10&sort=name,asc`
   - `GET /api/products/slice?page=0&size=10`
   - `GET /api/products/search?name=laptop&page=0&size=5`
   - `GET /api/products/user/1?page=0&size=5&sort=price,desc`

2. **Implementar métodos** en `ProductService`:
   - `findAll(page, size, sort)` con Page
   - `findAllSlice(page, size, sort)` con Slice
   - `findWithFilters(...)` con filtros y paginación
   - `findByUserIdWithFilters(...)` combinando todo

3. **Actualizar ProductRepository** con consultas paginadas:
   - Usar `@Query` con `Pageable` parameter
   - Implementar consultas con filtros opcionales

4. **Validaciones robustas**:
   - Límites de página y tamaño
   - Lista blanca de propiedades de ordenamiento
   - Combinación de filtros y paginación

## **8.2. Casos de prueba específicos**

**Probar los siguientes casos**:

```bash
# 1. Paginación básica
GET /api/products?page=0&size=5

# 2. Paginación con ordenamiento
GET /api/products?page=1&size=10&sort=price,desc

# 3. Paginación con ordenamiento múltiple
GET /api/products?page=0&size=5&sort=category.name,asc&sort=price,desc

# 4. Slice para performance
GET /api/products/slice?page=0&size=10&sort=createdAt,desc

# 5. Búsqueda con filtros y paginación
GET /api/products/search?name=gaming&minPrice=500&page=0&size=3

# 6. Productos de usuario con paginación
GET /api/products/user/1?page=0&size=5&sort=name,asc

# 7. Casos de error
GET /api/products?page=-1&size=0  # Error de validación
GET /api/products?sort=invalidField,asc  # Campo no permitido
```

## **8.3. Verificaciones técnicas**

1. **SQL generado**: Verificar LIMIT y OFFSET en los logs
2. **Metadatos**: Confirmar que Page incluye totalElements y totalPages
3. **Performance**: Comparar tiempos Page vs Slice
4. **Índices**: Verificar que las consultas usen índices apropiados
5. **Validaciones**: Probar límites de página y tamaño

# **9. Resultados y Evidencias Requeridas**

## **9.1. Datos para revisión**

**Usar un dataset de al menos 1000 productos**:
Crear un script de carga masiva para poblar la base de datos con datos variados:
- al menos 5 usuarios
- alemnos 2 categorias por producto  
- Precios variados ($10 - $5000)
- Nombres con texto buscable

## **9.2. Evidencias de funcionamiento** Caputuras de Postman Bruno o similar mostrando respuestas correctas
1. **Page response**: `GET /api/products?page=0&size=5` mostrando metadatos completos

![paginated rensponse](./image/Paginated.png)

2. **Slice response**: `GET /api/products/slice?page=0&size=5` sin totalElements

![paginated rensponse](./image/slice.png)

3. **Filtros + paginación**: `GET /api/products/search?name=laptop&page=0&size=3`

![paginated rensponse](./image/search.png)

4. **Ordenamiento**: `GET /api/products?sort=price,desc&page=1&size=5`

![paginated rensponse](./image/orden.png)


## **9.3. Evidencias de performance**
1. **Comparación**: Tiempos de respuesta Page vs Slice

**Consultas de prueba con volumen**: para cada uno Page y Slice
1. Primera página de productos (page=0, size=10)
2. Página intermedia (page=5, size=10) 
3. Últimas páginas para verificar performance
4. Búsquedas con pocos y muchos resultados
5. Ordenamiento por diferentes campos

# **10. Conclusiones**

Esta implementación de paginación en Spring Boot demuestra:

* **Paginación nativa**: Uso completo de Spring Data JPA Pageable
* **Flexibilidad**: Page vs Slice según necesidades de performance
* **Integración**: Filtros + paginación + ordenamiento en una sola consulta
* **Escalabilidad**: Funciona eficientemente con millones de registros
* **Usabilidad**: APIs REST estándar con metadatos completos
* **Performance**: Consultas optimizadas con índices apropiados

El enfoque implementado proporciona una base sólida para aplicaciones que requieren manejar grandes volúmenes de datos de manera eficiente, manteniendo una excelente experiencia de usuario y siguiendo las mejores prácticas de Spring Boot.


-----

### PRACTICA 11 (CAPTURAS)
1. **Login**: `POST /auth/login`


![login](./image/login.png)


2. **register**: `POST /auth/register`

v![refister ](./image/register.png)

3. **SIN TOKEN**: `GET /api/users`

![sintoken ](./image/sinToken.png)


