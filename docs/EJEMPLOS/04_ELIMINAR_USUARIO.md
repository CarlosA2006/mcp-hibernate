# Ejemplo 4: Eliminar Usuario con remove()

## Objetivo
Eliminar un usuario de la base de datos usando `EntityManager.remove()`.

## Código (TODO - Implementa tú)

```java
@Override
@Transactional
public boolean deleteUser(Long id) {
    // 1. Buscar usuario
    User user = entityManager.find(User.class, id);
    
    // 2. Verificar si existe
    if (user == null) {
        return false;
    }
    
    // 3. Eliminar con remove()
    entityManager.remove(user);
    
    // 4. Retornar true
    return true;
}
```

## SQL Generado

```sql
DELETE FROM users WHERE id = ?
```

## Conceptos Clave

- `remove()` requiere objeto "managed" (por eso usamos `find()` primero)
- @Transactional es obligatorio
- Si el usuario no existe, retornamos `false`

## Test

```java
@Test
@Transactional
void deleteUser_ExistingUser_Success() {
    User created = service.createUser(dto);
    boolean deleted = service.deleteUser(created.getId());
    
    assertTrue(deleted);
    assertNull(service.findUserById(created.getId()));
}
```

Ver [GUIA_ESTUDIANTE.md](../GUIA_ESTUDIANTE.md#51-todo-1-deleteuser---eliminar-con-remove).
