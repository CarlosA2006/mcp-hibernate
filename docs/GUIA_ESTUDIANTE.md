# Guía del Estudiante - mcp-hibernate

¡Bienvenido al proyecto **mcp-hibernate**! Esta guía te acompañará paso a paso en tu aprendizaje de Hibernate/JPA (RA3) para el módulo de Acceso a Datos.

## Tabla de Contenidos

1. [Bienvenida y Objetivos](#1-bienvenida-y-objetivos)
2. [Conceptos Fundamentales](#2-conceptos-fundamentales)
3. [Navegando el Código](#3-navegando-el-código)
4. [Los 6 Métodos Implementados (ESTUDIAR)](#4-los-6-métodos-implementados-estudiar)
5. [Los 4 Métodos TODO (IMPLEMENTAR)](#5-los-4-métodos-todo-implementar)
6. [Ejecutar y Entender los Tests](#6-ejecutar-y-entender-los-tests)
7. [Flujo de Trabajo Recomendado](#7-flujo-de-trabajo-recomendado)
8. [Recursos Adicionales](#8-recursos-adicionales)

---

## 1. Bienvenida y Objetivos

### ¿Qué vas a aprender?

Este proyecto te enseñará a:

✅ **Entender ORM** (Object-Relational Mapping) y por qué simplifica el acceso a datos
✅ **Usar Hibernate/JPA** para operaciones CRUD sin escribir SQL
✅ **Escribir consultas JPQL** (Java Persistence Query Language)
✅ **Gestionar transacciones** con `@Transactional`
✅ **Mapear entidades** a tablas con anotaciones JPA
✅ **Comprender la diferencia** entre JDBC (RA2) y Hibernate (RA3)

### Resultados de Aprendizaje (RA3)

Este proyecto cubre completamente el **RA3**:

> **RA3:** Gestiona la persistencia de los datos identificando herramientas de mapeo objeto relacional (ORM) y desarrollando aplicaciones que las utilizan.

Consulta [CRITERIOS_RA3_DETALLADO.md](CRITERIOS_RA3_DETALLADO.md) para ver cómo cada método del código cubre un criterio específico.

---

## 2. Conceptos Fundamentales

### ¿Qué es un ORM?

**Antes (RA2 - JDBC):** Escribías SQL manualmente y mapeabas `ResultSet` a objetos:

```java
// RA2: JDBC manual
String sql = "SELECT * FROM users WHERE id = ?";
PreparedStatement ps = conn.prepareStatement(sql);
ps.setLong(1, id);
ResultSet rs = ps.executeQuery();
if (rs.next()) {
    User user = new User();
    user.setId(rs.getLong("id"));
    user.setName(rs.getString("name"));
    // ... 8 campos más ...
}
```

**Ahora (RA3 - Hibernate):** Hibernate hace todo esto automáticamente:

```java
// RA3: Hibernate automático
User user = entityManager.find(User.class, id);
// ¡Una línea! Hibernate genera el SQL y mapea automáticamente
```

### Diferencias Clave RA2 vs RA3

| Concepto | RA2 (JDBC) | RA3 (Hibernate/JPA) |
|----------|------------|---------------------|
| **Lenguaje** | SQL (tablas y columnas) | JPQL (entidades y atributos) |
| **Mapeo** | Manual con ResultSet | Automático con @Entity |
| **INSERT** | `PreparedStatement` + `setString()` | `persist(objeto)` |
| **SELECT** | `executeQuery()` + bucle `while(rs.next())` | `find(id)` o JPQL |
| **UPDATE** | `UPDATE users SET ...` | `merge(objeto)` + dirty checking |
| **DELETE** | `DELETE FROM users ...` | `remove(objeto)` |
| **Transacciones** | `commit()`/`rollback()` manual | `@Transactional` automático |

**Para profundizar:**
Lee [Explicacion_Clase_Hibernate.md](../Explicacion_Clase_Hibernate.md) para una explicación didáctica completa de ORM y Hibernate.

---

## 3. Navegando el Código

### Estructura de Paquetes

```
src/main/java/com/dam/accesodatos/
├── McpAccesoDatosRa3Application.java  → Clase principal
├── model/
│   ├── User.java                      → 🎯 Entidad JPA (mapeo @Entity)
│   ├── UserCreateDto.java             → DTO para crear usuarios
│   ├── UserUpdateDto.java             → DTO para actualizar
│   └── UserQueryDto.java              → DTO para búsquedas
├── ra3/
│   ├── HibernateUserService.java      → 📋 Interface (contratos de métodos)
│   └── HibernateUserServiceImpl.java  → 🎯 IMPLEMENTACIÓN (tu código está aquí)
├── repository/
│   └── UserRepository.java            → 🎯 Spring Data JPA Repository
└── mcp/
    ├── McpServerController.java       → REST endpoints
    └── McpToolRegistry.java           → Registro de herramientas MCP
```

### Archivos Clave que Debes Estudiar

#### 1. `User.java` - La Entidad

Este archivo define el mapeo entre la clase Java y la tabla de base de datos:

```java
@Entity                          // ← Marca como entidad JPA
@Table(name = "users")          // ← Mapea a tabla "users"
public class User {
    @Id                          // ← Clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // ← Autoincremental
    private Long id;

    @Column(nullable = false, length = 50)  // ← Mapeo de columna
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    // ... más campos
}
```

**Archivo:** `src/main/java/com/dam/accesodatos/model/User.java`

#### 2. `HibernateUserServiceImpl.java` - La Implementación

Este es el archivo MÁS IMPORTANTE. Aquí están los 6 ejemplos que debes estudiar y los 4 TODOs que debes implementar.

**Archivo:** `src/main/java/com/dam/accesodatos/ra3/HibernateUserServiceImpl.java`

#### 3. `UserRepository.java` - Spring Data JPA

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Spring Data JPA genera implementación automáticamente
    List<User> findByDepartment(String department);  // ← Query derivado
    User findByEmail(String email);
}
```

**Archivo:** `src/main/java/com/dam/accesodatos/repository/UserRepository.java`

---

## 4. Los 6 Métodos Implementados (ESTUDIAR)

Estos métodos están completamente implementados como ejemplos. **Tu tarea es estudiarlos y entenderlos** antes de implementar los TODOs.

### 4.1. Método 1: `testEntityManager()` - Verificar Conexión

**Qué hace:** Verifica que EntityManager esté funcionando.

**Código:**

```java
@Override
public String testEntityManager() {
    if (!entityManager.isOpen()) {
        throw new RuntimeException("EntityManager está cerrado");
    }

    Query query = entityManager.createNativeQuery("SELECT 1 as test, DATABASE() as db_name");
    Object[] result = (Object[]) query.getSingleResult();

    return String.format("✓ EntityManager activo | Base de datos: %s", result[1]);
}
```

**SQL Generado:**

```sql
SELECT 1 as test, DATABASE() as db_name
```

**Conceptos Clave:**

- `EntityManager`: Interfaz principal de JPA (equivalente a `Connection` en JDBC)
- `isOpen()`: Verifica si está activo (vs `Connection.isClosed()` en JDBC)
- `createNativeQuery()`: Ejecuta SQL nativo (no JPQL)

**Test Relacionado:**
`HibernateUserServiceIntegrationTest.testEntityManager_RealConnection_Success()`

**Comparación RA2 vs RA3:**

| RA2 (JDBC) | RA3 (Hibernate) |
|------------|-----------------|
| `Connection.isClosed()` | `EntityManager.isOpen()` |
| `Statement.executeQuery()` | `createNativeQuery().getSingleResult()` |

---

### 4.2. Método 2: `createUser()` - INSERT con persist()

**Qué hace:** Crea un nuevo usuario en la base de datos.

**Código:**

```java
@Override
@Transactional  // ← OBLIGATORIO para modificar BD
public User createUser(UserCreateDto dto) {
    // 1. Crear entidad desde DTO
    User user = new User();
    user.setName(dto.getName());
    user.setEmail(dto.getEmail());
    user.setDepartment(dto.getDepartment());
    user.setRole(dto.getRole());
    user.setActive(true);
    user.setCreatedAt(LocalDateTime.now());
    user.setUpdatedAt(LocalDateTime.now());

    // 2. Persistir (Hibernate genera INSERT automáticamente)
    entityManager.persist(user);

    // 3. Retornar usuario (ID ya está seteado por Hibernate)
    return user;
}
```

**SQL Generado (automático):**

```sql
INSERT INTO users (name, email, department, role, active, created_at, updated_at)
VALUES (?, ?, ?, ?, ?, ?, ?)
```

**Conceptos Clave:**

- `@Transactional`: Spring maneja commit/rollback automáticamente
- `persist(user)`: Hibernate guarda el objeto en el contexto de persistencia
- **ID autogenerado**: Hibernate setea el ID automáticamente después del INSERT

**Test Relacionado:**
`HibernateUserServiceIntegrationTest.createUser_ValidDto_Success()`

**Comparación RA2 vs RA3:**

```java
// RA2: JDBC manual
String sql = "INSERT INTO users (name, email, ...) VALUES (?, ?, ...)";
PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
ps.setString(1, dto.getName());
ps.setString(2, dto.getEmail());
// ... 5 setString más ...
ps.executeUpdate();
ResultSet keys = ps.getGeneratedKeys();
if (keys.next()) {
    user.setId(keys.getLong(1));
}

// RA3: Hibernate automático
entityManager.persist(user);  // ¡Una línea!
```

**Observa el SQL en logs:**
Ejecuta la aplicación con `./gradlew bootRun` y observa la consola:

```
Hibernate: INSERT INTO users (...) VALUES (?, ?, ?, ?, ?, ?, ?)
binding parameter [1] as [VARCHAR] - [Ana López]
binding parameter [2] as [VARCHAR] - [ana.lopez@test.com]
...
```

---

### 4.3. Método 3: `findUserById()` - SELECT por ID

**Qué hace:** Busca un usuario por su ID.

**Código:**

```java
@Override
public User findUserById(Long id) {
    // find() es la forma más simple de buscar por clave primaria
    return entityManager.find(User.class, id);
}
```

**SQL Generado:**

```sql
SELECT id, name, email, department, role, active, created_at, updated_at
FROM users
WHERE id = ?
```

**Conceptos Clave:**

- `find(User.class, id)`: Busca por clave primaria
- **Retorna `null`** si no existe (no lanza excepción)
- Mapeo automático de columnas a atributos

**Test Relacionado:**
`HibernateUserServiceIntegrationTest.findUserById_ExistingId_Success()`

**Comparación RA2 vs RA3:**

```java
// RA2: JDBC - 15 líneas
String sql = "SELECT * FROM users WHERE id = ?";
PreparedStatement ps = conn.prepareStatement(sql);
ps.setLong(1, id);
ResultSet rs = ps.executeQuery();
if (rs.next()) {
    User user = new User();
    user.setId(rs.getLong("id"));
    user.setName(rs.getString("name"));
    user.setEmail(rs.getString("email"));
    user.setDepartment(rs.getString("department"));
    user.setRole(rs.getString("role"));
    user.setActive(rs.getBoolean("active"));
    user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
    user.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
    return user;
}
return null;

// RA3: Hibernate - 1 línea
return entityManager.find(User.class, id);
```

---

### 4.4. Método 4: `updateUser()` - UPDATE con merge()

**Qué hace:** Actualiza un usuario existente.

**Código:**

```java
@Override
@Transactional
public User updateUser(Long id, UserUpdateDto dto) {
    // 1. Buscar entidad existente
    User existing = findUserById(id);
    if (existing == null) {
        throw new RuntimeException("No se encontró usuario con ID " + id);
    }

    // 2. Aplicar cambios del DTO (solo campos no nulos)
    if (dto.getName() != null) {
        existing.setName(dto.getName());
    }
    if (dto.getEmail() != null) {
        existing.setEmail(dto.getEmail());
    }
    // ... más campos
    existing.setUpdatedAt(LocalDateTime.now());

    // 3. merge() sincroniza cambios con BD
    return entityManager.merge(existing);
    // Spring hace commit al finalizar → Hibernate ejecuta UPDATE
}
```

**SQL Generado (solo campos modificados):**

```sql
UPDATE users
SET name = ?, email = ?, updated_at = ?
WHERE id = ?
```

**Conceptos Clave:**

- **Dirty Checking**: Hibernate detecta qué campos cambiaron
- `merge(user)`: Sincroniza cambios del objeto con la BD
- **UPDATE parcial**: Solo actualiza campos modificados

**Test Relacionado:**
`HibernateUserServiceIntegrationTest.updateUser_ValidChanges_Success()`

**Comparación RA2 vs RA3:**

```java
// RA2: Construir UPDATE manualmente
StringBuilder sql = new StringBuilder("UPDATE users SET ");
if (dto.getName() != null) sql.append("name = ?, ");
if (dto.getEmail() != null) sql.append("email = ?, ");
// ... quitar última coma, añadir WHERE...

// RA3: Hibernate detecta cambios automáticamente
entityManager.merge(existing);
```

---

### 4.5. Método 5: `findAll()` - SELECT todos con Repository

**Qué hace:** Obtiene todos los usuarios de la base de datos.

**Código:**

```java
@Override
public List<User> findAll() {
    // Spring Data JPA genera la query automáticamente
    return userRepository.findAll();
}
```

**SQL Generado:**

```sql
SELECT u FROM User u
```

(Hibernate traduce esto a SQL estándar)

**Conceptos Clave:**

- **Spring Data JPA Repository**: Métodos CRUD sin implementación
- `findAll()`: Heredado de `JpaRepository<User, Long>`
- Retorna `List<User>` con todas las filas

**Test Relacionado:**
`HibernateUserServiceIntegrationTest.findAll_ReturnsAllUsers()`

**Repositorio:**

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // findAll() ya está implementado por JpaRepository
    // ¡No necesitas escribir código!
}
```

---

### 4.6. Método 6: `findUsersByDepartment()` - JPQL Básico

**Qué hace:** Busca usuarios activos de un departamento específico.

**Código:**

```java
@Override
public List<User> findUsersByDepartment(String department) {
    // JPQL: Query orientado a objetos (entidades, no tablas)
    String jpql = "SELECT u FROM User u WHERE u.department = :dept AND u.active = true ORDER BY u.name";

    TypedQuery<User> query = entityManager.createQuery(jpql, User.class);
    query.setParameter("dept", department);

    return query.getResultList();
}
```

**JPQL vs SQL:**

| JPQL | SQL |
|------|-----|
| `SELECT u FROM User u` | `SELECT * FROM users u` |
| `u.department` (atributo) | `u.department` (columna) |
| `:dept` (parámetro nombrado) | `?` (parámetro posicional) |

**SQL Generado:**

```sql
SELECT id, name, email, department, role, active, created_at, updated_at
FROM users
WHERE department = ? AND active = true
ORDER BY name
```

**Conceptos Clave:**

- **JPQL**: Java Persistence Query Language (orientado a objetos)
- `User` (entidad) vs `users` (tabla)
- `TypedQuery<User>`: Query con type-safety
- `:dept`: Parámetro nombrado (evita SQL injection)

**Test Relacionado:**
`HibernateUserServiceIntegrationTest.findUsersByDepartment_WithRealData_Success()`

**Comparación RA2 vs RA3:**

```java
// RA2: SQL directo con tablas
String sql = "SELECT * FROM users WHERE department = ? AND active = true";

// RA3: JPQL con entidades
String jpql = "SELECT u FROM User u WHERE u.department = :dept AND u.active = true";
```

---

## 5. Los 4 Métodos TODO (IMPLEMENTAR)

Ahora es tu turno. Estos métodos están marcados como `TODO` y debes implementarlos aplicando lo aprendido de los ejemplos.

### 5.1. TODO 1: `deleteUser()` - Eliminar con remove()

**Objetivo:** Eliminar un usuario por su ID.

**Criterio RA3 que cubre:** CE3.e (Modificación y recuperación de objetos)

**Pistas de Implementación:**

1. Buscar el usuario con `findUserById(id)`
2. Si es `null`, retornar `false`
3. Eliminar con `entityManager.remove(user)`
4. Retornar `true`

**IMPORTANTE:** `remove()` requiere que la entidad esté "managed" (en el contexto de persistencia), por eso primero la buscas con `find()`.

**Método TODO en `HibernateUserServiceImpl.java:192`:**

```java
@Override
@Transactional
public boolean deleteUser(Long id) {
    // TODO CE3.e: Implementar deleteUser()
    throw new UnsupportedOperationException("TODO...");
}
```

**Estructura Esperada:**

```java
@Override
@Transactional
public boolean deleteUser(Long id) {
    // 1. Buscar usuario
    // 2. Verificar si existe
    // 3. Eliminar con remove()
    // 4. Retornar resultado
}
```

**SQL Esperado:**

```sql
DELETE FROM users WHERE id = ?
```

**Test para Validar:**
Crea un test en `HibernateUserServiceIntegrationTest`:

```java
@Test
void deleteUser_ExistingId_Success() {
    User created = service.createUser(createDto);
    boolean deleted = service.deleteUser(created.getId());
    assertTrue(deleted);
    assertNull(service.findUserById(created.getId()));
}
```

**Enlace a Ejemplo:**
Consulta [EJEMPLOS/04_ELIMINAR_USUARIO.md](EJEMPLOS/04_ELIMINAR_USUARIO.md) para un ejemplo completo.

---

### 5.2. TODO 2: `searchUsers()` - JPQL Dinámico

**Objetivo:** Buscar usuarios con filtros opcionales (department, role, active).

**Criterio RA3 que cubre:** CE3.f (Consultas JPQL)

**Pistas de Implementación:**

1. Crear `StringBuilder` con JPQL base: `"SELECT u FROM User u WHERE 1=1"`
2. Añadir condiciones según filtros presentes:
   - Si `queryDto.getDepartment() != null`: `append(" AND u.department = :dept")`
   - Si `queryDto.getRole() != null`: `append(" AND u.role = :role")`
   - Si `queryDto.getActive() != null`: `append(" AND u.active = :active")`
3. Crear `TypedQuery<User>` con el JPQL construido
4. Setear parámetros **solo para filtros presentes**
5. Ejecutar `getResultList()`

**Método TODO en `HibernateUserServiceImpl.java:267`:**

```java
@Override
public List<User> searchUsers(UserQueryDto queryDto) {
    // TODO CE3.f: Implementar searchUsers() con JPQL dinámico
    throw new UnsupportedOperationException("TODO...");
}
```

**Estructura Esperada:**

```java
@Override
public List<User> searchUsers(UserQueryDto queryDto) {
    StringBuilder jpql = new StringBuilder("SELECT u FROM User u WHERE 1=1");

    // Añadir condiciones dinámicamente
    if (queryDto.getDepartment() != null) {
        jpql.append(" AND u.department = :dept");
    }
    // ... más condiciones

    TypedQuery<User> query = entityManager.createQuery(jpql.toString(), User.class);

    // Setear parámetros solo si el filtro está presente
    if (queryDto.getDepartment() != null) {
        query.setParameter("dept", queryDto.getDepartment());
    }
    // ... más parámetros

    return query.getResultList();
}
```

**JPQL Generado (ejemplo con 2 filtros):**

```java
SELECT u FROM User u WHERE 1=1 AND u.department = :dept AND u.active = :active
```

**Test para Validar:**

```java
@Test
void searchUsers_WithFilters_ReturnsMatching() {
    UserQueryDto queryDto = new UserQueryDto();
    queryDto.setDepartment("IT");
    queryDto.setActive(true);

    List<User> results = service.searchUsers(queryDto);

    assertTrue(results.size() > 0);
    results.forEach(u -> {
        assertEquals("IT", u.getDepartment());
        assertTrue(u.getActive());
    });
}
```

**Enlace a Ejemplo:**
Consulta [EJEMPLOS/05_BUSCAR_CON_FILTROS.md](EJEMPLOS/05_BUSCAR_CON_FILTROS.md) para el patrón completo.

---

### 5.3. TODO 3: `transferData()` - Transacción Múltiple

**Objetivo:** Insertar múltiples usuarios en una sola transacción.

**Criterio RA3 que cubre:** CE3.g (Gestión de transacciones)

**Pistas de Implementación:**

1. Anotar método con `@Transactional`
2. Iterar sobre la lista de usuarios: `for (User user : users) { ... }`
3. Para cada usuario: `entityManager.persist(user)`
4. Retornar `true`
5. Si hay error, Spring hace **rollback automáticamente**

**Método TODO en `HibernateUserServiceImpl.java:308`:**

```java
@Override
@Transactional
public boolean transferData(List<User> users) {
    // TODO CE3.g: Implementar transferData()
    throw new UnsupportedOperationException("TODO...");
}
```

**Estructura Esperada:**

```java
@Override
@Transactional
public boolean transferData(List<User> users) {
    for (User user : users) {
        entityManager.persist(user);
    }
    return true;
    // Spring hace commit automáticamente si todo OK
    // Spring hace rollback automáticamente si hay excepción
}
```

**Comportamiento Transaccional:**

- **Si todos los persist() tienen éxito**: Spring hace `commit()` al finalizar
- **Si uno falla**: Spring hace `rollback()` de TODOS (atomicidad)

**Test para Validar:**

```java
@Test
void transferData_MultipleUsers_AllInsertedOrNone() {
    List<User> users = Arrays.asList(
        new User("User1", "user1@test.com", "IT", "Dev", true),
        new User("User2", "user2@test.com", "HR", "Manager", true)
    );

    boolean success = service.transferData(users);

    assertTrue(success);
    assertEquals(10, service.findAll().size());  // 8 iniciales + 2 nuevos
}
```

**Comparación RA2 vs RA3:**

```java
// RA2: Transacción manual
conn.setAutoCommit(false);
try {
    for (User user : users) {
        ps.setString(1, user.getName());
        // ...
        ps.executeUpdate();
    }
    conn.commit();  // ← Manual
} catch (Exception e) {
    conn.rollback();  // ← Manual
}

// RA3: Transacción automática con @Transactional
@Transactional
public boolean transferData(List<User> users) {
    for (User user : users) {
        entityManager.persist(user);
    }
    return true;
    // Spring maneja commit/rollback automáticamente
}
```

**Enlace a Ejemplo:**
Consulta [EJEMPLOS/06_TRANSACCIONES.md](EJEMPLOS/06_TRANSACCIONES.md) para detalles de transacciones.

---

### 5.4. TODO 4: `executeCountByDepartment()` - COUNT en JPQL

**Objetivo:** Contar usuarios activos de un departamento.

**Criterio RA3 que cubre:** CE3.f (Consultas JPQL)

**Pistas de Implementación:**

1. Crear JPQL COUNT: `"SELECT COUNT(u) FROM User u WHERE u.department = :dept AND u.active = true"`
2. Crear `TypedQuery<Long>` (nota: Long, no User)
3. Setear parámetro `:dept`
4. Ejecutar con `getSingleResult()` (retorna un solo número)

**Método TODO en `HibernateUserServiceImpl.java:340`:**

```java
@Override
public long executeCountByDepartment(String department) {
    // TODO CE3.f: Implementar executeCountByDepartment()
    throw new UnsupportedOperationException("TODO...");
}
```

**Estructura Esperada:**

```java
@Override
public long executeCountByDepartment(String department) {
    String jpql = "SELECT COUNT(u) FROM User u WHERE u.department = :dept AND u.active = true";

    TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
    query.setParameter("dept", department);

    return query.getSingleResult();
}
```

**JPQL vs SQL:**

```java
// JPQL
SELECT COUNT(u) FROM User u WHERE u.department = :dept AND u.active = true

// SQL generado por Hibernate
SELECT COUNT(id) FROM users WHERE department = ? AND active = true
```

**Test para Validar:**

```java
@Test
void executeCountByDepartment_IT_ReturnsCorrectCount() {
    long count = service.executeCountByDepartment("IT");

    // En data.sql hay 3 usuarios IT activos
    assertEquals(3, count);
}
```

**IMPORTANTE:** Usa `getSingleResult()` (no `getResultList()`) porque COUNT retorna un solo valor.

---

## 6. Ejecutar y Entender los Tests

### 6.1. Ejecutar Todos los Tests

```bash
./gradlew test
```

**Salida Esperada:**

```
> Task :test

HibernateUserServiceImplTest > testEntityManager_Active_ReturnsMessage() PASSED
HibernateUserServiceImplTest > createUser_ValidDto_ReturnsUser() PASSED
...
HibernateUserServiceIntegrationTest > crudFlow_CompleteLifecycle_Success() PASSED

BUILD SUCCESSFUL in 8s
10 tests completed, 10 passed
```

### 6.2. Ejecutar un Test Específico

```bash
# Desde línea de comandos
./gradlew test --tests HibernateUserServiceIntegrationTest.createUser_ValidDto_Success

# Desde IntelliJ: Click derecho en el test → Run
```

### 6.3. Entender la Estructura de Tests

**Test de Integración:**

```java
@SpringBootTest  // ← Carga contexto completo de Spring
@ActiveProfiles("test")  // ← Usa application-test.yml
class HibernateUserServiceIntegrationTest {

    @Autowired
    private HibernateUserService service;  // ← Spring inyecta el servicio real

    @Test
    @Transactional  // ← Cada test se ejecuta en su propia transacción (rollback automático)
    void createUser_ValidDto_Success() {
        // ARRANGE: Preparar datos
        UserCreateDto dto = new UserCreateDto();
        dto.setName("Test User");
        dto.setEmail("test@example.com");

        // ACT: Ejecutar método
        User created = service.createUser(dto);

        // ASSERT: Verificar resultado
        assertNotNull(created.getId());
        assertEquals("Test User", created.getName());
    }
}
```

### 6.4. Ver SQL Generado en Tests

Los tests muestran el SQL en consola:

```
Hibernate: INSERT INTO users (...) VALUES (?, ?, ...)
binding parameter [1] as [VARCHAR] - [Test User]
binding parameter [2] as [VARCHAR] - [test@example.com]
```

Esto te ayuda a **entender qué SQL genera Hibernate** para cada operación.

---

## 7. Flujo de Trabajo Recomendado

Sigue estos pasos para cada método TODO:

### Paso 1: Estudiar Ejemplo Relacionado

| TODO a Implementar | Ejemplo a Estudiar |
|--------------------|--------------------|
| `deleteUser()` | `findUserById()` (buscar) + concepto de `remove()` |
| `searchUsers()` | `findUsersByDepartment()` (JPQL básico) |
| `transferData()` | `createUser()` (@Transactional) |
| `executeCountByDepartment()` | `findUsersByDepartment()` (JPQL) |

### Paso 2: Leer Comentarios TODO

Cada método TODO tiene comentarios con pistas:

```java
// TODO CE3.e: Implementar deleteUser()
//
// Guía de implementación:
// 1. Buscar usuario: User user = findUserById(id);
// 2. Verificar si existe...
```

### Paso 3: Implementar

Escribe el código siguiendo las pistas.

### Paso 4: Ejecutar Test

```bash
./gradlew test --tests HibernateUserServiceIntegrationTest
```

### Paso 5: Observar SQL

Verifica que el SQL generado sea correcto en los logs.

### Paso 6: Validar con H2 Console

1. Abre http://localhost:8083/h2-console
2. Ejecuta queries para verificar los cambios:

```sql
SELECT * FROM users WHERE id = 100;
SELECT COUNT(*) FROM users WHERE department = 'IT';
```

---

## 8. Recursos Adicionales

### Documentación del Proyecto

- **[GUIA_INSTALACION.md](GUIA_INSTALACION.md)** - Setup y configuración
- **[API_REFERENCIA.md](API_REFERENCIA.md)** - Referencia técnica de endpoints
- **[TESTING_GUIA.md](TESTING_GUIA.md)** - Guía completa de testing
- **[CRITERIOS_RA3_DETALLADO.md](CRITERIOS_RA3_DETALLADO.md)** - Mapeo de criterios
- **[GUIA_HERRAMIENTAS_MCP.md](GUIA_HERRAMIENTAS_MCP.md)** - Uso de MCP tools
- **[PREGUNTAS_FRECUENTES.md](PREGUNTAS_FRECUENTES.md)** - FAQ y solución de problemas
- **[ARQUITECTURA.md](ARQUITECTURA.md)** - Diseño del sistema

### Documentación Pedagógica

- **[Explicacion_Clase_Hibernate.md](../Explicacion_Clase_Hibernate.md)** - Guía didáctica completa
- **[Criterios.md](../Criterios.md)** - Criterios oficiales RA3
- **[Recomendacion_Minimos.md](../Recomendacion_Minimos.md)** - Requisitos mínimos

### Documentación Oficial

- **Spring Data JPA**: https://docs.spring.io/spring-data/jpa/reference/
- **Hibernate ORM**: https://hibernate.org/orm/documentation/
- **Jakarta Persistence (JPA)**: https://jakarta.ee/specifications/persistence/

### Tutoriales

- **Baeldung - JPA/Hibernate**: https://www.baeldung.com/learn-jpa-hibernate
- **Spring Boot JPA**: https://spring.io/guides/gs/accessing-data-jpa/

---

## Resumen

✅ **Estudiaste** los 6 métodos implementados
✅ **Entendiste** las diferencias entre JDBC (RA2) y Hibernate (RA3)
✅ **Implementaste** los 4 métodos TODO
✅ **Validaste** tu código con tests
✅ **Observaste** el SQL generado por Hibernate

**¡Felicidades!** Has completado el aprendizaje de RA3 (Hibernate/JPA).

---

**¿Tienes dudas?** Consulta [PREGUNTAS_FRECUENTES.md](PREGUNTAS_FRECUENTES.md) o revisa los ejemplos en [EJEMPLOS/](EJEMPLOS/).
