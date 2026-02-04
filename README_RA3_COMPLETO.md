# RA3: Proyecto Completo de Acceso a Datos con Hibernate/ORM

Este documento es la **Guía Maestra** del proyecto. Explica la arquitectura general, la configuración y cómo se cumple con el Resultado de Aprendizaje 3 (RA3).

> **Para detalles específicos, consulta**:
> *   [Explicación Detallada de Métodos (Lógica)](docs/Explicacion_Metodos_RA3.md)
> *   [Explicación Detallada de Tests](docs/Explicacion_Tests_RA3.md)

---

## 1. Arquitectura y Tecnologías
El proyecto es un servidor backend construido con **Spring Boot 3** que utiliza **Hibernate** como proveedor de JPA (Java Persistence API).

*   **Spring Boot Starter Data JPA**: La dependencia mágica que trae Hibernate, Spring Data y el gestor de transacciones.
*   **H2 Database**: Base de datos en memoria (se borra al reiniciar) para pruebas rápidas sin instalar MySQL/PostgreSQL.
*   **MCP (Model Context Protocol)**: Capa que expone estas funciones a herramientas de IA.

---

## 2. Estructura de Archivos Clave

```text
mcp-hibernate/
├── build.gradle                 <- Dependencias (El "pom.xml" de Gradle)
├── src/main/resources/
│   └── application.yml          <- Configuración de BD y Hibernate
├── src/main/java/com/dam/accesodatos/
│   ├── model/
│   │   ├── User.java           <- La Entidad (Tabla 'users')
│   │   └── UserQueryDto.java   <- Objeto para pasar filtros de búsqueda
│   └── ra3/
│       └── HibernateUserServiceImpl.java <- ¡EL CEREBRO! Toda la lógica RA3
└── docs/                        <- Documentación adicional
```

---

## 3. Mapeo con Criterios de Evaluación (Checklist RA3)

Si el profesor pregunta "¿Dónde está...?", aquí está la respuesta:

*   **a) Instalar ORM**: Está en `build.gradle` (línea 37).
*   **b) Configurar ORM**: Está en `application.yml` (líneas 24-40).
*   **c) Ficheros de Mapeo**: Es la clase `User.java` con sus anotaciones.
*   **d) Mecanismos Persistencia**: Son los métodos `persist`, `remove` y `merge` en el Service.
*   **e) Modificar/Recuperar**: Es el CRUD completo (`create`, `find`, `update`, `delete`).
*   **f) Consultas (Lenguaje propio)**: Son los métodos que usan JPQL (`create native query` NO cuenta, tienen que ser los de JPQL).
*   **g) Transacciones**: Es el uso de `@Transactional` en la clase Service y método `transferData`.

---

## 4. Cómo probar que funciona

1.  **Ejecutar la App**: `./gradlew bootRun`
2.  **Ver Consola H2**: Entrar a `http://localhost:8083/h2-console`
3.  **Hacer peticiones HTTP**:
    *   Puedes usar las herramientas MCP o `curl`.
    *   Ejemplo crear usuario:
        ```bash
        curl -X POST http://localhost:8083/mcp/create_user -d '{"name":"Test","email":"test@test.com"}'
        ```
    *   Verás en la consola de Java el SQL que Hibernate "escribe" por ti.

Este archivo resume el alcance técnico del proyecto para el RA3.
