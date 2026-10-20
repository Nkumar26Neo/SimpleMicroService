# SimpleMicroService API Documentation

This project provides an in-memory REST API service managing `SimpleDto` records with preloaded data, thread-safe sequence generation, and prefix-based searching.

---

## 📌 Base URL

```http
http://localhost:8080/api/v1/simples
```

---

## 📊 Endpoints Summary

| Method | Endpoint          | Description                           | Status Code                |
|:-------|:------------------|:--------------------------------------|:---------------------------|
| `GET`  | `/health`         | Service health check                  | `200 OK`                   |
| `POST` | `/auto`           | Create a generated record (`Neo_X`)   | `201 Created`              |
| `POST` | `/`               | Create a custom record                | `201 Created`              |
| `GET`  | `/{id}`           | Retrieve record by ID                 | `200 OK` / `404 Not Found` |
| `GET`  | `/?name={prefix}` | Filter by name prefix (or return all) | `200 OK`                   |

---

## 📦 Data Schema (`SimpleDto`)

```json
{
  "id": 1,
  "name": "Neo_1",
  "email": "Neo_1@gmail.com"
}
```

---

## 🚀 Endpoint Details & Testing Examples

### 1. Health Check
Checks internal health status of the service.

* **Method:** `GET`
* **Path:** `/api/v1/simples/health`

#### cURL Command
```bash
curl -X GET http://localhost:8080/api/v1/simples/health
```

#### Response (`200 OK`)
```json
{
  "status": "UP"
}
```

---

### 2. Auto-Generate Record
Creates an automatically populated record using the internal incremented ID sequence (`Neo_{id}`).

* **Method:** `POST`
* **Path:** `/api/v1/simples/auto`

#### cURL Command
```bash
curl -X POST http://localhost:8080/api/v1/simples/auto
```

#### Response (`201 Created`)
```json
{
  "id": 12,
  "name": "Neo_12",
  "email": "Neo_12@gmail.com"
}
```

---

### 3. Create Custom Record
Creates a custom record from the JSON payload. If `id` is omitted or `null`, a sequential ID is automatically assigned.

* **Method:** `POST`
* **Path:** `/api/v1/simples`
* **Headers:** `Content-Type: application/json`

#### cURL Command
```bash
curl -X POST http://localhost:8080/api/v1/simples \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Morpheus",
    "email": "morpheus@zion.org"
  }'
```

#### Response (`201 Created`)
```json
{
  "id": 13,
  "name": "Morpheus",
  "email": "morpheus@zion.org"
}
```

---

### 4. Get by ID
Retrieves a single record by its numeric identifier.

* **Method:** `GET`
* **Path:** `/api/v1/simples/{id}`

#### cURL Command
```bash
curl -X GET http://localhost:8080/api/v1/simples/1
```

#### Response (`200 OK`)
```json
{
  "id": 1,
  "name": "Neo_1",
  "email": "Neo_1@gmail.com"
}
```

#### Error Response (`404 Not Found`)
Returned when the requested ID does not exist in memory.

---

### 5. Search / List Records
Retrieves records matching a prefix filter on the `name` attribute. If the `name` parameter is omitted, empty, or whitespace, all records are returned.

* **Method:** `GET`
* **Path:** `/api/v1/simples` or `/api/v1/simples?name={prefix}`

#### Retrieve All Records
```bash
curl -X GET http://localhost:8080/api/v1/simples
```

#### Filter by Prefix (`Neo_1`)
```bash
curl -X GET "http://localhost:8080/api/v1/simples?name=Neo_1"
```

#### Response (`200 OK`)
```json
[
  {
    "id": 1,
    "name": "Neo_1",
    "email": "Neo_1@gmail.com"
  },
  {
    "id": 10,
    "name": "Neo_10",
    "email": "Neo_10@gmail.com"
  },
  {
    "id": 11,
    "name": "Neo_11",
    "email": "Neo_11@gmail.com"
  }
]
```

---

## 🧪 Postman & HTTP Client Quick-Test File

Save the following block as `test-requests.http` if using IntelliJ HTTP Client or VS Code REST Client extension:

```http
### Health Check
GET http://localhost:8080/api/v1/simples/health

### List All Records
GET http://localhost:8080/api/v1/simples

### Filter by Name Prefix
GET http://localhost:8080/api/v1/simples?name=Neo_1

### Get Single Record by ID
GET http://localhost:8080/api/v1/simples/1

### Auto Generate Record
POST http://localhost:8080/api/v1/simples/auto

### Create Custom Record
POST http://localhost:8080/api/v1/simples
Content-Type: application/json

{
  "name": "Trinity",
  "email": "trinity@matrix.io"
}
```