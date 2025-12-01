# Guía Didáctica: Introducción a Hibernate y Spring Data JPA
**Proyecto:** mcp-hibernate (RA3 - Acceso a Datos)

Este documento sirve como guía para explicar la transición del acceso a datos manual (JDBC) al acceso a datos automatizado (ORM) utilizando Spring Boot.

---

## 1. El Concepto Fundamental: ¿Qué es un ORM?

Antes de ver código, los alumnos deben entender el problema que resolvemos.

*   **El Problema (JDBC - RA2):** Las bases de datos hablan **SQL** (Tablas, Filas, Columnas). Java habla **Objetos** (Clases, Instancias, Atributos). Son dos idiomas diferentes. Antes, nosotros éramos los traductores manuales (escribiendo SQL Strings y mapeando `ResultSet` a mano).
*   **La Solución (Hibernate - RA3):** Un **ORM** (Object-Relational Mapping) es un traductor automático.
    *   Nosotros manipulamos **Objetos Java**.
    *   Hibernate traduce esos cambios a **SQL** automáticamente.

---

## 2. Diferencia entre JPA y Hibernate

Es vital distinguir entre el "Reglamento" y el "Motor".

| Concepto | Nombre Técnico | Analogía |
| :--- | :--- | :--- |
| **JPA** (Jakarta Persistence API) | Especificación (Estándar) | Es el **Reglamento de Tráfico**. Define las señales (`@Entity`, `@Id`) y las normas. No hace nada por sí solo. |
| **Hibernate** | Implementación (Librería) | Es el **Coche**. Es el software real que lee el reglamento y conduce (ejecuta las queries). |

> **Nota Pedagógica:** En `User.java` importamos `jakarta.persistence.*`. Esto significa que usamos el estándar. Si mañana cambiamos Hibernate por otro motor (EclipseLink), ¡no tendríamos que cambiar el código de la clase!

---

## 3. La Entidad (`User.java`): El Mapa

El archivo `User.java` es donde configuramos el diccionario de traducción.

### Las Anotaciones Clave
*   **`@Entity`:** "Spring, esta clase no es normal. Representa una tabla en la BD".
*   **`@Table(name = "users")`:** "La clase se llama `User` (singular), pero guárdala en la tabla `users` (plural)".
*   **`@Id` + `@GeneratedValue`:** "Olvídate de calcular IDs (Max+1). La base de datos se encarga de asignar el número (Autoincrement/Identity)".
*   **`@Column`:** Define reglas de la columna (longitud, si puede ser nulo, nombre específico).

### El "Constructor Vacío"
¿Por qué hay un `public User() {}` sin código?
Hibernate usa **Reflexión**. Instancia objetos vacíos primero y luego rellena los datos usando los setters o acceso directo a campos. Sin este constructor, Hibernate falla al intentar leer de la BD.

---

## 4. El Repositorio (`UserRepository.java`): La Magia

Aquí es donde Spring Data JPA brilla. No es una clase, es una **Interfaz**.

### Query Derivation (Derivación de Consultas)
Spring es capaz de "leer" el nombre del método y crear la SQL en tiempo de ejecución. Funciona como un análisis gramatical:

**Método:** `findByDepartmentAndActive(String dept, Boolean active)`

1.  **`find...`**: Spring sabe que es un `SELECT`.
2.  **`...By...`**: Empieza la cláusula `WHERE`.
3.  **`Department`**: Busca atributo `department` en la clase `User` -> `department = ?`
4.  **`And`**: Operador lógico -> `AND`
5.  **`Active`**: Busca atributo `active` en la clase `User` -> `active = ?`

**SQL Resultante (automático):**
```sql
SELECT * FROM users WHERE department = ? AND active = ?
```

### JPQL (Java Persistence Query Language)
Cuando el nombre del método es muy largo o complejo, usamos `@Query`.
*   **SQL:** `SELECT * FROM users u ...` (Habla de tablas).
*   **JPQL:** `SELECT u FROM User u ...` (Habla de Clases/Entidades).
*   **Ventaja:** Si cambia el nombre de la tabla en la BD, JPQL sigue funcionando porque se basa en la Clase Java.

---

## 5. El Servicio (`HibernateUserServiceImpl.java`): Las Operaciones

Comparativa directa con RA2 (JDBC).

### A. Insertar (Create)
*   **JDBC:** `INSERT INTO users (name, email...) VALUES (?, ?...)`.
*   **Hibernate:**
    1.  Creas el objeto: `User u = new User(...);`
    2.  Guardas: `entityManager.persist(u);`
    *   *¡No hay SQL! Hibernate detecta el objeto nuevo y hace el INSERT.*

### B. Leer (Read)
*   **JDBC:** `ResultSet rs = st.executeQuery(...)` y bucle `while(rs.next())`.
*   **Hibernate:** `User u = entityManager.find(User.class, 1L);`
    *   Te devuelve el objeto ya montado.

### C. Actualizar (Update)
*   **JDBC:** `UPDATE users SET name = ? WHERE id = ?`.
*   **Hibernate:**
    1.  Recuperas el objeto: `User u = find(id);`
    2.  Modificas el objeto: `u.setName("Nuevo Nombre");`
    3.  Sincronizas: `entityManager.merge(u);`
    *   Hibernate compara el objeto en memoria con la copia original, detecta qué cambió y hace el UPDATE solo de los campos necesarios.

### D. Eliminar (Delete)
*   **JDBC:** `DELETE FROM users WHERE id = ?`.
*   **Hibernate:**
    1.  Buscas el objeto primero (debe estar "manageado").
    2.  Eliminas: `entityManager.remove(u);`

---

## 6. Concepto Avanzado: El Contexto de Persistencia y `@Transactional`

### La Transacción (`@Transactional`)
En JDBC (RA2) hacíamos `conn.setAutoCommit(false)` y luego `commit()` o `rollback()`.
En Spring, ponemos `@Transactional` encima del método.
*   Si el método termina bien -> **Commit automático**.
*   Si el método lanza una excepción (error) -> **Rollback automático**.

### Ciclo de Vida (Estado de los Objetos)
Hibernate no solo "guarda datos", gestiona estados de objetos:
1.  **Transient (Nuevo):** Acabas de hacer `new User()`. Hibernate no lo conoce.
2.  **Managed (Gestionado):** Lo acabas de guardar (`persist`) o leer (`find`). Hibernate lo está "vigilando".
    *   *Efecto Mágico:* Si cambias un set en un objeto *Managed* dentro de una transacción, ¡Hibernate hará el UPDATE automáticamente al final sin que llames a `save()`! (Dirty Checking).
3.  **Detached (Desacoplado):** La transacción terminó. El objeto existe en Java, pero Hibernate ya no vigila cambios.
4.  **Removed (Eliminado):** Marcado para borrar.

---

## 7. Resumen para el Alumno

| Acción | JDBC (Lo viejo) | Hibernate/JPA (Lo nuevo) |
| :--- | :--- | :--- |
| **Lenguaje** | SQL Strings | Métodos Java y JPQL |
| **Configuración** | `Connection`, `Statement` | `EntityManager`, `Repository` |
| **Insertar** | Escribir query manual | `persist(objeto)` |
| **Consultar** | Mapear `ResultSet` a mano | `find(id)` o `findBy...` |
| **Transacción** | `commit()` / `rollback()` manual | `@Transactional` |
| **Errores** | SQL Syntax Error | EntityNotFound, ConstraintViolation |
