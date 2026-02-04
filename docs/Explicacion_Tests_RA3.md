# Explicación Detallada: Tests del Proyecto (RA3)

Este documento analiza la estrategia de pruebas del proyecto, explicando la diferencia entre los tests unitarios y de integración, y detallando qué prueba cada clase.

---

## 1. Tests Unitarios (`HibernateUserServiceImplTest.java`)

**Objetivo**: Probar la lógica de negocio **AISLADA** de la base de datos.
**Herramientas**: JUnit 5, Mockito.

En estos tests, **NO hay base de datos real**. Simulamos el comportamiento de Hibernate (Mocks) para asegurarnos de que nuestro código Java hace lo que debe.

### ¿Cómo funcionan los Mocks?
Usamos `@Mock` para crear objetos falsos de `EntityManager`:
```java
@Mock
private EntityManager entityManager;
```
Y luego "entrenamos" a estos mocks:
*"Cuando alguien llame a `entityManager.find(1)`, devuelve este usuario de mentira"*.

### Análisis de Tests Clave

*   **`searchUsers_WithFilters`**:
    *   **Qué prueba**: Que la concatenación de Strings en el JPQL dinámico es correcta.
    *   **Verificación**: `verify(entityManager).createQuery(contains("AND u.department = :dept")...)`
    *   Asegura que si le paso un departamento, mi código añade el `AND` correspondiente a la query.

*   **`createUser_Success`**:
    *   **Qué prueba**: Que llamamos a `persist()`.
    *   **Verificación**: `verify(entityManager).persist(any(User.class))`
    *   No guarda nada de verdad, solo verifica que "se intentó guardar".

*   **`deleteUser_NotFound`**:
    *   **Qué prueba**: La lógica defensiva.
    *   Configuramos el mock para devolver `null` al buscar.
    *   Verificamos que **no** se llama a `remove()`.

---

## 2. Tests de Integración (`HibernateUserServiceIntegrationTest.java`)

**Objetivo**: Probar el sistema completo **CON** una base de datos real (H2 en memoria).
**Herramientas**: `@SpringBootTest`, H2 Database.

Aquí **SÍ hay base de datos**. Hibernate arranca de verdad, crea las tablas, e inserta y consulta filas reales.

### ¿Cómo funciona el entorno?
*   `@SpringBootTest`: Levanta todo el servidor Spring (como si hicieras `bootRun`).
*   `@Transactional` en el test: Un truco genial. Cada test inicia una transacción, hace sus inserts/updates, y al terminar hace **Rollback automático**. Así la base de datos queda limpia para el siguiente test.

### Análisis de Tests Clave

*   **`crudFlow_CompleteLifecycle_Success`**:
    *   **Qué prueba**: El ciclo de vida completo.
    *   1. Crea un usuario (INSERT real).
    *   2. Lo busca (SELECT real).
    *   3. Lo modifica (UPDATE real).
    *   4. Verifica que los datos modificados están ahí.
    *   Si algo falla en el mapeo ORM (`User.java`), este test explota.

*   **`deleteUser_RealDB_Success`**:
    *   **Qué prueba**: Que el `remove` realmente borra.
    *   Crea un usuario -> Lo borra -> Intenta buscarlo y afirma que es `null`.
    *   Confirma que el SQL `DELETE` se ejecutó correctamente.

*   **`transferData_RealDB_Success`**:
    *   **Qué prueba**: Que la inserción masiva funciona.
    *   Pasa una lista de usuarios y verifica con `COUNT` que todos están en la base de datos.

---

## Comparativa Resumen

| Característica | Test Unitario (`ImplTest`) | Test Integración (`IntegrationTest`) |
| :--- | :--- | :--- |
| **Velocidad** | Ultrarrápido (ms) | Lento (segundos, debe arrancar Spring) |
| **Base de Datos** | Falsa (Mock) | Real (H2 en memoria) |
| **Foco** | Lógica Java (if/else, Strings) | Configuración SQL, Mapeos, Queries reales |
| **Anotación** | `@ExtendWith(MockitoExtension.class)` | `@SpringBootTest` |

Para el RA3, los **Tests de Integración** son los más valiosos porque demuestran que tu configuración de Hibernate y tus consultas SQL/JPQL funcionan contra una base de datos de verdad.
