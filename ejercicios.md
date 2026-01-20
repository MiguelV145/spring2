

### 1) Endpoint: **Validación de conflicto de negocio (nombre duplicado con reglas)**

**Caso de negocio**
No se permite crear ni actualizar un producto si ya existe otro producto con el **mismo nombre**, ignorando mayúsculas/minúsculas, **excepto si es el mismo ID**.

Esto evalúa:

* Regla de dominio
* Uso correcto del repositorio
* Diferenciación entre error de negocio y técnico

**Endpoint** `@PostMapping("/validate-name")`

```
POST /api/products/validate-name
```

**Request** `ValidateProductNameDto`

```json
{
  "id": 3,
  "name": "Laptop Gamer"
}
```

**Comportamiento esperado**

* Si existe otro producto con el mismo nombre → error de negocio
* Si no existe → OK

**Respuesta exitosa**

```json
{
  "valid": true
}
```

**Error de negocio**

* HTTP 409

```json
{
  "message": "Ya existe un producto con el nombre 'Laptop Gamer'"
}
```

**Por qué es complejo**

* No es CRUD
* Obliga a consultar BD
* Obliga a comparar IDs
* Introduce conflicto lógico del dominio


##  SOLUCIÓN - ENDPOINT 1

### 1. DTO - ValidateProductNameDto.java

```java
package ec.edu.ups.icc.fundamentos01.products.dtos;

public class ValidateProductNameDto {

    public int id;
    public String name;

}
```

### 2. Controller - ProductsController.java

```java
@PostMapping("/validate-name")
public ResponseEntity<Boolean> validateName(@RequestBody ValidateProductNameDto dto) {
    productService.validateName(dto.id, dto.name);
    return ResponseEntity.ok(true);
}
```

### 3. Interfaz Service - ProductService.java

```java
boolean validateName(Integer id, String name);
```

### 4. Implementación Service - ProductServiceImpl.java

```java
@Override
public boolean validateName(Integer id, String name) {
    productRepo.findByName(name)
            .ifPresent(existing -> {
                if (id == null || existing.getId() != id.longValue()) {
                    throw new ConflictException(
                            "Ya existe un producto con el nombre '" + name + "'");
                }
            });
    return true;
}
```

### 5. Repository - ProductRepository.java

```java
package ec.edu.ups.icc.fundamentos01.products.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ec.edu.ups.icc.fundamentos01.products.models.ProductEntity;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
    Optional<ProductEntity> findByName(String name);
}
```

### 6. Excepción - ConflictException.java

```java
package ec.edu.ups.icc.fundamentos01.exceptions.domain;

import org.springframework.http.HttpStatus;
import ec.edu.ups.icc.fundamentos01.exceptions.base.ApplicationException;

public class ConflictException extends ApplicationException {

    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
```


### 2) Endpoint: **Actualización condicionada por regla de negocio**

**Caso de negocio**
Un producto **no puede ser modificado** si su precio es mayor a cierto umbral sin una justificación explícita.

Ejemplo de regla:

* Si `price > 1000`
* Se exige un campo adicional `reason`

**Endpoint**

```
PUT /api/products/{id}/secure-update
```

**Request**

```json
{
  "name": "Laptop Pro",
  "price": 1500,
  "description": "Modelo empresarial",
  "reason": "Actualización por inflación"
}
```

**Reglas**

* Si `price <= 1000` → se actualiza normal
* Si `price > 1000` y `reason` es null o vacío → error de negocio

**Error de negocio**

* HTTP 422

```json
{
  "message": "Productos con precio mayor a 1000 requieren justificación"
}
```

**Por qué es complejo**

* Lógica condicional
* Validación de negocio cruzada entre campos
* No resoluble con anotaciones `@Valid`
* Obliga a decidir dónde vive la regla (modelo o servicio)


##  SOLUCIÓN  - ENDPOINT 2

### 1. DTO - SecureUpdateProductDto.java

```java
package ec.edu.ups.icc.fundamentos01.products.dtos;

public class SecureUpdateProductDto {

    public String name;
    public Double price;
    public String description;
    public String reason;

}
```

### 2. Controller - ProductsController.java

```java
// Agregar import
import ec.edu.ups.icc.fundamentos01.products.dtos.SecureUpdateProductDto;

// Agregar endpoint
@PutMapping("/{id}/secure-update")
public ProductResponseDto secureUpdate(@PathVariable("id") int id, @RequestBody SecureUpdateProductDto dto) {
    return productService.secureUpdate(id, dto);
}
```

### 3. Interfaz Service - ProductService.java

```java
ProductResponseDto secureUpdate(int id, SecureUpdateProductDto dto);
```

### 4. Implementación Service - ProductServiceImpl.java

```java
// Agregar import
import ec.edu.ups.icc.fundamentos01.exceptions.domain.BusinessException;
import ec.edu.ups.icc.fundamentos01.products.dtos.SecureUpdateProductDto;

// Implementación
@Override
public ProductResponseDto secureUpdate(int id, SecureUpdateProductDto dto) {
    ProductEntity entity = productRepo.findById((long) id)
            .orElseThrow(() -> new BusinessException("Producto no encontrado"));

    if (dto.price != null && dto.price > 1000) {
        if (dto.reason == null || dto.reason.isBlank()) {
            throw new BusinessException(
                    "Productos con precio mayor a 1000 requieren justificación"
            );
        }
    }

    Product product = Product.fromEntity(entity);

    if (dto.name != null) product.setName(dto.name);
    if (dto.price != null) product.setPrice(dto.price);
    if (dto.description != null) product.setDescription(dto.description);

    ProductEntity saved = productRepo.save(product.toEntity());

    return ProductMapper.toResponse(Product.fromEntity(saved));
}
```

### 5. Excepción - BusinessException.java

```java
package ec.edu.ups.icc.fundamentos01.exceptions.domain;

import org.springframework.http.HttpStatus;
import ec.edu.ups.icc.fundamentos01.exceptions.base.ApplicationException;

public class BusinessException extends ApplicationException {

    public BusinessException(String message) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, message);
    }

    protected BusinessException(HttpStatus status, String message) {
        super(status, message);
    }
}
```


## PUNTOS DE LA SOLUCIÓN

### Endpoint 1: Validación de Nombre
1. **DTO simple**: Solo contiene `id` y `name`
2. **Lógica de negocio**: Usa `Optional.ifPresent()` para validar duplicados
3. **Comparación de IDs**: Permite mismo nombre si es el mismo producto (`id` coincide)
4. **Excepción HTTP 409**: `ConflictException` para conflictos de negocio
5. **Repository**: Método `findByName()` para búsqueda por nombre

### Endpoint 2: Actualización Segura
1. **DTO con reason**: Campo adicional `reason` para justificación
2. **Validación condicional**: Solo valida `reason` si `price > 1000`
3. **Actualización selectiva**: Solo actualiza campos no nulos
4. **Excepción HTTP 422**: `BusinessException` para errores de validación de negocio
5. **Uso del modelo de dominio**: Convierte Entity → Domain → Entity
