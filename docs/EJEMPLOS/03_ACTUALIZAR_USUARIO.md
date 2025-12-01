# Ejemplo 3: Actualizar Usuario con merge()

## Objetivo
Actualizar campos de un usuario existente usando `EntityManager.merge()`.

## Código

```java
@Override
@Transactional
public User updateUser(Long id, UserUpdateDto dto) {
    // 1. Buscar entidad existente
    User existing = findUserById(id);
    if (existing == null) {
        throw new RuntimeException("Usuario no encontrado");
    }

    // 2. Aplicar cambios
    if (dto.getName() != null) {
        existing.setName(dto.getName());
    }
    if (dto.getEmail() != null) {
        existing.setEmail(dto.getEmail());
    }
    existing.setUpdatedAt(LocalDateTime.now());

    // 3. Sincronizar con merge()
    return entityManager.merge(existing);
}
```

## SQL Generado (solo campos modificados)

```sql
UPDATE users
SET name = ?, updated_at = ?
WHERE id = ?
```

## Conceptos Clave

- **Dirty Checking**: Hibernate detecta qué campos cambiaron
- **UPDATE parcial**: Solo actualiza campos modificados
- `merge()` sincroniza cambios con BD

Ver [GUIA_ESTUDIANTE.md](../GUIA_ESTUDIANTE.md#44-método-4-updateuser---update-con-merge).
