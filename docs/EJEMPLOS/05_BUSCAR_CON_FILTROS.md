# Ejemplo 5: Búsqueda Dinámica con JPQL

## Objetivo
Crear consultas JPQL dinámicas con filtros opcionales.

## Código (TODO - Implementa tú)

```java
@Override
public List<User> searchUsers(UserQueryDto queryDto) {
    // 1. Construir JPQL dinámicamente
    StringBuilder jpql = new StringBuilder("SELECT u FROM User u WHERE 1=1");
    
    // 2. Añadir condiciones según filtros presentes
    if (queryDto.getDepartment() != null) {
        jpql.append(" AND u.department = :dept");
    }
    if (queryDto.getRole() != null) {
        jpql.append(" AND u.role = :role");
    }
    if (queryDto.getActive() != null) {
        jpql.append(" AND u.active = :active");
    }
    
    // 3. Crear TypedQuery
    TypedQuery<User> query = entityManager.createQuery(jpql.toString(), User.class);
    
    // 4. Setear parámetros SOLO para filtros presentes
    if (queryDto.getDepartment() != null) {
        query.setParameter("dept", queryDto.getDepartment());
    }
    if (queryDto.getRole() != null) {
        query.setParameter("role", queryDto.getRole());
    }
    if (queryDto.getActive() != null) {
        query.setParameter("active", queryDto.getActive());
    }
    
    // 5. Ejecutar
    return query.getResultList();
}
```

## JPQL Generado (ejemplo con 2 filtros)

```java
SELECT u FROM User u WHERE 1=1 AND u.department = :dept AND u.active = :active
```

## Conceptos Clave

- **JPQL dinámico**: Construir query según filtros
- **Parámetros nombrados**: `:dept`, `:role`, `:active`
- **Null-checking**: Solo añadir condiciones si el filtro está presente

Ver [GUIA_ESTUDIANTE.md](../GUIA_ESTUDIANTE.md#52-todo-2-searchusers---jpql-dinámico).
