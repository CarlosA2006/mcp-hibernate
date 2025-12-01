# API Referencia - mcp-hibernate

Referencia técnica completa de los endpoints MCP y métodos del servicio.

## Endpoints MCP (Base URL: http://localhost:8083/mcp)

### GET /health
Health check del servidor MCP.

**Response:**
```json
{"status":"UP","message":"MCP Server RA3 is running"}
```

### GET /tools
Lista todas las herramientas MCP disponibles.

**Response:**
```json
{
  "tools": [
    {"name": "test_entity_manager", "description": "Prueba el EntityManager..."},
    {"name": "create_user", "description": "Persiste un nuevo usuario..."},
    ...
  ],
  "count": 10
}
```

### POST /test_entity_manager
Verifica conexión EntityManager.

**Request:** No requiere body

**Response:**
```json
"✓ EntityManager activo | Base de datos: RA3DB | Test: 1"
```

### POST /create_user
Crea un nuevo usuario.

**Request:**
```json
{
  "name": "Ana López",
  "email": "ana.lopez@test.com",
  "department": "IT",
  "role": "Developer"
}
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

**Validaciones:**
- `name`: 2-50 caracteres, no nulo
- `email`: formato email válido, único
- `department`: no nulo
- `role`: no nulo

**Status Codes:**
- 200: Usuario creado exitosamente
- 400: Datos inválidos
- 500: Error de servidor (ej. email duplicado)

### POST /find_user_by_id
Busca usuario por ID.

**Request:**
```json
{
  "id": 1
}
```

**Response (encontrado):**
```json
{
  "id": 1,
  "name": "Juan Pérez",
  ...
}
```

**Response (no encontrado):**
```json
null
```

### POST /update_user
Actualiza usuario existente.

**Request:**
```json
{
  "id": 1,
  "name": "Juan Pérez Actualizado",
  "department": "Management"
}
```

**Response:** Usuario actualizado completo

**Notas:**
- Solo envía campos a modificar (actualización parcial)
- `id` es requerido
- Otros campos son opcionales

### POST /find_all_users
Obtiene todos los usuarios.

**Request:** No requiere body

**Response:**
```json
[
  {"id": 1, "name": "Juan Pérez", ...},
  {"id": 2, "name": "María García", ...},
  ...
]
```

### POST /find_users_by_department
Busca usuarios por departamento.

**Request:**
```json
{
  "department": "IT"
}
```

**Response:** Array de usuarios del departamento

## Métodos del Servicio

### HibernateUserService

Todos los métodos están documentados en `src/main/java/com/dam/accesodatos/ra3/HibernateUserService.java:1`

Ver [GUIA_ESTUDIANTE.md](GUIA_ESTUDIANTE.md) para detalles de implementación.

## Modelos de Datos

### User (Entidad)
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 50)
    private String name;
    
    @Column(unique = true, nullable = false, length = 100)
    private String email;
    
    @Column(nullable = false, length = 50)
    private String department;
    
    @Column(nullable = false, length = 50)
    private String role;
    
    @Column
    private Boolean active;
    
    @Column
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime updatedAt;
}
```

### UserCreateDto
```java
{
  "name": "string (2-50 chars)",
  "email": "string (email format)",
  "department": "string",
  "role": "string"
}
```

### UserUpdateDto
```java
{
  "name": "string (optional)",
  "email": "string (optional)",
  "department": "string (optional)",
  "role": "string (optional)",
  "active": "boolean (optional)"
}
```

### UserQueryDto
```java
{
  "department": "string (optional)",
  "role": "string (optional)",
  "active": "boolean (optional)",
  "limit": "integer (optional)",
  "offset": "integer (optional)"
}
```

## H2 Console

**URL:** http://localhost:8083/h2-console

**Conexión:**
- JDBC URL: `jdbc:h2:mem:ra3db`
- User: `sa`
- Password: *(vacío)*

**Queries de ejemplo:**
```sql
-- Todos los usuarios
SELECT * FROM users;

-- Por departamento
SELECT * FROM users WHERE department = 'IT';

-- Contar por departamento
SELECT department, COUNT(*) FROM users GROUP BY department;

-- Usuarios inactivos
SELECT * FROM users WHERE active = false;
```

## Error Handling

**Formato de Error:**
```json
{
  "timestamp": "2025-01-15T10:30:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "No se encontró usuario con ID 999",
  "path": "/mcp/update_user"
}
```

**Errores Comunes:**
- `EntityNotFoundException`: Usuario no encontrado
- `ConstraintViolationException`: Email duplicado o validación fallida
- `DataIntegrityViolationException`: Violación de constraints de BD

Para más información, consulta [GUIA_HERRAMIENTAS_MCP.md](GUIA_HERRAMIENTAS_MCP.md).
