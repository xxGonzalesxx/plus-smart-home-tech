# 🏠 Smart Home Technologies — Платформа умного дома

**Многомодульный микросервисный проект на Spring Cloud** для сбора телеметрии, управления устройствами и интернет-магазином.

---

## 📋 О проекте

Smart Home Technologies — это платформа для управления устройствами умного дома и интернет-магазин для их продажи. Проект построен на микросервисной архитектуре с использованием Spring Cloud, Eureka, Kafka и gRPC.

---

## 🎯 Ключевые возможности

| Возможность | Описание |
|-------------|----------|
| 📡 **Сбор телеметрии** | Приём событий от датчиков и хабов через gRPC/REST |
| 🔄 **Агрегация данных** | Обработка и агрегация данных через Kafka |
| 📊 **Анализ и хранение** | Анализ событий, хранение в PostgreSQL |
| 🛒 **Интернет-магазин** | Витрина, корзина, склад с проверкой наличия |
| 🔍 **Service Discovery** | Все сервисы зарегистрированы в Eureka |
| ⚙️ **Внешняя конфигурация** | Централизованное управление через Config Server |
| 🐳 **Docker Ready** | Запуск в один клик через Docker Compose |

---

## 🧠 Технологический стек

### Бэкенд

| Технология | Версия | Назначение |
|------------|--------|------------|
| Java | 21 (Corretto) | Основной язык |
| Spring Boot | 3.3.2 | Основной фреймворк |
| Spring Cloud | 2023.0.3 | Микросервисная инфраструктура |
| Netflix Eureka | 4.1.3 | Service Discovery |
| OpenFeign | 4.0.4 | HTTP клиент (REST) |
| gRPC | 1.63.0 | Высокопроизводительный RPC |
| Protobuf | 3.23.4 | Сериализация данных |
| Apache Kafka | 3.6.1 | Брокер сообщений |
| PostgreSQL | 15 | База данных |
| Spring Data JPA | — | ORM |
| Hibernate | 6.5.2 | JPA реализация |
| Lombok | 1.18.36 | Упрощение кода |

### Инфраструктура

| Технология | Назначение |
|------------|------------|
| Docker / Docker Compose | Контейнеризация |
| Maven | Сборка проекта |
| Git | Контроль версий |
| GitHub Actions | CI/CD |

---

## 📁 Структура проекта
plus-smart-home-tech/
│
├── 📁 infra/
│ ├── 📁 config-server/ # Spring Cloud Config Server
│ └── 📁 discovery-server/ # Netflix Eureka Server
│
├── 📁 telemetry/
│ ├── 📁 collector/ # Приём событий (gRPC/REST)
│ ├── 📁 aggregator/ # Агрегация данных (Kafka)
│ ├── 📁 analyzer/ # Анализ и хранение (Kafka/JPA)
│ └── 📁 serialization/ # Avro/Protobuf схемы
│ ├── 📁 avro-schemas/
│ └── 📁 proto-schemas/
│
├── 📁 commerce/
│ ├── 📁 shopping-store/ # Витрина товаров
│ ├── 📁 shopping-cart/ # Корзина пользователя
│ └── 📁 warehouse/ # Управление складом
│
├── 📁 interaction-api/ # Общие DTO и Feign-клиенты
├── 📄 compose.yaml # Docker Compose
├── 📄 pom.xml # Корневой POM
└── 📄 README.md

text

---

## 🔧 API Эндпоинты

### shopping-store (Витрина)

| Метод | URL | Описание |
|-------|-----|----------|
| `GET` | `/api/v1/shopping-store?category={category}&page={page}&size={size}` | Получить товары по категории |
| `GET` | `/api/v1/shopping-store/{productId}` | Получить товар по ID |
| `PUT` | `/api/v1/shopping-store` | Создать товар |
| `POST` | `/api/v1/shopping-store` | Обновить товар |
| `POST` | `/api/v1/shopping-store/removeProductFromStore` | Деактивировать товар |
| `POST` | `/api/v1/shopping-store/quantityState` | Обновить статус количества |

### shopping-cart (Корзина)

| Метод | URL | Описание |
|-------|-----|----------|
| `GET` | `/api/v1/shopping-cart?username={username}` | Получить корзину |
| `PUT` | `/api/v1/shopping-cart?username={username}` | Добавить товары |
| `POST` | `/api/v1/shopping-cart/change-quantity?username={username}` | Изменить количество |
| `POST` | `/api/v1/shopping-cart/remove?username={username}` | Удалить товары |
| `DELETE` | `/api/v1/shopping-cart?username={username}` | Деактивировать корзину |

### warehouse (Склад)

| Метод | URL | Описание |
|-------|-----|----------|
| `PUT` | `/api/v1/warehouse` | Добавить товар на склад |
| `POST` | `/api/v1/warehouse/add` | Добавить количество |
| `POST` | `/api/v1/warehouse/check` | Проверить наличие |
| `GET` | `/api/v1/warehouse/address` | Получить адрес склада |

---

## 🚀 Быстрый старт

### Требования

| Компонент | Версия | Скачать |
|-----------|--------|---------|
| Java | 21+ | [Eclipse Temurin](https://adoptium.net/) |
| Docker | 24+ | [Docker Desktop](https://www.docker.com/) |
| Maven | 3.9+ | Встроен в IDEA |
| Git | 2.40+ | [Git](https://git-scm.com/) |

### Запуск

```bash
# 1. Запустить Docker контейнеры
docker compose up -d

# 2. Запустить инфраструктуру (в IDEA):
#    discovery-server → config-server

# 3. Запустить микросервисы (в IDEA):
#    collector → aggregator → analyzer → shopping-store → warehouse → shopping-cart

# 4. Проверить Eureka:
#    http://localhost:8761
🧪 Postman тесты
Импортируй коллекцию: docs/postman-collection.json

Пример запроса
http
PUT http://localhost:{PORT}/api/v1/shopping-store
Content-Type: application/json

{
    "productName": "Умная лампочка",
    "description": "LED лампа с управлением через Wi-Fi",
    "productCategory": "LIGHTING",
    "price": 999.99
}
📊 Схема БД
Сервис	Таблицы
shopping-store	products
shopping-cart	carts, cart_items
warehouse	warehouse_products
analyzer	scenarios, sensors, actions, conditions
📌 Версии
Компонент	Версия
Java	21 (Corretto 21.0.11)
Spring Boot	3.3.2
Spring Cloud	2023.0.3
Netflix Eureka	4.1.3
OpenFeign	4.0.4
gRPC	1.63.0
Protobuf	3.23.4
Apache Kafka	3.6.1
PostgreSQL	15
Lombok	1.18.36
Maven	3.9.x
Docker Compose	2.x
👤 Автор
@xxGonzalesxx

GitHub: github.com/xxGonzalesxx

Email: stef.kir1999@gmail.com
