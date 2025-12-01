# Guía de Instalación y Configuración

Esta guía te ayudará a configurar el proyecto `mcp-hibernate` paso a paso, desde la verificación de requisitos hasta la primera ejecución exitosa.

## Tabla de Contenidos

1. [Verificación de Requisitos Previos](#1-verificación-de-requisitos-previos)
2. [Descarga del Proyecto](#2-descarga-del-proyecto)
3. [Importación en IDEs](#3-importación-en-ides)
4. [Configuración Gradle](#4-configuración-gradle)
5. [Primera Ejecución](#5-primera-ejecución)
6. [Acceso a H2 Console](#6-acceso-a-h2-console)
7. [Verificación de Endpoints MCP](#7-verificación-de-endpoints-mcp)
8. [Solución de Problemas](#8-solución-de-problemas)

---

## 1. Verificación de Requisitos Previos

### 1.1. Java 21

El proyecto requiere **Java 21 (LTS)** o superior.

```bash
# Verificar versión de Java instalada
java -version

# Salida esperada (ejemplo):
# openjdk version "21.0.1" 2023-10-17 LTS
# OpenJDK Runtime Environment (build 21.0.1+12-LTS)
```

**Si no tienes Java 21:**
- Descarga **Eclipse Temurin (AdoptOpenJDK)**: https://adoptium.net/
- Selecciona versión 21 (LTS)
- Instala y configura `JAVA_HOME`

**Configurar JAVA_HOME** (si es necesario):

**Linux/Mac:**
```bash
export JAVA_HOME=/ruta/a/java21
export PATH=$JAVA_HOME/bin:$PATH
```

**Windows:**
```cmd
setx JAVA_HOME "C:\Program Files\Java\jdk-21"
setx PATH "%JAVA_HOME%\bin;%PATH%"
```

### 1.2. Gradle

El proyecto incluye **Gradle Wrapper** (`./gradlew`), por lo que NO necesitas instalar Gradle manualmente.

```bash
# Verificar que el wrapper funciona
./gradlew --version

# Salida esperada:
# Gradle 8.6
# Kotlin: ...
# Groovy: ...
# JVM: 21.0.1 (Eclipse Adoptium)
```

### 1.3. Git

```bash
# Verificar Git
git --version

# Salida esperada:
# git version 2.40.0 (o superior)
```

Si no tienes Git, descárgalo de: https://git-scm.com/downloads

### 1.4. Editor/IDE (opcional pero recomendado)

- **IntelliJ IDEA Community Edition** (recomendado): https://www.jetbrains.com/idea/download/
- **VS Code** con extensiones Java: https://code.visualstudio.com/
- **Eclipse IDE for Java Developers**: https://www.eclipse.org/downloads/

---

## 2. Descarga del Proyecto

### 2.1. Clonar desde GitHub

```bash
# Navegar al directorio donde quieres el proyecto
cd ~/proyectos  # O la carpeta que prefieras

# Clonar el repositorio
git clone https://github.com/balejosg/mcp-hibernate.git

# Entrar al directorio
cd mcp-hibernate
```

### 2.2. Verificar Estructura del Proyecto

```bash
# Listar archivos principales
ls -la

# Deberías ver:
# - build.gradle
# - gradlew y gradlew.bat
# - src/
# - README.md
# - docs/
```

---

## 3. Importación en IDEs

### 3.1. IntelliJ IDEA (Recomendado)

1. **Abrir IntelliJ IDEA**
2. **Importar Proyecto**:
   - Menú: `File` → `Open...`
   - Selecciona la carpeta `mcp-hibernate`
   - IntelliJ detectará automáticamente que es un proyecto Gradle

3. **Esperar Importación**:
   - IntelliJ descargará dependencias automáticamente
   - Aparecerá una barra de progreso en la esquina inferior derecha
   - Puede tardar 2-5 minutos la primera vez

4. **Configurar SDK del Proyecto**:
   - Menú: `File` → `Project Structure` (`Ctrl+Alt+Shift+S`)
   - En **Project Settings → Project**:
     - **SDK**: Selecciona Java 21
     - **Language level**: 21 - Record patterns, pattern matching for switch

5. **Verificar Gradle**:
   - Panel derecho: Click en **Gradle** (icono de elefante)
   - Debería aparecer `mcp-hibernate` con tareas como `build`, `bootRun`, `test`

6. **Ejecutar desde IntelliJ**:
   - Navega a `src/main/java/com/dam/accesodatos/McpAccesoDatosRa3Application.java`
   - Click derecho → `Run 'McpAccesoDatosRa3Application'`

### 3.2. VS Code

1. **Instalar Extensiones** (si no las tienes):
   - **Extension Pack for Java** (Microsoft)
   - **Spring Boot Extension Pack** (VMware)
   - **Gradle for Java** (Microsoft)

2. **Abrir Proyecto**:
   - Menú: `File` → `Open Folder...`
   - Selecciona `mcp-hibernate/`

3. **Configurar Java**:
   - Presiona `Ctrl+Shift+P` (Cmd+Shift+P en Mac)
   - Escribe: `Java: Configure Java Runtime`
   - Asegúrate de que Java 21 esté seleccionado

4. **Ejecutar**:
   - Terminal integrada: `Ctrl+`ñ (backtick)
   - Ejecutar: `./gradlew bootRun`

### 3.3. Eclipse

1. **Abrir Eclipse**
2. **Importar Proyecto Gradle**:
   - Menú: `File` → `Import...`
   - Selecciona: `Gradle` → `Existing Gradle Project`
   - Click `Next`

3. **Seleccionar Proyecto**:
   - **Project root directory**: Navega a `mcp-hibernate/`
   - Click `Finish`

4. **Configurar JRE**:
   - Click derecho en el proyecto → `Properties`
   - `Java Build Path` → `Libraries`
   - Asegúrate de que JRE System Library sea Java 21

5. **Ejecutar**:
   - Click derecho en `McpAccesoDatosRa3Application.java`
   - `Run As` → `Java Application`

---

## 4. Configuración Gradle

### 4.1. Entender `build.gradle`

El archivo `build.gradle` define todas las dependencias del proyecto:

```gradle
dependencies {
    // Spring Boot Core
    implementation 'org.springframework.boot:spring-boot-starter'
    implementation 'org.springframework.boot:spring-boot-starter-web'

    // ⭐ CLAVE RA3: Hibernate/JPA
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'

    // Base de datos H2
    runtimeOnly 'com.h2database:h2'

    // Validación de entidades
    implementation 'org.springframework.boot:spring-boot-starter-validation'

    // Testing
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}
```

### 4.2. Descargar Dependencias Manualmente

Si las dependencias no se descargaron automáticamente:

```bash
# Forzar descarga de dependencias
./gradlew build --refresh-dependencies

# Ver árbol de dependencias (opcional)
./gradlew dependencies
```

### 4.3. Limpiar Caché de Gradle (si hay problemas)

```bash
# Limpiar build
./gradlew clean

# Limpiar caché de Gradle
rm -rf ~/.gradle/caches/  # Linux/Mac
# En Windows: del /s /q %USERPROFILE%\.gradle\caches
```

---

## 5. Primera Ejecución

### 5.1. Compilar el Proyecto

```bash
# Limpiar y compilar
./gradlew clean build

# Salida esperada al final:
# BUILD SUCCESSFUL in 15s
# 8 actionable tasks: 8 executed
```

**Posibles Salidas:**
- **BUILD SUCCESSFUL**: Todo bien, continúa
- **BUILD FAILED**: Revisa errores en la sección [Solución de Problemas](#8-solución-de-problemas)

### 5.2. Ejecutar la Aplicación

```bash
# Método 1: Usando Gradle
./gradlew bootRun

# Método 2: Ejecutar el JAR compilado
java -jar build/libs/mcp-acceso-datos-ra3-1.0.0.jar
```

### 5.3. Verificar Inicio Exitoso

**Logs Esperados:**

```
Iniciando MCP Server RA3 - Hibernate/JPA
...
Hibernate:
    drop table if exists users cascade
Hibernate:
    create table users (...)
...
Started McpAccesoDatosRa3Application in 3.245 seconds (process running for 3.567)
===================================================
  MCP Server RA3 - Hibernate/JPA INICIADO
===================================================
  H2 Console: http://localhost:8083/h2-console
  Endpoints MCP: http://localhost:8083/mcp
===================================================
```

### 5.4. Señales de Inicio Exitoso

- ✅ No aparece error `Port 8083 already in use`
- ✅ Ves logs de Hibernate creando tablas
- ✅ Mensaje final: `Started McpAccesoDatosRa3Application`
- ✅ Puerto 8083 escuchando

---

## 6. Acceso a H2 Console

H2 Console es una interfaz web para consultar la base de datos directamente.

### 6.1. Abrir H2 Console

**URL:** http://localhost:8083/h2-console

### 6.2. Configurar Conexión

En la pantalla de login de H2:

| Campo | Valor |
|-------|-------|
| **Saved Settings** | Generic H2 (Embedded) |
| **Driver Class** | org.h2.Driver |
| **JDBC URL** | `jdbc:h2:mem:ra3db` |
| **User Name** | `sa` |
| **Password** | *(dejar vacío)* |

Click en **Connect**.

### 6.3. Verificar Datos

En el panel izquierdo, expande la base de datos y verás la tabla `USERS`.

**Ejecuta esta consulta:**

```sql
SELECT * FROM users;
```

Deberías ver **8 usuarios** precargados:
- Juan Pérez (IT, Developer, Active)
- María García (HR, Manager, Active)
- Carlos López (Finance, Analyst, Active)
- etc.

### 6.4. Consultas de Prueba

```sql
-- Usuarios del departamento IT
SELECT * FROM users WHERE department = 'IT';

-- Usuarios activos
SELECT * FROM users WHERE active = true;

-- Contar usuarios por departamento
SELECT department, COUNT(*)
FROM users
GROUP BY department;
```

---

## 7. Verificación de Endpoints MCP

### 7.1. Health Check

```bash
curl http://localhost:8083/mcp/health

# Respuesta esperada:
# {"status":"UP","message":"MCP Server RA3 is running"}
```

### 7.2. Listar Herramientas MCP

```bash
curl http://localhost:8083/mcp/tools

# Respuesta esperada (JSON):
# {
#   "tools": [
#     { "name": "test_entity_manager", "description": "..." },
#     { "name": "create_user", "description": "..." },
#     ...
#   ],
#   "count": 10
# }
```

### 7.3. Probar `test_entity_manager`

```bash
curl -X POST http://localhost:8083/mcp/test_entity_manager

# Respuesta esperada:
# "EntityManager activo. DB: RA3DB"
```

### 7.4. Probar `find_all_users`

```bash
curl -X POST http://localhost:8083/mcp/find_all_users

# Respuesta esperada: Array JSON con 8 usuarios
```

### 7.5. Crear un Usuario

```bash
curl -X POST http://localhost:8083/mcp/create_user \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "test@example.com",
    "department": "IT",
    "role": "Tester",
    "active": true
  }'

# Respuesta esperada: Objeto User con ID generado
```

---

## 8. Solución de Problemas

### 8.1. Error: "Port 8083 is already in use"

**Problema:** Otro proceso está usando el puerto 8083.

**Solución 1: Detener el proceso**

```bash
# Linux/Mac: Encontrar proceso
lsof -i :8083
# Matar proceso: kill -9 <PID>

# Windows: Encontrar proceso
netstat -ano | findstr :8083
# Matar proceso: taskkill /PID <PID> /F
```

**Solución 2: Cambiar puerto**

Edita `src/main/resources/application.yml`:

```yaml
server:
  port: 8084  # Cambiar a otro puerto
```

### 8.2. Error: "Unsupported class file major version 65"

**Problema:** Estás usando Java 17 o inferior.

**Solución:**
1. Verifica versión: `java -version`
2. Instala Java 21 de https://adoptium.net/
3. Configura `JAVA_HOME` correctamente
4. Reinicia terminal/IDE

### 8.3. Error: "Could not resolve all dependencies"

**Problema:** Gradle no puede descargar dependencias.

**Solución:**

```bash
# Limpiar caché y rebuild
./gradlew clean build --refresh-dependencies

# Si estás detrás de un proxy, configura en ~/.gradle/gradle.properties:
# systemProp.http.proxyHost=proxy.empresa.com
# systemProp.http.proxyPort=8080
```

### 8.4. Error: "Table 'USERS' not found"

**Problema:** `schema.sql` no se ejecutó.

**Solución:**

Verifica en `application.yml`:

```yaml
spring:
  sql:
    init:
      mode: always  # Debe ser "always"
      schema-locations: classpath:schema.sql
      data-locations: classpath:data.sql
```

Reinicia la aplicación.

### 8.5. Error al Conectar a H2 Console

**Problema:** JDBC URL incorrecta.

**Solución:**

Usa exactamente: `jdbc:h2:mem:ra3db` (sin espacios, sin mayúsculas/minúsculas incorrectas).

Verifica que la aplicación esté corriendo ANTES de abrir H2 Console.

### 8.6. IntelliJ No Reconoce @Entity

**Problema:** Anotaciones JPA no se reconocen.

**Solución:**

1. Click derecho en proyecto → `Maven` / `Gradle` → `Reload Project`
2. Verifica que la dependencia `spring-boot-starter-data-jpa` esté en `build.gradle`
3. Invalidar caché: `File` → `Invalidate Caches / Restart`

### 8.7. Tests Fallan con "No qualifying bean"

**Problema:** Spring no encuentra el bean `HibernateUserService`.

**Solución:**

Verifica que `HibernateUserServiceImpl` tenga `@Service`:

```java
@Service
public class HibernateUserServiceImpl implements HibernateUserService {
    // ...
}
```

### 8.8. Gradle Wrapper No Ejecutable (Linux/Mac)

**Problema:** `./gradlew` da error de permisos.

**Solución:**

```bash
# Dar permisos de ejecución
chmod +x gradlew
```

---

## Próximos Pasos

Una vez que hayas completado la instalación y verificado que todo funciona:

1. **Lee la Guía del Estudiante**: [GUIA_ESTUDIANTE.md](GUIA_ESTUDIANTE.md)
2. **Ejecuta los tests**: `./gradlew test`
3. **Explora el código**: Empieza por `HibernateUserServiceImpl.java`
4. **Prueba los ejemplos**: Usa H2 Console y observa el SQL generado

---

## Recursos Adicionales

- **Documentación Spring Boot**: https://docs.spring.io/spring-boot/docs/current/reference/html/
- **Documentación Spring Data JPA**: https://docs.spring.io/spring-data/jpa/reference/
- **Documentación Hibernate**: https://hibernate.org/orm/documentation/
- **H2 Database**: https://www.h2database.com/html/main.html

---

**¿Problemas no resueltos?**

Consulta la [PREGUNTAS_FRECUENTES.md](PREGUNTAS_FRECUENTES.md) o revisa los logs de la aplicación para más detalles del error.
