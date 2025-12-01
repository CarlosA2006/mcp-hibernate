# Guía de Testing - mcp-hibernate

Esta guía te enseñará a escribir y ejecutar tests para validar tus implementaciones de Hibernate/JPA.

## Tabla de Contenidos

1. [Introducción al Testing en Spring Boot](#1-introducción-al-testing-en-spring-boot)
2. [Estructura de Tests Existentes](#2-estructura-de-tests-existentes)
3. [Cómo Ejecutar Tests](#3-cómo-ejecutar-tests)
4. [Entender Tests Existentes](#4-entender-tests-existentes)
5. [Escribir Tests para Métodos TODO](#5-escribir-tests-para-métodos-todo)
6. [Base de Datos de Test](#6-base-de-datos-de-test)
7. [Debugging de Tests](#7-debugging-de-tests)

---

## 1. Introducción al Testing en Spring Boot

### ¿Por qué testing?

Los tests automáticos te permiten:

✅ **Validar** que tu código funciona correctamente
✅ **Detectar errores** antes de ejecutar la aplicación
✅ **Documentar** el comportamiento esperado
✅ **Refactorizar** con confianza (los tests detectan regresiones)

### Frameworks de Testing

Este proyecto usa:

- **JUnit 5 (Jupiter)**: Framework de testing para Java
- **Spring Boot Test**: Integración de Spring con JUnit
- **Mockito**: Para crear mocks (tests unitarios)
- **H2 Database**: Base de datos en memoria para tests

---

## 2. Estructura de Tests Existentes

### Archivos de Test

```
src/test/java/com/dam/accesodatos/ra3/
├── HibernateUserServiceImplTest.java          # Tests unitarios (con mocks)
└── HibernateUserServiceIntegrationTest.java   # Tests de integración (BD real)
```

### Diferencias: Tests Unitarios vs Integración

| Tipo | Tests Unitarios | Tests de Integración |
|------|-----------------|----------------------|
| **Archivo** | `HibernateUserServiceImplTest` | `HibernateUserServiceIntegrationTest` |
| **Anotación** | `@ExtendWith(MockitoExtension.class)` | `@SpringBootTest` |
| **BD Real** | ❌ No (usa mocks) | ✅ Sí (H2 en memoria) |
| **Velocidad** | ⚡ Muy rápido | 🐢 Más lento |
| **Propósito** | Aislar lógica del servicio | Validar integración completa |

**Para este proyecto, nos enfocamos en Tests de Integración** porque validan el comportamiento real con Hibernate y BD.

---

## 3. Cómo Ejecutar Tests

### 3.1. Ejecutar Todos los Tests

```bash
# Desde línea de comandos
./gradlew test

# Con logs detallados
./gradlew test --info
```

**Salida Esperada:**

```
> Task :test

HibernateUserServiceIntegrationTest > testEntityManager_RealConnection_Success() PASSED
HibernateUserServiceIntegrationTest > createUser_ValidDto_Success() PASSED
HibernateUserServiceIntegrationTest > crudFlow_CompleteLifecycle_Success() PASSED
...

BUILD SUCCESSFUL in 10s
```

### 3.2. Ejecutar un Test Específico

```bash
# Ejecutar una clase completa
./gradlew test --tests HibernateUserServiceIntegrationTest

# Ejecutar un solo método
./gradlew test --tests HibernateUserServiceIntegrationTest.createUser_ValidDto_Success
```

### 3.3. Ejecutar desde el IDE

**IntelliJ IDEA:**
- Click derecho en el test → `Run 'testName()'`
- Click en el icono verde ▶️ junto al método
- Atajo: `Ctrl+Shift+F10` (Windows/Linux) o `Ctrl+Shift+R` (Mac)

**VS Code:**
- Instala extensión "Test Runner for Java"
- Click en "Run Test" que aparece sobre el método `@Test`

**Eclipse:**
- Click derecho en el test → `Run As` → `JUnit Test`

---

## 4. Entender Tests Existentes

### 4.1. Anatomía de un Test

```java
@Test  // ← Marca método como test
@DisplayName("Crear usuario con DTO válido debe retornar usuario con ID")  // ← Descripción legible
void createUser_ValidDto_Success() {  // ← Nombre descriptivo: método_condición_resultado
    // ARRANGE: Preparar datos de entrada
    UserCreateDto dto = new UserCreateDto();
    dto.setName("Test User");
    dto.setEmail("test@example.com");
    dto.setDepartment("IT");
    dto.setRole("Developer");

    // ACT: Ejecutar el método a probar
    User created = service.createUser(dto);

    // ASSERT: Verificar que el resultado es el esperado
    assertNotNull(created);
    assertNotNull(created.getId());  // ID debe estar generado
    assertEquals("Test User", created.getName());
    assertEquals("test@example.com", created.getEmail());
    assertTrue(created.getActive());  // Por defecto debe ser true
}
```

**Patrón AAA (Arrange-Act-Assert):**
1. **Arrange**: Preparar datos y estado inicial
2. **Act**: Ejecutar el método bajo prueba
3. **Assert**: Verificar resultados

### 4.2. Test: `testEntityManager_RealConnection_Success()`

```java
@Test
void testEntityManager_RealConnection_Success() {
    String result = service.testEntityManager();

    assertNotNull(result);
    assertTrue(result.contains("EntityManager activo"));
    assertTrue(result.contains("RA3DB"));  // Nombre de la BD H2
}
```

**Qué valida:**
- EntityManager está activo
- La conexión a H2 funciona
- El resultado contiene información esperada

### 4.3. Test: `crudFlow_CompleteLifecycle_Success()`

```java
@Test
@Transactional
void crudFlow_CompleteLifecycle_Success() {
    // 1. CREATE
    User created = service.createUser(createDto);
    assertNotNull(created.getId());

    // 2. READ
    User found = service.findUserById(created.getId());
    assertEquals(created.getName(), found.getName());

    // 3. UPDATE
    UserUpdateDto updateDto = new UserUpdateDto();
    updateDto.setName("Updated Name");
    User updated = service.updateUser(created.getId(), updateDto);
    assertEquals("Updated Name", updated.getName());

    // 4. VERIFY
    User verified = service.findUserById(created.getId());
    assertEquals("Updated Name", verified.getName());
}
```

**Qué valida:**
- Flujo CRUD completo (Create, Read, Update)
- Los cambios persisten en la BD
- Los métodos funcionan en secuencia

### 4.4. Aserciones Comunes

```java
// Verificar no null
assertNotNull(user);
assertNull(user);  // null

// Igualdad
assertEquals(expected, actual);
assertNotEquals(expected, actual);

// Booleanos
assertTrue(condition);
assertFalse(condition);

// Colecciones
assertNotEmpty(list);
assertEquals(5, list.size());

// Excepciones
assertThrows(RuntimeException.class, () -> {
    service.deleteUser(999L);
});
```

---

## 5. Escribir Tests para Métodos TODO

### 5.1. Test para `deleteUser()`

**Archivo:** `HibernateUserServiceIntegrationTest.java`

```java
@Test
@Transactional
@DisplayName("Eliminar usuario existente debe retornar true y eliminar de BD")
void deleteUser_ExistingUser_Success() {
    // ARRANGE: Crear usuario primero
    UserCreateDto dto = new UserCreateDto();
    dto.setName("To Delete");
    dto.setEmail("delete@test.com");
    dto.setDepartment("IT");
    dto.setRole("Tester");

    User created = service.createUser(dto);
    Long userId = created.getId();

    // ACT: Eliminar el usuario
    boolean deleted = service.deleteUser(userId);

    // ASSERT: Verificar eliminación
    assertTrue(deleted);  // Debe retornar true
    assertNull(service.findUserById(userId));  // No debe existir en BD
}

@Test
@DisplayName("Eliminar usuario inexistente debe retornar false")
void deleteUser_NonExistentUser_ReturnsFalse() {
    // ACT
    boolean deleted = service.deleteUser(99999L);

    // ASSERT
    assertFalse(deleted);  // No existía, retorna false
}
```

### 5.2. Test para `searchUsers()`

```java
@Test
@Transactional
@DisplayName("Buscar usuarios por departamento debe retornar solo los del departamento")
void searchUsers_ByDepartment_ReturnsMatching() {
    // ARRANGE: Crear query DTO con filtro de departamento
    UserQueryDto queryDto = new UserQueryDto();
    queryDto.setDepartment("IT");

    // ACT
    List<User> results = service.searchUsers(queryDto);

    // ASSERT
    assertFalse(results.isEmpty());
    results.forEach(user -> {
        assertEquals("IT", user.getDepartment());
    });
}

@Test
@Transactional
@DisplayName("Buscar con múltiples filtros debe aplicar AND")
void searchUsers_MultipleFilters_ReturnsMatching() {
    // ARRANGE
    UserQueryDto queryDto = new UserQueryDto();
    queryDto.setDepartment("IT");
    queryDto.setActive(true);

    // ACT
    List<User> results = service.searchUsers(queryDto);

    // ASSERT
    results.forEach(user -> {
        assertEquals("IT", user.getDepartment());
        assertTrue(user.getActive());
    });
}

@Test
@DisplayName("Buscar sin filtros debe retornar todos")
void searchUsers_NoFilters_ReturnsAll() {
    // ARRANGE
    UserQueryDto queryDto = new UserQueryDto();  // Sin filtros

    // ACT
    List<User> results = service.searchUsers(queryDto);

    // ASSERT
    assertEquals(8, results.size());  // 8 usuarios precargados en data.sql
}
```

### 5.3. Test para `transferData()`

```java
@Test
@Transactional
@DisplayName("Transferir múltiples usuarios debe insertarlos en una transacción")
void transferData_MultipleUsers_AllInserted() {
    // ARRANGE: Crear lista de usuarios
    List<User> users = Arrays.asList(
        createTestUser("Batch User 1", "batch1@test.com"),
        createTestUser("Batch User 2", "batch2@test.com"),
        createTestUser("Batch User 3", "batch3@test.com")
    );

    int initialCount = service.findAll().size();

    // ACT
    boolean success = service.transferData(users);

    // ASSERT
    assertTrue(success);
    assertEquals(initialCount + 3, service.findAll().size());
}

// Helper method
private User createTestUser(String name, String email) {
    User user = new User();
    user.setName(name);
    user.setEmail(email);
    user.setDepartment("IT");
    user.setRole("Developer");
    user.setActive(true);
    user.setCreatedAt(LocalDateTime.now());
    user.setUpdatedAt(LocalDateTime.now());
    return user;
}
```

### 5.4. Test para `executeCountByDepartment()`

```java
@Test
@DisplayName("Contar usuarios por departamento debe retornar número correcto")
void executeCountByDepartment_IT_ReturnsCorrectCount() {
    // ACT
    long count = service.executeCountByDepartment("IT");

    // ASSERT: En data.sql hay 3 usuarios IT activos
    assertEquals(3, count);
}

@Test
@DisplayName("Contar departamento sin usuarios debe retornar 0")
void executeCountByDepartment_NonExistent_ReturnsZero() {
    // ACT
    long count = service.executeCountByDepartment("NonExistent");

    // ASSERT
    assertEquals(0, count);
}

@Test
@Transactional
@DisplayName("Contar debe excluir usuarios inactivos")
void executeCountByDepartment_OnlyActive_Excluded() {
    // ARRANGE: Crear usuario inactivo
    UserCreateDto dto = new UserCreateDto();
    dto.setName("Inactive");
    dto.setEmail("inactive@test.com");
    dto.setDepartment("Marketing");
    dto.setRole("Specialist");

    User created = service.createUser(dto);
    created.setActive(false);
    service.updateUser(created.getId(), new UserUpdateDto());

    // ACT: Contar departamento (debe ser 1, solo el activo de data.sql)
    long count = service.executeCountByDepartment("Marketing");

    // ASSERT
    assertEquals(1, count);  // No incluye el inactivo
}
```

---

## 6. Base de Datos de Test

### 6.1. Configuración de Test

**Archivo:** `src/main/resources/application.yml`

La configuración es la misma para tests (usa H2 en memoria).

### 6.2. Anotación `@Transactional` en Tests

```java
@Test
@Transactional  // ← Rollback automático al finalizar test
void testMethod() {
    // Cambios en BD se revierten después del test
}
```

**Beneficio:** Cada test es independiente y no afecta a otros.

### 6.3. Datos Precargados

El archivo `src/main/resources/data.sql` carga 8 usuarios al iniciar:

| ID | Nombre | Departamento | Role | Active |
|----|--------|--------------|------|--------|
| 1 | Juan Pérez | IT | Developer | ✅ |
| 2 | María García | HR | Manager | ✅ |
| 3 | Carlos López | Finance | Analyst | ✅ |
| 4 | Ana Martínez | IT | Senior Developer | ✅ |
| 5 | Luis Rodríguez | Marketing | Specialist | ✅ |
| 6 | Elena Fernández | IT | DevOps | ❌ Inactive |
| 7 | Pedro Sánchez | Sales | Representative | ✅ |
| 8 | Laura González | HR | Recruiter | ✅ |

**En tests, puedes asumir que estos usuarios existen.**

---

## 7. Debugging de Tests

### 7.1. Ver Logs de Hibernate

Los tests muestran SQL generado en consola:

```
Hibernate: INSERT INTO users (name, email, ...) VALUES (?, ?, ...)
binding parameter [1] as [VARCHAR] - [Test User]
```

### 7.2. Tests Fallan: ¿Qué hacer?

#### Error: AssertionError

```
expected: <true> but was: <false>
```

**Solución:**
- Revisa la lógica del método
- Usa debugger para ver valores reales
- Verifica que el SQL generado sea correcto

#### Error: EntityNotFoundException

```
jakarta.persistence.EntityNotFoundException: Unable to find User with id 999
```

**Solución:**
- El usuario no existe en la BD
- Verifica que creaste el usuario antes de buscarlo
- Revisa `data.sql` para IDs precargados

#### Error: ConstraintViolationException

```
org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException: Unique index or primary key violation
```

**Solución:**
- Estás intentando insertar un email duplicado
- Usa emails únicos en cada test: `test1@example.com`, `test2@example.com`

### 7.3. Debugging con Breakpoints

**IntelliJ IDEA:**
1. Click en el margen izquierdo junto a la línea → Breakpoint rojo
2. Click derecho en test → `Debug 'testName()'`
3. Usa `F8` (step over), `F7` (step into) para navegar
4. Inspecciona variables en el panel de debugging

---

## Resumen

✅ **Ejecutar tests**: `./gradlew test`
✅ **Patrón AAA**: Arrange, Act, Assert
✅ **@Transactional**: Rollback automático en tests
✅ **Validar SQL**: Observa logs de Hibernate
✅ **Tests de integración**: Validan comportamiento real con BD

**¡Ahora puedes escribir tests para validar tus implementaciones!**

Para más información sobre los métodos a implementar, consulta [GUIA_ESTUDIANTE.md](GUIA_ESTUDIANTE.md).
