# Ejemplo 2: Buscar Usuario con find()

## Objetivo
Recuperar un usuario por su ID usando `EntityManager.find()`.

## Código

```java
@Override
public User findUserById(Long id) {
    return entityManager.find(User.class, id);
}
```

## SQL Generado

```sql
SELECT id, name, email, department, role, active, created_at, updated_at
FROM users
WHERE id = ?
```

## Conceptos Clave

- `find(User.class, id)`: Busca por clave primaria
- Retorna `null` si no existe (no lanza excepción)
- Mapeo automático de columnas a atributos
- Objeto retornado está en estado "managed"

## Comparación RA2 vs RA3

**RA2:** 15 líneas (SQL manual + mapeo ResultSet)

**RA3:** 1 línea (automático)

Ver [GUIA_ESTUDIANTE.md](../GUIA_ESTUDIANTE.md#43-método-3-finduserbyid---select-por-id).
