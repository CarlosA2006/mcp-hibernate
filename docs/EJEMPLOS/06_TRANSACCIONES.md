# Ejemplo 6: Transacciones con @Transactional

## Objetivo
Insertar múltiples usuarios en una transacción atómica (todo o nada).

## Código (TODO - Implementa tú)

```java
@Override
@Transactional
public boolean transferData(List<User> users) {
    for (User user : users) {
        entityManager.persist(user);
    }
    return true;
    // Spring hace COMMIT automáticamente si todo OK
    // Spring hace ROLLBACK automáticamente si hay excepción
}
```

## Comportamiento Transaccional

**Caso 1: Todos los persist() exitosos**
```
BEGIN TRANSACTION
  INSERT user 1
  INSERT user 2
  INSERT user 3
COMMIT  ← Spring automático
```

**Caso 2: Uno falla (email duplicado)**
```
BEGIN TRANSACTION
  INSERT user 1  ← OK
  INSERT user 2  ← FALLA (email duplicado)
ROLLBACK  ← Spring automático (ninguno se inserta)
```

## Comparación RA2 vs RA3

### RA2 (JDBC Manual)

```java
conn.setAutoCommit(false);
try {
    for (User user : users) {
        ps.setString(1, user.getName());
        // ...
        ps.executeUpdate();
    }
    conn.commit();  // ← Manual
} catch (Exception e) {
    conn.rollback();  // ← Manual
}
```

### RA3 (Spring Automático)

```java
@Transactional
public boolean transferData(List<User> users) {
    for (User user : users) {
        entityManager.persist(user);
    }
    return true;
    // Spring maneja commit/rollback automáticamente
}
```

## Conceptos Clave

- **Atomicidad**: O se insertan todos o ninguno
- **@Transactional**: Spring maneja BEGIN, COMMIT, ROLLBACK
- **Rollback automático**: Si hay excepción, Spring revierte todos los cambios

Ver [GUIA_ESTUDIANTE.md](../GUIA_ESTUDIANTE.md#53-todo-3-transferdata---transacción-múltiple).
