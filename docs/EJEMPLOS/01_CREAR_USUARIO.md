# Ejemplo 1: Crear Usuario con persist()

## Objetivo

Aprender a insertar un nuevo registro en la base de datos usando `EntityManager.persist()`.

## Conocimientos Previos

- Entidades JPA (@Entity)
- @Transactional
- Constructor de entidades

## Código Completo

```java
@Override
@Transactional  // ← OBLIGATORIO para operaciones de escritura
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

    // 2. Persistir en el contexto de persistencia
    entityManager.persist(user);
    // Hibernate marca el objeto como "managed" y programa un INSERT

    // 3. Retornar usuario (ID ya está seteado por Hibernate)
    return user;  // ID generado automáticamente
}
```

## SQL Generado Automáticamente

```sql
INSERT INTO users (name, email, department, role, active, created_at, updated_at)
VALUES ('Ana López', 'ana.lopez@test.com', 'IT', 'Developer', true, '2025-01-15 10:30:00', '2025-01-15 10:30:00')
```

## Paso a Paso

### 1. Crear el objeto entidad

```java
User user = new User();
user.setName(dto.getName());
// ... más setters
```

**No necesitas setear el ID** - Hibernate lo generará automáticamente porque tenemos:

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```

### 2. Persistir con persist()

```java
entityManager.persist(user);
```

Esto marca el objeto como "managed" en el contexto de persistencia. El INSERT no se ejecuta inmediatamente, sino cuando la transacción hace commit.

### 3. @Transactional maneja el commit

```java
@Transactional  // Spring hace:
public User createUser(...) {
    // BEGIN TRANSACTION
    entityManager.persist(user);
    // ... lógica adicional
    return user;
    // COMMIT (ejecuta el INSERT)
}
```

**Si hay excepción** → ROLLBACK automático

## Comparación RA2 vs RA3

### RA2 (JDBC Manual)

```java
String sql = "INSERT INTO users (name, email, department, role, active, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
ps.setString(1, dto.getName());
ps.setString(2, dto.getEmail());
ps.setString(3, dto.getDepartment());
ps.setString(4, dto.getRole());
ps.setBoolean(5, true);
ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
ps.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
ps.executeUpdate();

// Obtener ID generado
ResultSet keys = ps.getGeneratedKeys();
if (keys.next()) {
    user.setId(keys.getLong(1));
}
```

**17 líneas de código manual**

### RA3 (Hibernate Automático)

```java
User user = new User();
user.setName(dto.getName());
// ... más setters
entityManager.persist(user);
return user;  // ID ya está seteado
```

**5 líneas de código automático**

## Test Case

```java
@Test
@Transactional
void createUser_ValidDto_ReturnsUserWithId() {
    // ARRANGE
    UserCreateDto dto = new UserCreateDto();
    dto.setName("Test User");
    dto.setEmail("test@example.com");
    dto.setDepartment("IT");
    dto.setRole("Developer");

    // ACT
    User created = service.createUser(dto);

    // ASSERT
    assertNotNull(created);
    assertNotNull(created.getId());  // ID generado automáticamente
    assertEquals("Test User", created.getName());
    assertTrue(created.getActive());  // Default true
}
```

## Ejercicio Práctico

Modifica `createUser()` para:

1. Validar que el email no exista (usar `userRepository.findByEmail()`)
2. Si existe, lanzar excepción `IllegalArgumentException`
3. Verificar con un test

**Pista:**
```java
User existing = userRepository.findByEmail(dto.getEmail());
if (existing != null) {
    throw new IllegalArgumentException("Email ya existe");
}
```

## Conceptos Clave

- **persist()**: Inserta un objeto nuevo en BD
- **@Transactional**: Maneja commit/rollback automáticamente
- **@GeneratedValue**: ID autogenerado por la BD
- **Managed state**: Objeto rastreado por Hibernate

Para más información, consulta [GUIA_ESTUDIANTE.md](../GUIA_ESTUDIANTE.md#42-método-2-createuser---insert-con-persist).
