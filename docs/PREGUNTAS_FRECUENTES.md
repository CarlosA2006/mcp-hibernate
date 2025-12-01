# Preguntas Frecuentes (FAQ)

Soluciones a preguntas comunes sobre el proyecto mcp-hibernate.

## Preguntas Generales

### ¿Cuál es la diferencia entre RA2 y RA3?

**RA2 (JDBC):** Acceso manual a base de datos escribiendo SQL y mapeando `ResultSet` manualmente.

**RA3 (Hibernate/JPA):** ORM automático que genera SQL y mapea objetos automáticamente.

**Ejemplo:**
```java
// RA2
String sql = "SELECT * FROM users WHERE id = ?";
PreparedStatement ps = conn.prepareStatement(sql);
ps.setLong(1, id);
ResultSet rs = ps.executeQuery();
// Mapeo manual de 8 campos...

// RA3
User user = entityManager.find(User.class, id);  // ¡Una línea!
```

### ¿Por qué usar ORM?

**Ventajas:**
- ✅ Menos código (no escribes SQL manualmente)
- ✅ Mapeo automático de ResultSet a objetos
- ✅ Type-safe (errores en compilación, no en runtime)
- ✅ Manejo automático de transacciones
- ✅ Cache de primer y segundo nivel
- ✅ Lazy loading de relaciones
- ✅ Portabilidad entre bases de datos (cambias dialect, no queries)

**Desventajas:**
- ⚠️ Curva de aprendizaje inicial
- ⚠️ Queries complejas pueden requerir SQL nativo
- ⚠️ N+1 queries si no se usa correctamente

---

## Configuración e Instalación

### ¿Por qué no funciona el puerto 8083?

**Problema:** `Port 8083 is already in use`

**Solución 1:** Matar el proceso que usa el puerto
```bash
# Linux/Mac
lsof -i :8083
kill -9 <PID>

# Windows
netstat -ano | findstr :8083
taskkill /PID <PID> /F
```

**Solución 2:** Cambiar puerto en `application.yml`
```yaml
server:
  port: 8084  # Usar otro puerto
```

### ¿Cómo accedo a H2 Console?

1. Asegúrate de que la aplicación esté corriendo
2. Abre http://localhost:8083/h2-console
3. Configuración:
   - **JDBC URL:** `jdbc:h2:mem:ra3db`
   - **User:** `sa`
   - **Password:** *(vacío)*
4. Click "Connect"

### ¿Puedo cambiar a PostgreSQL o MySQL?

Sí, solo necesitas cambiar la configuración:

**application.yml:**
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/mydb
    driver-class-name: org.postgresql.Driver
    username: postgres
    password: mypassword

  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
```

**build.gradle:**
```gradle
// Quitar H2, agregar PostgreSQL
runtimeOnly 'org.postgresql:postgresql'
```

---

## Conceptos de Hibernate/JPA

### ¿Qué es @Entity?

`@Entity` marca una clase como entidad JPA, mapeada a una tabla de base de datos.

```java
@Entity  // ← Hibernate sabe que esto mapea a una tabla
@Table(name = "users")
public class User {
    // ...
}
```

### ¿Por qué necesito un constructor sin argumentos?

Hibernate usa reflexión para instanciar entidades. Crea un objeto vacío y luego setea los valores:

```java
// Hibernate hace esto internamente:
User user = new User();  // ← Necesita constructor sin args
user.setId(rs.getLong("id"));
user.setName(rs.getString("name"));
// ...
```

**Solución:** Siempre añade un constructor sin argumentos en entidades:
```java
@Entity
public class User {
    public User() {}  // ← Requerido por Hibernate

    public User(String name, String email) {  // Constructor con args (opcional)
        this.name = name;
        this.email = email;
    }
}
```

### ¿Qué diferencia hay entre persist() y save()?

**`persist()` (EntityManager - JPA estándar):**
- Hace el objeto "managed" inmediatamente
- Solo funciona con objetos nuevos (transient)
- Lanza excepción si el objeto ya existe

**`save()` (Hibernate Session):**
- Retorna el ID generado
- Puede usarse con objetos nuevos o existentes
- Es específico de Hibernate

**En este proyecto usamos `persist()` porque es JPA estándar.**

### ¿Qué es merge()?

`merge()` sincroniza un objeto con la base de datos.

**Cuándo usar:**
- Actualizar objetos detached (fuera del contexto)
- Aplicar cambios a entidades existentes

```java
// 1. Buscar usuario (es managed)
User user = entityManager.find(User.class, 1L);

// 2. Modificar
user.setName("Nuevo nombre");

// 3. merge() actualiza en BD
entityManager.merge(user);

// Al hacer commit, Hibernate ejecuta UPDATE
```

### ¿Por qué @Transactional?

`@Transactional` le dice a Spring que maneje la transacción automáticamente:

```java
@Transactional
public void crearUsuario() {
    entityManager.persist(user);
    // Spring hace COMMIT automáticamente al finalizar
    // Spring hace ROLLBACK si hay excepción
}
```

**Sin @Transactional:**
```
org.hibernate.TransactionRequiredException: no transaction is in progress
```

---

## Implementación de Métodos TODO

### ¿Cómo implemento deleteUser()?

**Pasos:**
1. Buscar usuario con `find()`
2. Verificar si existe
3. Eliminar con `remove()`

```java
@Override
@Transactional
public boolean deleteUser(Long id) {
    User user = entityManager.find(User.class, id);
    
    if (user == null) {
        return false;
    }
    
    entityManager.remove(user);
    return true;
}
```

**IMPORTANTE:** `remove()` requiere que el objeto esté "managed" (por eso usamos `find()` primero).

### ¿Cómo hago la búsqueda dinámica en searchUsers()?

**Patrón:**
1. StringBuilder para construir JPQL
2. Añadir condiciones según filtros presentes
3. Setear parámetros solo para filtros usados

```java
@Override
public List<User> searchUsers(UserQueryDto queryDto) {
    StringBuilder jpql = new StringBuilder("SELECT u FROM User u WHERE 1=1");
    
    if (queryDto.getDepartment() != null) {
        jpql.append(" AND u.department = :dept");
    }
    if (queryDto.getRole() != null) {
        jpql.append(" AND u.role = :role");
    }
    if (queryDto.getActive() != null) {
        jpql.append(" AND u.active = :active");
    }
    
    TypedQuery<User> query = entityManager.createQuery(jpql.toString(), User.class);
    
    if (queryDto.getDepartment() != null) {
        query.setParameter("dept", queryDto.getDepartment());
    }
    if (queryDto.getRole() != null) {
        query.setParameter("role", queryDto.getRole());
    }
    if (queryDto.getActive() != null) {
        query.setParameter("active", queryDto.getActive());
    }
    
    return query.getResultList();
}
```

### ¿Cómo funcionan las transacciones en transferData()?

```java
@Override
@Transactional  // ← TODO en una transacción
public boolean transferData(List<User> users) {
    for (User user : users) {
        entityManager.persist(user);
    }
    return true;
    // Si todo OK → Spring hace COMMIT de todos
    // Si uno falla → Spring hace ROLLBACK de todos (atomicidad)
}
```

**Atomicidad:** O se insertan todos o ninguno.

### ¿Cómo uso COUNT en JPQL?

```java
@Override
public long executeCountByDepartment(String department) {
    String jpql = "SELECT COUNT(u) FROM User u WHERE u.department = :dept AND u.active = true";
    
    TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
    query.setParameter("dept", department);
    
    return query.getSingleResult();  // ← Retorna un Long
}
```

**Nota:** Usa `getSingleResult()` (no `getResultList()`) porque COUNT retorna un solo valor.

---

## Testing

### ¿Cómo ejecuto un test individual?

```bash
# Desde terminal
./gradlew test --tests HibernateUserServiceIntegrationTest.createUser_ValidDto_Success

# Desde IntelliJ
Click derecho en el test → Run
```

### ¿Por qué algunos tests fallan?

**Error común:** `EntityNotFoundException`
```
Unable to find User with id 999
```

**Causa:** Estás buscando un usuario que no existe.

**Solución:** Crea el usuario antes de buscarlo o usa IDs precargados (1-8).

**Error común:** `ConstraintViolationException`
```
Unique index or primary key violation: "email"
```

**Causa:** Email duplicado.

**Solución:** Usa emails únicos en cada test.

### ¿Cómo creo nuevos tests?

**Patrón AAA:**
```java
@Test
@Transactional
void miTest() {
    // ARRANGE: Preparar datos
    User user = createTestUser();
    
    // ACT: Ejecutar método
    boolean result = service.deleteUser(user.getId());
    
    // ASSERT: Verificar resultado
    assertTrue(result);
}
```

---

## Solución de Problemas

### "No qualifying bean of type 'HibernateUserService'"

**Causa:** Spring no encuentra el bean del servicio.

**Solución:** Verifica que `HibernateUserServiceImpl` tenga `@Service`:
```java
@Service  // ← Necesario
public class HibernateUserServiceImpl implements HibernateUserService {
    // ...
}
```

### "La transacción no se ha hecho commit"

**Causa:** Falta `@Transactional` en método de escritura.

**Solución:** Añade `@Transactional`:
```java
@Override
@Transactional  // ← Necesario para persist/merge/remove
public User createUser(UserCreateDto dto) {
    entityManager.persist(user);
    return user;
}
```

### "No puedo actualizar un usuario"

**Problema:** Los cambios no se guardan.

**Solución 1:** Usa `merge()` después de modificar:
```java
User user = findUserById(id);
user.setName("Nuevo nombre");
entityManager.merge(user);  // ← Necesario
```

**Solución 2:** O asegúrate de que el objeto esté "managed":
```java
@Transactional
public User updateUser(Long id) {
    User user = entityManager.find(User.class, id);  // managed
    user.setName("Nuevo");  // dirty checking detecta cambio
    // No necesitas merge() si está managed
    return user;
}
```

### "La consulta JPQL me da error de sintaxis"

**Error común:**
```
FROM users u  // ← INCORRECTO (tabla)
```

**Correcto:**
```
FROM User u  // ← CORRECTO (entidad)
```

**Recuerda:** JPQL usa nombres de entidades (Java), no tablas (SQL).

---

## Recursos

- [GUIA_ESTUDIANTE.md](GUIA_ESTUDIANTE.md) - Guía de aprendizaje completa
- [GUIA_INSTALACION.md](GUIA_INSTALACION.md) - Setup detallado
- [TESTING_GUIA.md](TESTING_GUIA.md) - Escribir y ejecutar tests
- [API_REFERENCIA.md](API_REFERENCIA.md) - Referencia de endpoints
- [Explicacion_Clase_Hibernate.md](../Explicacion_Clase_Hibernate.md) - Teoría ORM

¿No encuentras tu pregunta? Revisa los logs de la aplicación para más detalles del error.
