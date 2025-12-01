# Arquitectura del Sistema

## Arquitectura General

```
┌─────────────────────────────────────────────────────────┐
│                    CAPA PRESENTACIÓN                     │
│  (REST/MCP Endpoints)                                    │
│  ┌─────────────────────────────────────────────┐        │
│  │       McpServerController                    │        │
│  │  GET  /mcp/health                            │        │
│  │  GET  /mcp/tools                             │        │
│  │  POST /mcp/create_user                       │        │
│  │  POST /mcp/find_user_by_id                   │        │
│  │  ...                                         │        │
│  └─────────────────────────────────────────────┘        │
└──────────────────────┬──────────────────────────────────┘
                       ↓
┌─────────────────────────────────────────────────────────┐
│                    CAPA DE SERVICIO                      │
│  (Lógica de Negocio + ORM)                              │
│  ┌─────────────────────────────────────────────┐        │
│  │    HibernateUserServiceImpl                  │        │
│  │  @Service                                    │        │
│  │  @Transactional(readOnly=true)               │        │
│  │                                              │        │
│  │  + testEntityManager()                       │        │
│  │  + createUser() @Transactional               │        │
│  │  + findUserById()                            │        │
│  │  + updateUser() @Transactional               │        │
│  │  + deleteUser() @Transactional               │        │
│  │  + findAll()                                 │        │
│  │  + findUsersByDepartment()                   │        │
│  │  ...                                         │        │
│  └─────────────────────────────────────────────┘        │
└──────────────────────┬──────────────────────────────────┘
                       ↓
┌─────────────────────────────────────────────────────────┐
│                CAPA DE PERSISTENCIA                      │
│  (JPA/Hibernate)                                        │
│  ┌──────────────────┐       ┌────────────────────┐      │
│  │  EntityManager   │       │  UserRepository    │      │
│  │  (JPA)           │       │  (Spring Data JPA) │      │
│  │                  │       │                    │      │
│  │  persist()       │       │  findAll()         │      │
│  │  find()          │       │  findByDepartment()│      │
│  │  merge()         │       │  findByEmail()     │      │
│  │  remove()        │       │  ...               │      │
│  │  createQuery()   │       │                    │      │
│  └──────────────────┘       └────────────────────┘      │
└──────────────────────┬──────────────────────────────────┘
                       ↓
┌─────────────────────────────────────────────────────────┐
│                   CAPA DE MODELO                         │
│  (Entidades JPA)                                        │
│  ┌─────────────────────────────────────────────┐        │
│  │  @Entity User                                │        │
│  │  @Table(name = "users")                      │        │
│  │                                              │        │
│  │  @Id Long id                                 │        │
│  │  @Column String name                         │        │
│  │  @Column String email                        │        │
│  │  ...                                         │        │
│  └─────────────────────────────────────────────┘        │
└──────────────────────┬──────────────────────────────────┘
                       ↓
┌─────────────────────────────────────────────────────────┐
│                BASE DE DATOS (H2)                        │
│  jdbc:h2:mem:ra3db                                      │
│                                                         │
│  TABLE users (                                          │
│    id BIGINT PRIMARY KEY AUTO_INCREMENT,                │
│    name VARCHAR(50),                                    │
│    email VARCHAR(100) UNIQUE,                           │
│    ...                                                  │
│  )                                                      │
└─────────────────────────────────────────────────────────┘
```

## Patrones de Diseño

### 1. Repository Pattern

```
HibernateUserServiceImpl
        ↓
  UserRepository (interface)
        ↓
  JpaRepository<User, Long> (Spring Data)
        ↓
  Implementación automática por Spring
```

**Beneficios:**
- Abstracción del acceso a datos
- Código más testeable
- Métodos CRUD sin implementación manual

### 2. Service Layer Pattern

```
Controller → Service (interface) → ServiceImpl
```

**Responsabilidades:**
- **Controller**: Manejo HTTP, validación entrada
- **Service**: Lógica de negocio, transacciones
- **Repository**: Acceso a datos

### 3. DTO Pattern (Data Transfer Object)

```
HTTP Request (JSON)
     ↓
UserCreateDto (validaciones)
     ↓
Service (mapeo a Entity)
     ↓
User (entity)
     ↓
EntityManager.persist()
```

**DTOs usados:**
- `UserCreateDto`: Crear usuarios
- `UserUpdateDto`: Actualizar (campos opcionales)
- `UserQueryDto`: Búsquedas con filtros

**Beneficios:**
- Separación de API externa vs modelo interno
- Validaciones con Bean Validation
- Actualizaciones parciales

### 4. Dependency Injection

**Spring inyecta dependencias:**

```java
@Service
public class HibernateUserServiceImpl {
    @PersistenceContext
    private EntityManager entityManager;  // ← Inyectado por Spring

    @Autowired
    private UserRepository userRepository;  // ← Inyectado por Spring
}
```

**Tipos de inyección:**
- `@PersistenceContext`: Para EntityManager (JPA)
- `@Autowired`: Para beans de Spring (field injection)
- Constructor injection (recomendado en producción)

## Flujo de una Solicitud

### Ejemplo: `POST /mcp/create_user`

```
1. HTTP Request (JSON)
   ↓
2. McpServerController.createUser(@RequestBody UserCreateDto dto)
   ↓
3. Validación del DTO (Bean Validation)
   ↓
4. service.createUser(dto)
   ↓
5. HibernateUserServiceImpl.createUser()
   - Crear objeto User
   - entityManager.persist(user)
   ↓
6. Hibernate genera SQL INSERT
   ↓
7. Spring hace commit de transacción
   ↓
8. Hibernate ejecuta INSERT en BD
   ↓
9. Hibernate setea ID generado en el objeto
   ↓
10. Retornar User al controller
    ↓
11. Jackson serializa User a JSON
    ↓
12. HTTP Response 200 OK (JSON)
```

## Gestión de Transacciones

### Configuración de Transacciones

```java
@Service
@Transactional(readOnly = true)  // Default para toda la clase
public class HibernateUserServiceImpl {

    @Transactional  // Sobrescribe: readOnly=false
    public User createUser(UserCreateDto dto) {
        // Operación de escritura
    }

    public User findUserById(Long id) {
        // Usa transacción readOnly del nivel de clase
    }
}
```

### Propagación de Transacciones

```
Controller (sin @Transactional)
   ↓
Service.method1() @Transactional  ← INICIA transacción
   ↓
Service.method2() @Transactional  ← PARTICIPA en misma transacción
   ↓
COMMIT/ROLLBACK
```

### Spring Transaction Manager

```
Antes del método:
  1. BEGIN TRANSACTION

Durante el método:
  2. Ejecutar lógica
  3. Hibernate acumula cambios (dirty checking)

Al finalizar:
  - Si éxito: COMMIT (persiste cambios)
  - Si excepción: ROLLBACK (revierte cambios)
```

## Componentes Principales

### 1. User (Entidad)

**Archivo:** `src/main/java/com/dam/accesodatos/model/User.java`

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    // ... campos
}
```

**Responsabilidad:** Mapeo objeto-relacional

### 2. UserRepository

**Archivo:** `src/main/java/com/dam/accesodatos/repository/UserRepository.java`

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByDepartment(String department);
    User findByEmail(String email);
}
```

**Responsabilidad:** Acceso a datos con Spring Data JPA

### 3. HibernateUserService (Interface)

**Archivo:** `src/main/java/com/dam/accesodatos/ra3/HibernateUserService.java`

**Responsabilidad:** Contrato de métodos (interface)

### 4. HibernateUserServiceImpl

**Archivo:** `src/main/java/com/dam/accesodatos/ra3/HibernateUserServiceImpl.java`

**Responsabilidad:** Implementación de lógica de negocio + ORM

### 5. McpServerController

**Archivo:** `src/main/java/com/dam/accesodatos/mcp/McpServerController.java`

**Responsabilidad:** Exponer endpoints REST/MCP

### 6. McpToolRegistry

**Archivo:** `src/main/java/com/dam/accesodatos/mcp/McpToolRegistry.java`

**Responsabilidad:** Escanear y registrar herramientas @Tool

## Ciclo de Vida de Entidades JPA

```
NEW (Transient)
  ↓ persist()
MANAGED (en contexto de persistencia)
  ↓ commit()
PERSISTED (en BD)
  ↓ clear() / close()
DETACHED (fuera de contexto)
  ↓ merge()
MANAGED
  ↓ remove()
REMOVED
  ↓ commit()
DELETED (eliminado de BD)
```

**Estados:**
- **NEW**: Recién creado con `new User()`
- **MANAGED**: Hibernate rastreando cambios (persist, find, merge)
- **DETACHED**: Fuera del contexto (transacción cerrada)
- **REMOVED**: Marcado para eliminar (remove())

## Configuración de Spring Boot

### application.yml

```yaml
server:
  port: 8083

spring:
  datasource:
    url: jdbc:h2:mem:ra3db
    driver-class-name: org.h2.Driver

  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: none
    show-sql: true
```

### build.gradle

```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    runtimeOnly 'com.h2database:h2'
}
```

---

Esta arquitectura sigue los principios de:
- **Separación de responsabilidades** (capas)
- **Inversión de dependencias** (interfaces + DI)
- **Abstracción del acceso a datos** (Repository pattern)
- **Gestión declarativa de transacciones** (@Transactional)

Para más información, consulta [GUIA_ESTUDIANTE.md](GUIA_ESTUDIANTE.md).
