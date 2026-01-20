#!/usr/bin/env python3
import sys
import time
import random
import string
from typing import List, Dict, Optional, Tuple

try:
    import requests
except ImportError:
    print("ERROR: La librería 'requests' no está instalada.")
    print("Ejecute: python -m pip install requests")
    sys.exit(1)

# -----------------------------
# Configuración
# -----------------------------
BASE_URL = "http://localhost:8080"
API_PRODUCTS = f"{BASE_URL}/api/products"
API_USERS = f"{BASE_URL}/api/users"
API_CATEGORIES = f"{BASE_URL}/api/categories"

TOTAL_PRODUCTS = 1000
MIN_USERS = 5
CATEGORIES_POOL = 30                 # pool de categorías para escoger
MIN_CATEGORIES_PER_PRODUCT = 2       # requisito: 2 categorías por producto
PRICE_MIN = 10
PRICE_MAX = 5000
REQUEST_TIMEOUT = 20
MAX_RETRIES = 3

# Ya no forzamos un userId fijo; asignaremos aleatoriamente
# entre los usuarios disponibles para cumplir "al menos 5 usuarios".

# -----------------------------
# Utilidades
# -----------------------------
def rand_suffix(n=6) -> str:
    return "".join(random.choices(string.ascii_lowercase + string.digits, k=n))

def now_ns() -> int:
    return time.time_ns()

def safe_json(resp: requests.Response):
    try:
        return resp.json()
    except Exception:
        return None

def get_list(url: str, session: requests.Session) -> List[Dict]:
    r = session.get(url, timeout=REQUEST_TIMEOUT)
    if r.status_code != 200:
        return []
    data = safe_json(r)
    return data if isinstance(data, list) else []

def ensure_ok(resp: requests.Response, expected: List[int], context: str) -> bool:
    if resp.status_code in expected:
        return True
    print(f"[ERROR] {context} -> Status {resp.status_code}")
    if resp.text:
        print(" ", resp.text[:250])
    return False

def extract_category_ids(product_data: Dict) -> List[int]:
    """
    Intenta extraer IDs de categorías desde diferentes formatos de respuesta.
    """
    if not isinstance(product_data, dict):
        return []

    # 1) categoryIds: [1,2]
    if isinstance(product_data.get("categoryIds"), list):
        return [c for c in product_data["categoryIds"] if isinstance(c, int)]

    # 2) categories: [{id:1}, {id:2}]
    cats = product_data.get("categories")
    if isinstance(cats, list):
        ids = []
        for c in cats:
            if isinstance(c, dict) and isinstance(c.get("id"), int):
                ids.append(c["id"])
            elif isinstance(c, int):
                ids.append(c)
        return ids

    # 3) categoryId: 1 (1:N)
    if isinstance(product_data.get("categoryId"), int):
        return [product_data["categoryId"]]

    return []

# -----------------------------
# Generación “buscable”
# -----------------------------
BRANDS = ["Apex", "Nova", "Orion", "Vertex", "Zenith", "Hyper", "Titan", "Lumen", "Nimbus", "Sigma"]
PRODUCT_TYPES = [
    "Laptop", "Monitor", "Mouse", "Teclado", "CPU", "GPU", "SSD", "HDD", "Router", "Switch",
    "Auriculares", "Webcam", "Impresora", "Tablet", "Smartphone", "Fuente", "RAM", "Motherboard"
]
KEYWORDS = ["gaming", "pro", "ultra", "office", "student", "creator", "wireless", "rgb", "4k", "portable", "budget", "premium"]

def make_product_name(i: int, seed_tag: str) -> str:
    brand = random.choice(BRANDS)
    ptype = random.choice(PRODUCT_TYPES)
    kw1 = random.choice(KEYWORDS)
    kw2 = random.choice(KEYWORDS)
    while kw2 == kw1:
        kw2 = random.choice(KEYWORDS)
    model = f"{random.choice(string.ascii_uppercase)}{random.randint(10,999)}"
    # Incluye un tag para identificar lo creado por este seed
    return f"{seed_tag} {brand} {ptype} {kw1} {kw2} {model} #{i}"

def make_description(name: str) -> str:
    extras = [
        "Alta durabilidad", "Bajo consumo", "Excelente rendimiento", "Ideal para trabajo y estudio",
        "Edición limitada", "Garantía 12 meses", "Envío rápido", "Diseño moderno"
    ]
    return f"{name}. {random.choice(extras)}. SKU-{rand_suffix(8).upper()}"

def make_price() -> float:
    # Distribución mixta
    roll = random.random()
    if roll < 0.55:
        price = random.uniform(PRICE_MIN, 200)
    elif roll < 0.90:
        price = random.uniform(200, 1500)
    else:
        price = random.uniform(1500, PRICE_MAX)
    # Asegurar rango
    price = max(PRICE_MIN, min(PRICE_MAX, price))
    return round(price, 2)

# -----------------------------
# API helpers
# -----------------------------
def create_user(session: requests.Session, idx: int) -> Optional[Dict]:
    payload = {
        "name": f"Seed User {idx}",
        "email": f"seed.user.{idx}.{now_ns()}@test.com",
        "password": "SeedPassword123"
    }
    r = session.post(API_USERS, json=payload, timeout=REQUEST_TIMEOUT)
    if not ensure_ok(r, [200, 201], "Crear usuario"):
        return None
    return safe_json(r)

def create_category(session: requests.Session, idx: int) -> Optional[Dict]:
    payload = {
        "name": f"Seed Category {idx} {rand_suffix(4)}",
        # Backend espera "description" (ver CategoryCreateDto)
        "description": "Categoría seed para pruebas masivas"
    }
    r = session.post(API_CATEGORIES, json=payload, timeout=REQUEST_TIMEOUT)
    if not ensure_ok(r, [200, 201], "Crear categoría"):
        return None
    return safe_json(r)

def create_product_nn_in_create(
    session: requests.Session,
    user_id: int,
    category_ids: List[int],
    name: str,
    price: float,
    desc: str
) -> Optional[Dict]:
    """
    Crea producto exigiendo N:N desde el payload:
      { ..., "userId": x, "categoryIds": [a,b] }
    """
    payload = {
        "name": name,
        "price": price,
        "description": desc,
        "userId": user_id,
        "categoryIds": category_ids
    }
    r = session.post(API_PRODUCTS, json=payload, timeout=REQUEST_TIMEOUT)
    if r.status_code not in [200, 201]:
        return None
    return safe_json(r)

def get_product_by_id(session: requests.Session, pid: int) -> Optional[Dict]:
    r = session.get(f"{API_PRODUCTS}/{pid}", timeout=REQUEST_TIMEOUT)
    if r.status_code != 200:
        return None
    return safe_json(r)

# -----------------------------
# Main
# -----------------------------
def main():
    random.seed(time.time())
    session = requests.Session()
    seed_tag = f"SEED-{int(time.time())}"

    print("========================================")
    print("[SEED] Carga masiva garantizada")
    print(f"Base URL: {BASE_URL}")
    print(f"Tag: {seed_tag}")
    print("========================================\n")

    # 1) Asegurar usuarios (mínimo 5)
    users = get_list(API_USERS, session)
    if len(users) < MIN_USERS:
        print(f"[INFO] Usuarios actuales: {len(users)}. Creando hasta {MIN_USERS}...")
        for i in range(len(users) + 1, MIN_USERS + 1):
            u = create_user(session, i)
            if u:
                users.append(u)
    print(f"[OK] Usuarios listos: {len(users)}")

    user_ids = [u.get("id") for u in users if isinstance(u, dict) and isinstance(u.get("id"), int)]

    if len(user_ids) < MIN_USERS:
        print("[FATAL] No hay suficientes usuarios válidos (sin id). Revisa tu endpoint /api/users.")
        sys.exit(1)

    # 2) Asegurar categorías (pool)
    categories = get_list(API_CATEGORIES, session)
    if len(categories) < CATEGORIES_POOL:
        print(f"[INFO] Categorías actuales: {len(categories)}. Creando hasta {CATEGORIES_POOL}...")
        for i in range(len(categories) + 1, CATEGORIES_POOL + 1):
            c = create_category(session, i)
            # El POST /api/categories retorna un String, así que no podemos
            # confiar en el cuerpo de respuesta. Volveremos a consultar el listado.
            time.sleep(0.05)
        # Refrescar listado de categorías desde el backend
        categories = get_list(API_CATEGORIES, session)
    print(f"[OK] Categorías listas: {len(categories)}")

    category_ids = [c.get("id") for c in categories if isinstance(c, dict) and isinstance(c.get("id"), int)]
    if len(category_ids) < MIN_CATEGORIES_PER_PRODUCT:
        print("[FATAL] No hay suficientes categorías con id.")
        sys.exit(1)

    # 3) Verificar que el backend soporte crear con 2 categorías (N:N obligatorio)
    print("\n[CHECK] Verificando soporte N:N (POST /api/products con categoryIds=[a,b])...")
    probe_name = make_product_name(0, seed_tag)
    probe_desc = make_description(probe_name)
    probe_price = make_price()
    probe_cats = random.sample(category_ids, k=MIN_CATEGORIES_PER_PRODUCT)

    # Usamos cualquier usuario válido para la prueba
    probe_user_id = random.choice(user_ids)
    probe = create_product_nn_in_create(session, probe_user_id, probe_cats, probe_name, probe_price, probe_desc)
    if not probe or not isinstance(probe.get("id"), int):
        print("[FATAL] Tu API NO acepta 'categoryIds' al crear producto.")
        print("No se puede cumplir: 'al menos 2 categorías por producto' con tu backend actual.")
        print("Solución: implementar N:N y permitir:")
        print("  POST /api/products  { ..., categoryIds: [1,2] }")
        sys.exit(1)

    probe_id = probe["id"]

    # Confirmar que realmente guardó 2 categorías
    probe_fresh = get_product_by_id(session, probe_id) or probe
    probe_cat_ids = extract_category_ids(probe_fresh)
    if len(set(probe_cat_ids)) < 2:
        print("[FATAL] El producto se creó, pero NO devuelve/guarda 2 categorías.")
        print("No se puede garantizar el requisito de 2 categorías por producto.")
        sys.exit(1)

    print("[OK] N:N soportado. Continuando carga masiva...")

    # 4) Crear productos (garantizado: 1000 OK, si falla -> retry; si al final no llega -> FATAL)
    created_ids: List[int] = [probe_id]
    failures = 0

    target = TOTAL_PRODUCTS
    start_i = 1

    print(f"\n[RUN] Creando {target} productos (userId aleatorio entre {len(user_ids)} usuarios)...\n")

    for i in range(start_i, target):
        cats = random.sample(category_ids, k=MIN_CATEGORIES_PER_PRODUCT)
        name = make_product_name(i, seed_tag)
        desc = make_description(name)
        price = make_price()

        success = False
        for attempt in range(1, MAX_RETRIES + 1):
            # Asignar usuario aleatorio para distribuir
            uid = random.choice(user_ids)
            pdata = create_product_nn_in_create(session, uid, cats, name, price, desc)
            if pdata and isinstance(pdata.get("id"), int):
                pid = pdata["id"]

                # Verificar requisitos fuertes (precio y 2 categorías)
                fresh = get_product_by_id(session, pid) or pdata
                cat_ids = extract_category_ids(fresh)

                if not (PRICE_MIN <= float(price) <= PRICE_MAX):
                    print(f"[ERROR] Precio fuera de rango en i={i}, pid={pid}")
                    # si esto pasa, es bug de script (no debería)
                    continue

                if len(set(cat_ids)) < 2:
                    # si tu backend aceptó pero guardó 1, no cumple -> retry
                    print(f"[WARN] i={i} pid={pid} no tiene 2 categorías (tiene {len(set(cat_ids))}). Retry {attempt}/{MAX_RETRIES}")
                    continue

                created_ids.append(pid)
                success = True
                break

            time.sleep(0.15)  # micro pausa para no saturar

        if not success:
            failures += 1

        if (i + 1) % 50 == 0:
            print(f"[PROGRESS] {(i + 1)}/{target} | ok={len(created_ids)} | failures={failures}")

    print("\n========================================")
    print("[DONE] Resumen")
    print("========================================")
    print(f"Productos objetivo: {TOTAL_PRODUCTS}")
    print(f"Productos creados OK: {len(created_ids)}")
    print(f"Fallos: {failures}")

    # 5) Garantía final: si no llegamos a 1000, falla el script
    if len(created_ids) < TOTAL_PRODUCTS:
        print("\n[FATAL] No se pudo crear el dataset completo (1000 productos) cumpliendo 2 categorías por producto.")
        print("Revisa logs/errores del backend o aumenta CATEGORIES_POOL y verifica tu endpoint.")
        sys.exit(1)

    print("\n[OK] Dataset creado y cumple los requisitos:")
    print(f" - >= {MIN_USERS} usuarios (DB)")
    print(f" - {TOTAL_PRODUCTS} productos creados")
    print(" - 2 categorías por producto (N:N)")
    print(f" - precios {PRICE_MIN}..{PRICE_MAX}")
    print(" - nombres con texto buscable (keywords)")
    print("\nListo.")

if __name__ == "__main__":
    main()
