# Guía de Herramientas MCP

Guía práctica para usar las herramientas MCP (Model Context Protocol) del proyecto.

## ¿Qué es MCP?

**Model Context Protocol (MCP)** es un protocolo que permite a los LLMs (Large Language Models) interactuar con aplicaciones mediante "herramientas" (tools).

En este proyecto, cada método del servicio Hibernate está expuesto como una herramienta MCP que puede ser invocada vía REST.

## Arquitectura MCP

```
LLM/Cliente HTTP
     ↓
REST Endpoint (/mcp/create_user)
     ↓
McpServerController
     ↓
HibernateUserService (método anotado con @Tool)
     ↓
Hibernate/JPA → Base de Datos
```

## Herramientas Disponibles

### 1. test_entity_manager

**Propósito:** Verificar que EntityManager de Hibernate está activo.

**Cuándo usarla:** Al iniciar, para validar configuración ORM.

**Endpoint:** `POST /mcp/test_entity_manager`

**Request:** Sin body

**Ejemplo curl:**
```bash
curl -X POST http://localhost:8083/mcp/test_entity_manager
```

**Response:**
```
"✓ EntityManager activo | Base de datos: RA3DB | Test: 1"
```

**SQL Generado:**
```sql
SELECT 1 as test, DATABASE() as db_name
```

---

### 2. create_user

**Propósito:** Crear un nuevo usuario en la base de datos.

**Cuándo usarla:** Para INSERT de nuevos registros.

**Endpoint:** `POST /mcp/create_user`

**Request:**
```json
{
  "name": "Ana López",
  "email": "ana.lopez@test.com",
  "department": "IT",
  "role": "Developer"
}
```

**Ejemplo curl:**
```bash
curl -X POST http://localhost:8083/mcp/create_user \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Ana López",
    "email": "ana.lopez@test.com",
    "department": "IT",
    "role": "Developer"
  }'
```

**Response:**
```json
{
  "id": 100,
  "name": "Ana López",
  "email": "ana.lopez@test.com",
  "department": "IT",
  "role": "Developer",
  "active": true,
  "createdAt": "2025-01-15T10:30:00",
  "updatedAt": "2025-01-15T10:30:00"
}
```

**SQL Generado:**
```sql
INSERT INTO users (name, email, department, role, active, created_at, updated_at)
VALUES ('Ana López', 'ana.lopez@test.com', 'IT', 'Developer', true, ...)
```

---

### 3. find_user_by_id

**Propósito:** Buscar un usuario por su ID.

**Cuándo usarla:** Para SELECT por clave primaria.

**Endpoint:** `POST /mcp/find_user_by_id`

**Request:**
```json
{
  "id": 1
}
```

**Ejemplo curl:**
```bash
curl -X POST http://localhost:8083/mcp/find_user_by_id \
  -H "Content-Type: application/json" \
  -d '{"id": 1}'
```

**Response (encontrado):**
```json
{
  "id": 1,
  "name": "Juan Pérez",
  "email": "juan.perez@empresa.com",
  ...
}
```

**Response (no encontrado):**
```json
null
```

---

### 4. update_user

**Propósito:** Actualizar campos de un usuario existente.

**Cuándo usarla:** Para UPDATE de registros.

**Endpoint:** `POST /mcp/update_user`

**Request:**
```json
{
  "id": 1,
  "name": "Juan Pérez Actualizado",
  "department": "Management"
}
```

**Ejemplo curl:**
```bash
curl -X POST http://localhost:8083/mcp/update_user \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "name": "Juan Pérez Actualizado",
    "department": "Management"
  }'
```

**Response:** Usuario completo actualizado

**Nota:** Solo incluye campos a modificar (UPDATE parcial).

---

### 5. delete_user (TODO)

**Propósito:** Eliminar un usuario por ID.

**Estado:** ⚠️ TODO - Debe ser implementado por el estudiante

**Endpoint:** `POST /mcp/delete_user`

**Request esperado:**
```json
{
  "id": 100
}
```

**Response esperado:**
```json
true  // o false si no existía
```

---

### 6. find_all_users

**Propósito:** Obtener todos los usuarios.

**Cuándo usarla:** Para SELECT * (listado completo).

**Endpoint:** `POST /mcp/find_all_users`

**Request:** Sin body

**Ejemplo curl:**
```bash
curl -X POST http://localhost:8083/mcp/find_all_users
```

**Response:**
```json
[
  {"id": 1, "name": "Juan Pérez", ...},
  {"id": 2, "name": "María García", ...},
  ...
]
```

---

### 7. find_users_by_department

**Propósito:** Buscar usuarios activos de un departamento.

**Cuándo usarla:** Para consultas con filtro JPQL.

**Endpoint:** `POST /mcp/find_users_by_department`

**Request:**
```json
{
  "department": "IT"
}
```

**Ejemplo curl:**
```bash
curl -X POST http://localhost:8083/mcp/find_users_by_department \
  -H "Content-Type: application/json" \
  -d '{"department": "IT"}'
```

**Response:** Array de usuarios del departamento IT

**JPQL Usado:**
```java
SELECT u FROM User u WHERE u.department = :dept AND u.active = true ORDER BY u.name
```

---

### 8. search_users (TODO)

**Propósito:** Búsqueda dinámica con múltiples filtros opcionales.

**Estado:** ⚠️ TODO - Debe ser implementado por el estudiante

**Endpoint:** `POST /mcp/search_users`

**Request esperado:**
```json
{
  "department": "IT",
  "role": "Developer",
  "active": true
}
```

**Response esperado:** Array de usuarios que cumplen todos los filtros

**JPQL esperado:**
```java
SELECT u FROM User u WHERE 1=1 AND u.department = :dept AND u.role = :role AND u.active = :active
```

---

### 9. transfer_data (TODO)

**Propósito:** Insertar múltiples usuarios en una transacción atómica.

**Estado:** ⚠️ TODO - Debe ser implementado por el estudiante

**Endpoint:** `POST /mcp/transfer_data`

**Request esperado:**
```json
{
  "users": [
    {"name": "User 1", "email": "user1@test.com", "department": "IT", "role": "Dev"},
    {"name": "User 2", "email": "user2@test.com", "department": "HR", "role": "Manager"}
  ]
}
```

**Response esperado:**
```json
true
```

**Comportamiento:** Si uno falla, todos hacen rollback (atomicidad).

---

### 10. execute_count_by_department (TODO)

**Propósito:** Contar usuarios activos de un departamento.

**Estado:** ⚠️ TODO - Debe ser implementado por el estudiante

**Endpoint:** `POST /mcp/execute_count_by_department`

**Request esperado:**
```json
{
  "department": "IT"
}
```

**Response esperado:**
```json
3
```

**JPQL esperado:**
```java
SELECT COUNT(u) FROM User u WHERE u.department = :dept AND u.active = true
```

---

## Cómo Funciona el Registro de Herramientas

### McpToolRegistry

**Archivo:** `src/main/java/com/dam/accesodatos/mcp/McpToolRegistry.java`

```java
@Component
public class McpToolRegistry {
    @PostConstruct
    public void scanTools() {
        // Escanea métodos anotados con @Tool
        // Registra en lista de herramientas MCP
    }

    public List<McpToolInfo> getRegisteredTools() {
        return registeredTools;
    }
}
```

### Anotación @Tool

**Archivo:** `src/main/java/org/springframework/ai/mcp/server/annotation/Tool.java`

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Tool {
    String name();
    String description();
}
```

**Uso:**
```java
@Tool(name = "create_user", description = "Persiste un nuevo usuario...")
public User createUser(UserCreateDto dto) {
    // ...
}
```

### McpServerController

**Archivo:** `src/main/java/com/dam/accesodatos/mcp/McpServerController.java`

Expone endpoints REST para cada herramienta:

```java
@RestController
@RequestMapping("/mcp")
public class McpServerController {
    @GetMapping("/tools")
    public Map<String, Object> getTools() {
        // Lista todas las herramientas registradas
    }

    @PostMapping("/create_user")
    public User createUser(@RequestBody UserCreateDto dto) {
        return service.createUser(dto);
    }

    // ... más endpoints
}
```

---

## Agregar una Nueva Herramienta MCP

1. **Definir método en HibernateUserService:**
```java
@Tool(name = "mi_nueva_tool", description = "Descripción...")
MyResult miNuevoMetodo(MyDto dto);
```

2. **Implementar en HibernateUserServiceImpl:**
```java
@Override
public MyResult miNuevoMetodo(MyDto dto) {
    // Implementación Hibernate/JPA
}
```

3. **Agregar endpoint en McpServerController:**
```java
@PostMapping("/mi_nueva_tool")
public MyResult miNuevaTool(@RequestBody MyDto dto) {
    return service.miNuevoMetodo(dto);
}
```

4. **Reiniciar aplicación** - La herramienta se registra automáticamente

---

## Testing de Herramientas MCP

### Desde Terminal (curl)

```bash
# Health check
curl http://localhost:8083/mcp/health

# Listar herramientas
curl http://localhost:8083/mcp/tools

# Probar herramienta
curl -X POST http://localhost:8083/mcp/create_user \
  -H "Content-Type: application/json" \
  -d '{"name": "Test", "email": "test@example.com", "department": "IT", "role": "Tester"}'
```

### Desde Postman

1. Importar colección (crear archivo `mcp-hibernate.postman_collection.json`)
2. Configurar base URL: `http://localhost:8083/mcp`
3. Crear requests para cada herramienta

### Desde Tests Java

Ver [TESTING_GUIA.md](TESTING_GUIA.md) para tests de integración.

---

Para más información técnica, consulta [API_REFERENCIA.md](API_REFERENCIA.md).
