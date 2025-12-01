# Criterios RA3 Detallado - Mapeo a Implementación

Este documento mapea cada criterio de evaluación RA3 a su implementación en el código.

## Criterios Oficiales

Del archivo [Criterios.md](../Criterios.md):

> **RA3.** Gestiona la persistencia de los datos identificando herramientas de mapeo objeto relacional (ORM) y desarrollando aplicaciones que las utilizan.

## Mapeo Criterio por Criterio

### a) Se ha instalado la herramienta ORM

**Qué se requiere:**
- Agregar Hibernate/JPA como dependencia del proyecto
- Configurar el ORM en el sistema de construcción

**Dónde se demuestra:**

**Archivo:** `build.gradle:36`
```gradle
// RA3: Hibernate/JPA - Acceso a datos mediante ORM
implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
```

**Validación:**
- Método `testEntityManager()` verifica que Hibernate está activo
- Tests ejecutan operaciones JPA correctamente

---

### b) Se ha configurado la herramienta ORM

**Qué se requiere:**
- Configurar conexión a base de datos
- Configurar propiedades de Hibernate (show SQL, dialect, etc.)
- Configurar estrategia de generación de esquema

**Dónde se demuestra:**

**Archivo:** `src/main/resources/application.yml:11-40`
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:ra3db
    driver-class-name: org.h2.Driver

  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: none
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        generate_statistics: true
```

**Validación:**
- Aplicación inicia correctamente
- Logs muestran SQL generado por Hibernate
- H2 Console accesible en http://localhost:8083/h2-console

---

### c) Se han definido los ficheros de mapeo

**Qué se requiere:**
- Definir clases entidad que mapean a tablas
- Usar anotaciones JPA para mapeo (@Entity, @Table, @Column, etc.)

**Dónde se demuestra:**

**Archivo:** `src/main/java/com/dam/accesodatos/model/User.java:1`
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

**Anotaciones clave:**
- `@Entity`: Marca la clase como entidad JPA
- `@Table(name = "users")`: Mapea a tabla "users"
- `@Id`: Define clave primaria
- `@GeneratedValue(IDENTITY)`: ID autogenerado por BD
- `@Column`: Configuración de columnas (nullable, length, unique)

**Validación:**
- Hibernate genera correctamente CREATE TABLE
- Operaciones CRUD funcionan con el mapeo

---

### d) Se han aplicado mecanismos de persistencia a los objetos

**Qué se requiere:**
- Usar EntityManager para persistir objetos
- Usar operaciones persist(), merge(), remove()
- Entender el contexto de persistencia

**Dónde se demuestra:**

**Método:** `createUser()` en `HibernateUserServiceImpl.java:101`
```java
@Override
@Transactional
public User createUser(UserCreateDto dto) {
    User user = new User();
    user.setName(dto.getName());
    // ... setear campos

    entityManager.persist(user);  // ← Mecanismo de persistencia

    return user;  // ID ya generado
}
```

**Otros métodos:**
- `updateUser()`: usa `merge()` para sincronizar cambios
- `deleteUser()` (TODO): usa `remove()` para eliminar

**Concepto clave:** Contexto de persistencia
- Objetos "managed" son rastreados por Hibernate
- Cambios se sincronizan automáticamente con BD

**Validación:**
- Test `createUser_ValidDto_Success()` verifica persist()
- Test `updateUser_ValidChanges_Success()` verifica merge()

---

### e) Se han desarrollado aplicaciones que modifican y recuperan objetos persistentes

**Qué se requiere:**
- Implementar CRUD completo (Create, Read, Update, Delete)
- Usar find() para recuperar objetos
- Modificar objetos y persistir cambios

**Dónde se demuestra:**

**CREATE:** `createUser()` - `HibernateUserServiceImpl.java:101`
```java
entityManager.persist(user);
```

**READ:** `findUserById()` - `HibernateUserServiceImpl.java:136`
```java
return entityManager.find(User.class, id);
```

**UPDATE:** `updateUser()` - `HibernateUserServiceImpl.java:159`
```java
User existing = findUserById(id);
existing.setName(dto.getName());  // Modificar
return entityManager.merge(existing);  // Persistir cambios
```

**DELETE:** `deleteUser()` (TODO) - `HibernateUserServiceImpl.java:192`
```java
// TODO: entityManager.remove(user);
```

**Método adicional:** `findAll()` - `HibernateUserServiceImpl.java:229`
```java
return userRepository.findAll();  // Spring Data JPA
```

**Validación:**
- Test `crudFlow_CompleteLifecycle_Success()` valida ciclo CRUD completo
- Cada operación tiene test específico

---

### f) Se han desarrollado aplicaciones que realizan consultas usando el lenguaje SQL (o el propio de la herramienta, como HQL/JPQL)

**Qué se requiere:**
- Escribir consultas JPQL (Java Persistence Query Language)
- Usar TypedQuery para type-safety
- Usar parámetros nombrados
- Consultas de agregación (COUNT, SUM, etc.)

**Dónde se demuestra:**

**JPQL Básico:** `findUsersByDepartment()` - `HibernateUserServiceImpl.java:252`
```java
String jpql = "SELECT u FROM User u WHERE u.department = :dept AND u.active = true ORDER BY u.name";
TypedQuery<User> query = entityManager.createQuery(jpql, User.class);
query.setParameter("dept", department);
return query.getResultList();
```

**JPQL Dinámico:** `searchUsers()` (TODO) - `HibernateUserServiceImpl.java:267`
```java
// TODO: Construir JPQL dinámicamente según filtros
StringBuilder jpql = new StringBuilder("SELECT u FROM User u WHERE 1=1");
if (queryDto.getDepartment() != null) {
    jpql.append(" AND u.department = :dept");
}
```

**JPQL COUNT:** `executeCountByDepartment()` (TODO) - `HibernateUserServiceImpl.java:340`
```java
// TODO:
String jpql = "SELECT COUNT(u) FROM User u WHERE u.department = :dept";
TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
```

**Características JPQL:**
- Orientado a objetos (usa entidades, no tablas)
- Parámetros nombrados (`:param`)
- Type-safe con TypedQuery<T>

**Validación:**
- Test `findUsersByDepartment_WithRealData_Success()`
- Tests para TODO methods validarán JPQL dinámico y COUNT

---

### g) Se han gestionado las transacciones

**Qué se requiere:**
- Usar @Transactional para delimitar transacciones
- Entender commit y rollback automáticos
- Operaciones atómicas (todo o nada)

**Dónde se demuestra:**

**Transacciones de escritura:** `createUser()` - `HibernateUserServiceImpl.java:102`
```java
@Override
@Transactional  // ← Spring maneja transacción
public User createUser(UserCreateDto dto) {
    entityManager.persist(user);
    return user;
    // Spring hace commit automáticamente si no hay error
    // Spring hace rollback si hay excepción
}
```

**Transacción múltiple:** `transferData()` (TODO) - `HibernateUserServiceImpl.java:308`
```java
@Override
@Transactional
public boolean transferData(List<User> users) {
    // TODO: Persistir múltiples usuarios
    // Si uno falla, todos hacen rollback (atomicidad)
}
```

**Transacciones read-only:** Clase completa - `HibernateUserServiceImpl.java:47`
```java
@Service
@Transactional(readOnly = true)  // Por defecto read-only
public class HibernateUserServiceImpl {
    // Métodos de escritura sobrescriben con @Transactional (sin readOnly)
}
```

**Comparación RA2 vs RA3:**

| RA2 (JDBC Manual) | RA3 (Spring @Transactional) |
|-------------------|----------------------------|
| `conn.setAutoCommit(false)` | `@Transactional` |
| `conn.commit()` manual | Commit automático si éxito |
| `conn.rollback()` en catch | Rollback automático si excepción |

**Validación:**
- Test `transferData_MultipleUsers_AllInsertedOrNone()` valida atomicidad
- Si un `persist()` falla, todos hacen rollback

---

## Tabla Resumen: Método → Criterios

| Método | Criterios Cubiertos |
|--------|---------------------|
| `testEntityManager()` | a) Instalación, b) Configuración |
| `createUser()` | d) Persistencia, e) Modificación, g) Transacciones |
| `findUserById()` | e) Recuperación |
| `updateUser()` | d) Persistencia (merge), e) Modificación, g) Transacciones |
| `deleteUser()` (TODO) | d) Persistencia (remove), e) Modificación |
| `findAll()` | e) Recuperación |
| `findUsersByDepartment()` | f) JPQL |
| `searchUsers()` (TODO) | f) JPQL dinámico |
| `transferData()` (TODO) | d) Persistencia, g) Transacciones múltiples |
| `executeCountByDepartment()` (TODO) | f) JPQL agregación (COUNT) |
| **User.java** (entidad) | c) Ficheros de mapeo |

---

## Verificación de Cobertura

### Criterio a) Instalación ORM
- ✅ `build.gradle:36` - dependencia spring-boot-starter-data-jpa
- ✅ `testEntityManager()` - verifica Hibernate activo

### Criterio b) Configuración ORM
- ✅ `application.yml:11-40` - datasource, JPA properties, Hibernate config

### Criterio c) Ficheros de mapeo
- ✅ `User.java:1` - @Entity, @Table, @Column, @Id, @GeneratedValue

### Criterio d) Mecanismos de persistencia
- ✅ `createUser()` - persist()
- ✅ `updateUser()` - merge()
- ⚠️ `deleteUser()` (TODO) - remove()

### Criterio e) Modificación y recuperación
- ✅ `createUser()` - CREATE
- ✅ `findUserById()` - READ
- ✅ `updateUser()` - UPDATE
- ⚠️ `deleteUser()` (TODO) - DELETE
- ✅ `findAll()` - READ all

### Criterio f) Consultas JPQL
- ✅ `findUsersByDepartment()` - JPQL básico
- ⚠️ `searchUsers()` (TODO) - JPQL dinámico
- ⚠️ `executeCountByDepartment()` (TODO) - JPQL COUNT

### Criterio g) Transacciones
- ✅ `createUser()`, `updateUser()` - @Transactional individual
- ⚠️ `transferData()` (TODO) - @Transactional múltiple

**Leyenda:**
- ✅ Implementado completamente
- ⚠️ TODO (estudiante debe implementar)

---

## Lista de Verificación para Estudiantes

¿Has cubierto todos los criterios?

- [ ] **a)** ¿Entiendes qué dependencia instala Hibernate? (`build.gradle`)
- [ ] **b)** ¿Entiendes la configuración JPA? (`application.yml`)
- [ ] **c)** ¿Entiendes el mapeo de `User.java`? (@Entity, @Id, @Column)
- [ ] **d)** ¿Has usado `persist()`, `merge()`, `remove()`?
- [ ] **e)** ¿Implementaste CRUD completo?
- [ ] **f)** ¿Escribiste JPQL básico y dinámico?
- [ ] **g)** ¿Entiendes cómo funcionan las transacciones con @Transactional?

**Si marcaste todos, ¡has cubierto RA3 completamente!**

---

Para más información, consulta:
- [GUIA_ESTUDIANTE.md](GUIA_ESTUDIANTE.md) - Implementación paso a paso
- [Criterios.md](../Criterios.md) - Criterios oficiales del currículo
- [Explicacion_Clase_Hibernate.md](../Explicacion_Clase_Hibernate.md) - Teoría de ORM
