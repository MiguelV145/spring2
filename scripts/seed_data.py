"""Seed script to populate the API with users, categories, and ~1000 products.

Requirements:
- requests (pip install requests)
- The Spring Boot API must be running locally.

Adjust BASE_URL if needed.
"""
import random
import string
import time
from typing import List, Dict, Any

import requests

BASE_URL = "http://localhost:8080"
USERS_ENDPOINT = f"{BASE_URL}/api/users"
CATEGORIES_ENDPOINT = f"{BASE_URL}/api/categories"
PRODUCTS_ENDPOINT = f"{BASE_URL}/api/products"

# Config
TOTAL_PRODUCTS = 1000
MIN_PRICE = 10
MAX_PRICE = 5000

# Simple word banks for search-friendly names
ADJECTIVES = [
    "Ultra", "Pro", "Prime", "Smart", "Eco", "Max", "Lite", "Turbo", "Nano", "Quantum",
    "Vintage", "Retro", "Modern", "Classic", "Fusion", "Dynamic", "Epic", "Rapid", "Bold", "Urban"
]
NOUNS = [
    "Speaker", "Laptop", "Phone", "Headset", "Camera", "Monitor", "Keyboard", "Mouse", "Chair",
    "Desk", "Lamp", "Router", "Tablet", "Watch", "Printer", "Backpack", "Drone", "Mic", "SSD", "GPU"
]

CATEGORY_SEEDS = [
    ("Electrónicos", "Dispositivos y gadgets"),
    ("Hogar", "Productos para el hogar"),
    ("Oficina", "Equipos y accesorios de oficina"),
    ("Gaming", "Periféricos y consolas"),
    ("Audio", "Sonido y música"),
    ("Video", "Cámaras y streaming"),
    ("Movilidad", "Transporte personal"),
    ("Herramientas", "Equipamiento y bricolaje"),
    ("Decoración", "Estilo y ambientación"),
    ("Fitness", "Salud y ejercicio")
]

USER_SEEDS = [
    ("Alice Doe", "alice@example.com"),
    ("Bob Stone", "bob@example.com"),
    ("Carol Diaz", "carol@example.com"),
    ("David Kim", "david@example.com"),
    ("Eva Ruiz", "eva@example.com"),
]


def _rand_password() -> str:
    letters = string.ascii_letters + string.digits
    return "".join(random.choice(letters) for _ in range(12))


def create_users() -> List[int]:
    ids = []
    for name, email in USER_SEEDS:
        payload = {"name": name, "email": email, "password": _rand_password()}
        resp = requests.post(USERS_ENDPOINT, json=payload)
        resp.raise_for_status()
        ids.append(resp.json()["id"])
    print(f"Created {len(ids)} users: {ids}")
    return ids


def create_categories() -> Dict[str, int]:
    # Create
    for name, desc in CATEGORY_SEEDS:
        payload = {"name": name, "description": desc}
        resp = requests.post(CATEGORIES_ENDPOINT, json=payload)
        resp.raise_for_status()
    # Read back to capture IDs
    resp = requests.get(CATEGORIES_ENDPOINT)
    resp.raise_for_status()
    categories = resp.json()
    mapping = {c["name"]: c["id"] for c in categories}
    print(f"Loaded categories: {mapping}")
    return mapping


def _rand_product_name() -> str:
    return f"{random.choice(ADJECTIVES)} {random.choice(NOUNS)} {random.randint(1000, 9999)}"


def _rand_description() -> str:
    return "Producto de prueba para paginación y filtros."  # short and safe


def create_products(user_ids: List[int], category_ids: List[int]):
    total = TOTAL_PRODUCTS
    for idx in range(total):    
        name = _rand_product_name()
        price = round(random.uniform(MIN_PRICE, MAX_PRICE), 2)
        user_id = random.choice(user_ids)
        cats = random.sample(category_ids, k=2 if len(category_ids) >= 2 else 1)

        payload = {
            "name": name,
            "price": price,
            "description": _rand_description(),
            "userId": user_id,
            "categoryIds": cats,
        }

        resp = requests.post(PRODUCTS_ENDPOINT, json=payload)
        if resp.status_code >= 400:
            print(f"Error creating product {idx}: {resp.status_code} {resp.text}")
            break

        if (idx + 1) % 100 == 0:
            print(f"Created {idx + 1}/{total} products...")
            time.sleep(0.1)  # small pause to avoid overwhelming the API
    print("Done creating products")


def main():
    users = create_users()
    categories = create_categories()
    create_products(users, list(categories.values()))


if __name__ == "__main__":
    main()
