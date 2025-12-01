# mcp-hibernate

> **Servidor MCP con Hibernate/JPA para RA3 - Acceso a Datos mediante ORM**

Proyecto educativo de Spring Boot que demuestra el uso de Hibernate/JPA como ORM (Object-Relational Mapping) para estudiantes de DAM (Desarrollo de Aplicaciones Multiplataforma). Este proyecto implementa un servidor MCP (Model Context Protocol) que expone operaciones de persistencia de datos utilizando Hibernate.

## Tabla de Contenidos

- [Características Principales](#características-principales)
- [Requisitos Previos](#requisitos-previos)
- [Instalación Rápida](#instalación-rápida)
- [Estructura del Proyecto](#estructura-del-proyecto)
- [Cómo Ejecutar](#cómo-ejecutar)
- [Herramientas MCP Disponibles](#herramientas-mcp-disponibles)
- [Stack Tecnológico](#stack-tecnológico)
- [Metodología de Aprendizaje](#metodología-de-aprendizaje)
- [Documentación Complementaria](#documentación-complementaria)
- [Próximos Pasos](#próximos-pasos)

## Características Principales

- **Servidor MCP** que expone operaciones Hibernate/JPA como herramientas para LLMs
- **Hibernate/JPA** como implementación ORM para mapeo objeto-relacional
- **Spring Boot 3.3.0** con Java 21
- **Base de datos H2** en memoria (modo PostgreSQL)
- **6 métodos implementados** como ejemplos de estudio
- **4 métodos TODO** para que los estudiantes implementen
- **Suite de tests completa** con JUnit 5 y Spring Boot Test
- **Documentación pedagógica** en español

## Requisitos Previos

Antes de comenzar, asegúrate de tener instalado:

- **Java 21** o superior ([Descargar OpenJDK](https://adoptium.net/))
- **Gradle 8.x** (wrapper incluido en el proyecto)
- **Git** para clonar el repositorio
- **Editor/IDE**: IntelliJ IDEA, VS Code, o Eclipse
- *Opcional*: Postman o curl para probar endpoints

## Instalación Rápida

```bash
# 1. Clonar el repositorio
git clone https://github.com/balejosg/mcp-hibernate.git
cd mcp-hibernate

# 2. Verificar versión de Java
java -version  # Debe ser Java 21+

# 3. Compilar el proyecto
./gradlew clean build

# 4. Ejecutar la aplicación
./gradlew bootRun

# 5. Verificar que el servidor está corriendo
curl http://localhost:8083/mcp/health
```

Para instrucciones detalladas de instalación y configuración de IDEs, consulta [docs/GUIA_INSTALACION.md](docs/GUIA_INSTALACION.md).

## Estructura del Proyecto

```
mcp-hibernate/
├── src/main/java/com/dam/accesodatos/
│   ├── McpAccesoDatosRa3Application.java  # Aplicación principal
│   ├── model/                              # Entidades JPA y DTOs
│   │   ├── User.java                       # Entidad @Entity mapeada a tabla users
│   │   ├── UserCreateDto.java              # DTO para crear usuarios
│   │   ├── UserUpdateDto.java              # DTO para actualizar usuarios
│   │   └── UserQueryDto.java               # DTO para búsquedas con filtros
│   ├── ra3/                                # Servicios Hibernate/JPA (RA3)
│   │   ├── HibernateUserService.java       # Interface con @Tool annotations
│   │   └── HibernateUserServiceImpl.java   # Implementación (6 + 4 TODO)
│   ├── repository/                         # Repositorios Spring Data JPA
│   │   └── UserRepository.java             # JpaRepository<User, Long>
│   └── mcp/                                # Componentes MCP Server
│       ├── McpServerController.java        # Endpoints REST/MCP
│       └── McpToolRegistry.java            # Registro de herramientas MCP
├── src/main/resources/
│   ├── application.yml                     # Configuración Spring Boot
│   ├── schema.sql                          # Esquema de base de datos
│   └── data.sql                            # Datos de prueba
├── src/test/java/                          # Tests unitarios e integración
├── docs/                                   # Documentación del proyecto
│   ├── GUIA_ESTUDIANTE.md                  # ⭐ Guía principal para estudiantes
│   ├── GUIA_INSTALACION.md                 # Setup detallado
│   ├── API_REFERENCIA.md                   # Referencia técnica de endpoints
│   ├── CRITERIOS_RA3_DETALLADO.md          # Mapeo código ↔ criterios RA3
│   ├── GUIA_HERRAMIENTAS_MCP.md            # Uso de herramientas MCP
│   ├── TESTING_GUIA.md                     # Guía de testing
│   ├── ARQUITECTURA.md                     # Diseño y patrones
│   ├── PREGUNTAS_FRECUENTES.md             # FAQ
│   └── EJEMPLOS/                           # Ejemplos de código detallados
├── Explicacion_Clase_Hibernate.md          # Guía didáctica ORM y Hibernate
├── Criterios.md                            # Criterios de evaluación RA3
├── Recomendacion_Minimos.md                # Propuesta de ejercicio mínimo
└── build.gradle                            # Configuración Gradle
```

## Cómo Ejecutar

### Ejecutar la Aplicación

```bash
# Usando Gradle Wrapper
./gradlew bootRun

# Compilar JAR y ejecutar
./gradlew clean build
java -jar build/libs/mcp-acceso-datos-ra3-1.0.0.jar
```

La aplicación se iniciará en **http://localhost:8083**

### Acceder a H2 Console

Una vez iniciada la aplicación, puedes acceder a la consola de la base de datos H2:

- **URL**: http://localhost:8083/h2-console
- **JDBC URL**: `jdbc:h2:mem:ra3db`
- **Username**: `sa`
- **Password**: *(dejar en blanco)*

### Verificar Endpoints MCP

```bash
# Health check
curl http://localhost:8083/mcp/health

# Listar herramientas MCP disponibles
curl http://localhost:8083/mcp/tools

# Probar EntityManager
curl -X POST http://localhost:8083/mcp/test_entity_manager

# Crear un usuario
curl -X POST http://localhost:8083/mcp/create_user \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Ana López",
    "email": "ana.lopez@test.com",
    "department": "IT",
    "role": "Developer",
    "active": true
  }'
```

### Ejecutar Tests

```bash
# Ejecutar todos los tests
./gradlew test

# Ejecutar tests con logs detallados
./gradlew test --info
```

## Herramientas MCP Disponibles

El servidor MCP expone 10 herramientas que demuestran operaciones Hibernate/JPA:

| Herramienta | Endpoint | Estado | Descripción |
|-------------|----------|--------|-------------|
| `test_entity_manager` | `/mcp/test_entity_manager` | ✅ Implementado | Verifica conexión EntityManager |
| `create_user` | `/mcp/create_user` | ✅ Implementado | Crea usuario con `persist()` |
| `find_user_by_id` | `/mcp/find_user_by_id` | ✅ Implementado | Busca usuario por ID con `find()` |
| `update_user` | `/mcp/update_user` | ✅ Implementado | Actualiza usuario con `merge()` |
| `delete_user` | `/mcp/delete_user` | ⚠️ TODO | Elimina usuario con `remove()` |
| `find_all_users` | `/mcp/find_all_users` | ✅ Implementado | Obtiene todos los usuarios |
| `find_users_by_department` | `/mcp/find_users_by_department` | ✅ Implementado | Busca por departamento con JPQL |
| `search_users` | `/mcp/search_users` | ⚠️ TODO | Búsqueda dinámica con JPQL |
| `transfer_data` | `/mcp/transfer_data` | ⚠️ TODO | Inserta múltiples usuarios en transacción |
| `execute_count_by_department` | `/mcp/execute_count_by_department` | ⚠️ TODO | Ejecuta COUNT con JPQL |

**Leyenda:**
- ✅ **Implementado**: Método completo con código de ejemplo para estudiar
- ⚠️ **TODO**: Método para que el estudiante implemente

Para más detalles sobre cada herramienta, consulta [docs/GUIA_HERRAMIENTAS_MCP.md](docs/GUIA_HERRAMIENTAS_MCP.md).

## Stack Tecnológico

| Componente | Versión | Propósito |
|------------|---------|-----------|
| **Java** | 21 (LTS) | Lenguaje de programación |
| **Spring Boot** | 3.3.0 | Framework de aplicación |
| **Spring Data JPA** | 3.3.0 | Abstracción sobre JPA/Hibernate |
| **Hibernate** | 6.x | Implementación ORM (JPA provider) |
| **Jakarta Persistence API** | 3.1 | Especificación JPA |
| **H2 Database** | Latest | Base de datos en memoria |
| **JUnit 5 (Jupiter)** | 5.10.x | Framework de testing |
| **Gradle** | 8.6 | Herramienta de construcción |

### Dependencia Clave (RA3)

La diferencia fundamental entre RA2 (JDBC) y RA3 (ORM) está en esta dependencia:

```gradle
// RA2 (JDBC - proyecto anterior)
implementation 'org.springframework.boot:spring-boot-starter-jdbc'

// RA3 (ORM - este proyecto) ⭐
implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
```

## Metodología de Aprendizaje

Este proyecto sigue un enfoque pedagógico de **aprender por ejemplo**:

### 1. Estudiar Métodos Implementados (6 ejemplos)

Los siguientes métodos están completamente implementados con comentarios pedagógicos:

1. **`testEntityManager()`** - Verificar conexión EntityManager
2. **`createUser()`** - INSERT con `persist()`
3. **`findUserById()`** - SELECT por ID con `find()`
4. **`updateUser()`** - UPDATE con `merge()`
5. **`findAll()`** - SELECT todos con Repository
6. **`findUsersByDepartment()`** - JPQL básico

### 2. Implementar Métodos TODO (4 ejercicios)

Los estudiantes deben implementar estos métodos aplicando lo aprendido:

1. **`deleteUser()`** - Eliminar con `remove()`
2. **`searchUsers()`** - JPQL dinámico con filtros
3. **`transferData()`** - Transacciones con `@Transactional`
4. **`executeCountByDepartment()`** - Consulta COUNT en JPQL

### 3. Validar con Tests

Cada método tiene tests asociados en:
- `HibernateUserServiceImplTest.java` (tests unitarios)
- `HibernateUserServiceIntegrationTest.java` (tests de integración)

### Flujo de Trabajo Recomendado

```
1. Lee GUIA_ESTUDIANTE.md
   ↓
2. Estudia un método implementado
   ↓
3. Ejecuta su test correspondiente
   ↓
4. Observa el SQL generado en logs
   ↓
5. Implementa el método TODO relacionado
   ↓
6. Ejecuta el test para validar
   ↓
7. Repite con el siguiente método
```

## Documentación Complementaria

### Guías Principales

- **[docs/GUIA_ESTUDIANTE.md](docs/GUIA_ESTUDIANTE.md)** ⭐ - **EMPIEZA AQUÍ**: Guía completa de aprendizaje paso a paso
- **[docs/GUIA_INSTALACION.md](docs/GUIA_INSTALACION.md)** - Instalación detallada y configuración de IDEs
- **[docs/TESTING_GUIA.md](docs/TESTING_GUIA.md)** - Cómo escribir y ejecutar tests

### Documentación de Referencia

- **[docs/API_REFERENCIA.md](docs/API_REFERENCIA.md)** - Especificación técnica de endpoints MCP
- **[docs/CRITERIOS_RA3_DETALLADO.md](docs/CRITERIOS_RA3_DETALLADO.md)** - Mapeo de código a criterios de evaluación RA3
- **[docs/GUIA_HERRAMIENTAS_MCP.md](docs/GUIA_HERRAMIENTAS_MCP.md)** - Uso práctico de herramientas MCP
- **[docs/ARQUITECTURA.md](docs/ARQUITECTURA.md)** - Diseño del sistema y patrones

### Recursos Pedagógicos

- **[Explicacion_Clase_Hibernate.md](Explicacion_Clase_Hibernate.md)** - Guía didáctica sobre ORM y Hibernate
- **[Criterios.md](Criterios.md)** - Criterios de evaluación oficiales RA3
- **[Recomendacion_Minimos.md](Recomendacion_Minimos.md)** - Propuesta de ejercicio mínimo
- **[docs/PREGUNTAS_FRECUENTES.md](docs/PREGUNTAS_FRECUENTES.md)** - FAQ y solución de problemas
- **[docs/EJEMPLOS/](docs/EJEMPLOS/)** - Ejemplos de código detallados

## Próximos Pasos

Para comenzar tu aprendizaje con este proyecto:

1. **Configura tu entorno**: Sigue [docs/GUIA_INSTALACION.md](docs/GUIA_INSTALACION.md)
2. **Lee la guía del estudiante**: [docs/GUIA_ESTUDIANTE.md](docs/GUIA_ESTUDIANTE.md)
3. **Ejecuta los tests**: `./gradlew test` y observa los resultados
4. **Estudia los ejemplos**: Lee el código de los 6 métodos implementados
5. **Implementa los TODOs**: Completa los 4 métodos pendientes
6. **Valida tu trabajo**: Ejecuta los tests correspondientes

## Diferencias RA2 (JDBC) vs RA3 (Hibernate/JPA)

| Aspecto | RA2 (JDBC) | RA3 (Hibernate/JPA) |
|---------|------------|---------------------|
| **Lenguaje** | SQL strings | JPQL (orientado a objetos) |
| **Configuración** | Connection, Statement | EntityManager, Repository |
| **INSERT** | PreparedStatement + getGeneratedKeys() | `persist(objeto)` |
| **SELECT** | ResultSet manual mapping | `find(id)` automático |
| **UPDATE** | PreparedStatement UPDATE | `merge(objeto)` + dirty checking |
| **DELETE** | PreparedStatement DELETE | `remove(objeto)` |
| **Transacciones** | commit()/rollback() manual | `@Transactional` automático |

## Licencia y Contribuciones

Este es un proyecto educativo para estudiantes de DAM (Desarrollo de Aplicaciones Multiplataforma) en el módulo de Acceso a Datos, específicamente para el Resultado de Aprendizaje 3 (RA3) sobre ORM.

---

**Proyecto:** mcp-hibernate
**Versión:** 1.0.0
**Autor:** Proyecto educativo RA3 - Acceso a Datos
**Repositorio:** https://github.com/balejosg/mcp-hibernate
