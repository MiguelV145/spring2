#!/usr/bin/env python3
"""
Script para probar los endpoints de Relación de Entidades
Fase 1: Relaciones 1:N (One-to-Many) - User -> Products, Category -> Products
Fase 2: Relaciones N:N (Many-to-Many) - Products <-> Categories

Basado en: 08_relacion_entidades.md
Autor: GitHub Copilot
Puntuación: /10 puntos
"""

import sys
import subprocess
import json
from typing import Dict, List, Optional

# -----------------------------
# Dependencia obligatoria
# -----------------------------
try:
    import requests
except ImportError:
    print("ERROR: La librería 'requests' no está instalada.")
    print("Ejecute:")
    print("    python3 -m pip install requests")
    sys.exit(1)

# -----------------------------
# Configuración
# -----------------------------
BASE_URL = "http://localhost:8080"
API_PRODUCTS = f"{BASE_URL}/api/products"
API_USERS = f"{BASE_URL}/api/users"
API_CATEGORIES = f"{BASE_URL}/api/categories"

SCORE = 0
MAX_SCORE = 10

# Variables de control
test_user_id = None
test_category_id = None
test_product_id = None
initial_product_count = 0

# -----------------------------
# Utilidades
# -----------------------------
def last_commit():
    """Muestra información del último commit"""
    print("=== INFORMACIÓN DEL REPOSITORIO ===")
    try:
        result = subprocess.run(
            ["git", "log", "-1", "--format=%cd - %s", "--date=iso"],
            capture_output=True, text=True, cwd="."
        )
        if result.returncode == 0:
            print(f"Último commit: {result.stdout.strip()}")
        else:
            print("No se pudo obtener información del commit")
    except Exception:
        print("Git no disponible o no es un repositorio")
    print("-" * 60)


def safe_request(fn, description: str, expected_status: int = [200]):
    """Ejecuta una función de request de forma segura"""
    try:
        response = fn()
        if response.status_code in expected_status:
            print(f"[OK] {description}")
            return response
        else:
            print(f"[ERROR] {description} - Status: {response.status_code} - Status Esperado: {expected_status}")
            if response.text:
                print(f"   Error: {response.text[:200]}")
            return None
    except Exception as e:
        print(f"[ERROR] {description} - Error: {str(e)[:200]}")
        return None


def print_test_header(test_name: str, points: int):
    """Imprime encabezado de prueba"""
    print(f"\n[TEST] {test_name} ({points} punto{'s' if points != 1 else ''})")
    print("-" * 50)


def add_points(points: int, description: str):
    """Agrega puntos al score total"""
    global SCORE
    SCORE += points
    print(f"   [+] +{points} punto{'s' if points != 1 else ''}: {description}")


# -----------------------------
# Funciones de API
# -----------------------------
def create_test_user():
    """Crea un usuario de prueba"""
    user_data = {
        "name": "Test User Relations",
        "email": f"test.relations.{subprocess.time.time_ns()}@test.com",
        "password": "TestPassword123"
    }
    return requests.post(API_USERS, json=user_data)


def create_test_category():
    """Crea una categoría de prueba"""
    category_data = {
        "name": f"Test Category {subprocess.time.time_ns()}",
        "descripcion": "Categoría de prueba para relaciones"
    }
    return requests.post(API_CATEGORIES, json=category_data)


def get_all_users():
    """Obtiene todos los usuarios"""
    return requests.get(API_USERS)


def get_all_categories():
    """Obtiene todas las categorías"""
    return requests.get(API_CATEGORIES)


def get_all_products():
    """Obtiene todos los productos"""
    return requests.get(API_PRODUCTS)


def create_product(name: str, price: float, description: str, user_id: int, category_id: int):
    """Crea un producto con relaciones"""
    product_data = {
        "name": name,
        "price": price,
        "description": description,
        "userId": user_id,
        "categoryId": category_id
    }
    return requests.post(API_PRODUCTS, json=product_data)


def get_product_by_id(product_id: int):
    """Obtiene un producto por ID"""
    return requests.get(f"{API_PRODUCTS}/{product_id}")


def get_products_by_user(user_id: int):
    """Obtiene productos de un usuario específico"""
    return requests.get(f"{API_PRODUCTS}/user/{user_id}")


def get_products_by_category(category_id: int):
    """Obtiene productos de una categoría específica"""
    return requests.get(f"{API_PRODUCTS}/category/{category_id}")


def update_product(product_id: int, name: str, price: float, description: str, category_id: int):
    """Actualiza un producto"""
    update_data = {
        "name": name,
        "price": price,
        "description": description,
        "categoryId": category_id
    }
    return requests.put(f"{API_PRODUCTS}/{product_id}", json=update_data)


def delete_product(product_id: int):
    """Elimina un producto"""
    return requests.delete(f"{API_PRODUCTS}/{product_id}")


# -----------------------------
# PRUEBAS PRINCIPALES
# -----------------------------
def test_setup():
    """Configuración inicial - usar datos existentes"""
    global test_user_id, test_category_id, initial_product_count
    
    print_test_header("CONFIGURACIÓN INICIAL", 1)
    
    # Contar productos iniciales
    response = safe_request(get_all_products, "Contar productos iniciales")
    if response:
        initial_product_count = len(response.json())
        print(f"   [INFO] Productos iniciales: {initial_product_count}")
        add_points(0.5, "Conteo inicial de productos obtenido")

    
    # Obtener primer usuario existente
    response = safe_request(get_all_users, "Obtener usuarios existentes")
    if response:
        users = response.json()
        if isinstance(users, list) and len(users) > 0:
            test_user_id = users[0].get('id')
            user_name = users[0].get('name', 'N/A')
            print(f"   [USER] Usuario encontrado con ID: {test_user_id} - Nombre: {user_name}")
            add_points(0.5, "Usuario existente seleccionado")
        else:
            print("   [ERROR] No se encontraron usuarios existentes")
    
# Obtener primera categoría existente
    # response = safe_request(get_all_categories, "Obtener categorías existentes")
    # if response:
    #     categories = response.json()
    #     if isinstance(categories, list) and len(categories) > 0:
    #         test_category_id = categories[0].get('id')
    #         category_name = categories[0].get('name', 'N/A')
    #         print(f"   📁 Categoría encontrada con ID: {test_category_id} - Nombre: {category_name}")
    #     else:
    #         print("   ❌ No se encontraron categorías existentes")
    #  add_points(0.5, "Categoría existente seleccionada")
    test_category_id =1
    return test_user_id is not None and test_category_id is not None


def test_create_product():
    """Prueba creación de producto con relaciones 1:N"""
    global test_product_id
    
    print_test_header("CREAR PRODUCTO CON RELACIONES 1:N", 2)
    
    if not test_user_id or not test_category_id:
        print("[ERROR] No se pueden crear productos sin usuario y categoría")
        return False
    
    response = safe_request(
        lambda: create_product(
            "Laptop Test Relations" + str(subprocess.time.time_ns()), 
            1299.99, 
            "Laptop para pruebas de relaciones", 
            test_user_id, 
            test_category_id
        ),
        "Crear producto con userId y categoryId",
        [201,200]
    )
    
    if response:
        product_data = response.json()
        test_product_id = product_data.get('id')
        
        # Validar que la respuesta incluya información de relaciones
        if 'ownerName' in product_data or 'owner' in product_data or 'user' in product_data:
            add_points(1, "Producto creado con relación a usuario")
        
        if 'categoryName' in product_data or 'category' in product_data:
            add_points(1, "Producto creado con relación a categoría")
        
        print(f"   [PRODUCT] Producto creado con ID: {test_product_id}")
        return True
    
    return False


def test_get_product_by_id():
    """Prueba obtener producto por ID (debe incluir relaciones)"""
    print_test_header("OBTENER PRODUCTO POR ID", 1)
    
    if not test_product_id:
        print("[ERROR] No hay producto de prueba para consultar")
        return False
    
    response = safe_request(
        lambda: get_product_by_id(test_product_id),
        f"Obtener producto ID {test_product_id}"
    )
    
    if response:
        product_data = response.json()
        
        # Validar que incluya información de relaciones
        has_owner_info = 'ownerName' in product_data or 'owner' in product_data or 'user' in product_data
        has_category_info = 'categoryName' in product_data or 'category' in product_data 
        
        if has_owner_info and has_category_info:
            add_points(1, "Producto incluye información de relaciones")
            return True
        else:
            print("   [WARN] El producto no incluye información completa de relaciones")
    
    return False


def test_get_products_by_user():
    """Prueba obtener productos por usuario"""
    print_test_header("OBTENER PRODUCTOS POR USUARIO", 1.5)
    
    if not test_user_id:
        print("[ERROR] No hay usuario de prueba")
        return False
    
    response = safe_request(
        lambda: get_products_by_user(test_user_id),
        f"Obtener productos del usuario {test_user_id}"
    )
    
    if response:
        products = response.json()
        if isinstance(products, list) and len(products) > 0:
            add_points(1.5, f"Encontrados {len(products)} producto(s) del usuario")
            return True
        else:
            print("   [WARN] No se encontraron productos para el usuario")
    
    return False


def test_get_products_by_category():
    """Prueba obtener productos por categoría"""
    print_test_header("OBTENER PRODUCTOS POR CATEGORÍA", 1.5)
    
    if not test_category_id:
        print("[ERROR] No hay categoría de prueba")
        return False
    
    response = safe_request(
        lambda: get_products_by_category(test_category_id),
        f"Obtener productos de la categoría {test_category_id}"
    )
    
    if response:
        products = response.json()
        if isinstance(products, list) and len(products) > 0:
            add_points(1.5, f"Encontrados {len(products)} producto(s) de la categoría")
            return True
        else:
            print("   [WARN] No se encontraron productos para la categoría")
    
    return False


def test_update_product():
    """Prueba actualización de producto"""
    print_test_header("ACTUALIZAR PRODUCTO", 1.5)
    
    
    if not test_product_id or not test_category_id:
        print("[ERROR] No hay producto o categoría para actualizar")
        return False
    
    response = safe_request(
        lambda: update_product(
            test_product_id,
            "Laptop Test Relations - Updated",
            1399.99,
            "Laptop actualizada para pruebas",
            test_category_id
        ),
        f"Actualizar producto {test_product_id}"
    )
    
    if response:
        product_data = response.json()
        if product_data.get('name') == "Laptop Test Relations - Updated":
            add_points(1.5, "Producto actualizado correctamente")
            return True
    
    return False


def test_sequential_validation():
    """Prueba validación secuencial - verificar que el conteo aumentó"""
    print_test_header("VALIDACIÓN SECUENCIAL", 1)
    
    response = safe_request(get_all_products, "Contar productos finales")
    if response:
        final_count = len(response.json())
        print(f"   [INFO] Productos iniciales: {initial_product_count}")
        print(f"   [INFO] Productos finales: {final_count}")
        
        if final_count > initial_product_count:
            add_points(1, "El conteo de productos aumentó correctamente")
            return True
        else:
            print("   [WARN] El conteo no aumentó como se esperaba")
    
    return False


def test_delete_product():
    """Prueba eliminación de producto (opcional - no suma puntos)"""
    print_test_header("LIMPIAR DATOS DE PRUEBA", 0.5)
    
    if test_product_id:
        response = safe_request(
            lambda: delete_product(test_product_id),
            f"Eliminar producto de prueba {test_product_id}",
            [204,200]
        )
        if response:
            print("   [DELETE] Producto de prueba eliminado")
            add_points(0.5, "Producto de prueba eliminado")


# -----------------------------
# EJECUCIÓN PRINCIPAL
# -----------------------------
if __name__ == "__main__":
    print("=" * 60)
    print("[TESTS] PRUEBAS DE RELACIÓN ENTRE ENTIDADES (FASE 1 y 2)")
    print("[DOC] Basado en: 08_relacion_entidades.md")
    print("[GOAL] Objetivo: Validar endpoints de relaciones 1:N")
    print("=" * 60)
    
    last_commit()
    
    # Ejecutar todas las pruebas
    try:
        if test_setup():
            test_create_product()
            test_get_product_by_id()
            test_get_products_by_user()
            test_get_products_by_category()
            test_update_product()
            test_sequential_validation()
        
        # Limpiar datos de prueba
        test_delete_product()
        
    except KeyboardInterrupt:
        print("\n[WARN] Pruebas interrumpidas por el usuario")
    except Exception as e:
        print(f"\n[ERROR] Error inesperado: {e}")
    
    # Resultado final
    print("\n" + "=" * 60)
    print("RESULTADO FINAL")
    print("=" * 60)
    
    # Calcular calificación sobre 10
    final_grade = round((SCORE / MAX_SCORE) * 10, 1)
    
    print(f"Puntos obtenidos: {SCORE:.1f} / {MAX_SCORE}")
    print(f"Calificación final: {final_grade} / 10")
    
    # Interpretación de la calificación
    if final_grade >= 9:
        print("[EXCELLENT] ¡Excelente! Implementación completa")
    elif final_grade >= 7:
        print("[GOOD] Bien. La mayoría de endpoints funcionan")
    elif final_grade >= 5:
        print("[REGULAR] Regular. Algunos endpoints tienen problemas")
    else:
        print("[NEEDS_WORK] Necesita mejoras. Varios endpoints no funcionan")
    
    print("\n[ENDPOINTS] Endpoints evaluados:")
    print("   - POST /api/products (con relaciones)")
    print("   - GET /api/products/{id}")
    print("   - GET /api/products/user/{userId}")
    print("   - GET /api/products/category/{categoryId}")
    print("   - PUT /api/products/{id}")
    print("   - Validación secuencial de datos")
    
    print("\n[NOTE] Nota: Este script evalúa la implementación básica")
    print("    de relaciones 1:N. Para relaciones N:N, se requiere")
    print("    implementación adicional según la Fase 2 del documento.")