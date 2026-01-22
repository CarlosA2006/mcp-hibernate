package com.dam.accesodatos.ra3;

import com.dam.accesodatos.model.User;
import com.dam.accesodatos.model.UserCreateDto;
import com.dam.accesodatos.model.UserUpdateDto;
import com.dam.accesodatos.model.UserQueryDto;
import com.dam.accesodatos.repository.UserRepository;
import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Implementación del servicio Hibernate/JPA para gestión de usuarios
 *
 * VERSIÓN MÍNIMOS ESTRICTOS
 *
 * ESTRUCTURA DE IMPLEMENTACIÓN:
 * - ✅ 6 MÉTODOS IMPLEMENTADOS (ejemplos para estudiantes)
 * - ❌ 4 MÉTODOS TODO (estudiantes deben implementar - mínimo para aprobar RA3)
 *
 * MÉTODOS IMPLEMENTADOS (Ejemplos):
 * 1. testEntityManager() - Ejemplo básico de EntityManager
 * 2. createUser() - INSERT con persist() y @Transactional
 * 3. findUserById() - SELECT con find()
 * 4. updateUser() - UPDATE con merge()
 * 5. findAll() - SELECT all con Repository
 * 6. findUsersByDepartment() - JPQL básico
 *
 * MÉTODOS TODO (Estudiantes implementan - MÍNIMOS):
 * 1. deleteUser() - EntityManager.remove()
 * 2. searchUsers() - JPQL dinámico (simplificado, sin Criteria API)
 * 3. transferData() - Transacción múltiple
 * 4. executeCountByDepartment() - JPQL COUNT
 */
@Service
@Transactional(readOnly = true) // Transacciones de solo lectura por defecto
public class HibernateUserServiceImpl implements HibernateUserService {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    // ========== CE3.a: Configuración y Conexión ORM ==========

    /**
     * ✅ EJEMPLO IMPLEMENTADO 1/6: Prueba EntityManager
     *
     * Este método muestra el patrón fundamental de JPA:
     * 1. Verificar que EntityManager esté abierto
     * 2. Ejecutar una query nativa simple
     * 3. Procesar resultados
     *
     * DIFERENCIAS vs RA2 (JDBC):
     * - RA2: Connection.isClosed(), Statement.executeQuery("SELECT 1")
     * - RA3: EntityManager.isOpen(), createNativeQuery()
     */
    @Override
    public String testEntityManager() {
        if (!entityManager.isOpen()) {
            throw new RuntimeException("EntityManager está cerrado");
        }

        // Ejecutar query nativa simple (SQL directo, no JPQL)
        Query query = entityManager.createNativeQuery("SELECT 1 as test, DATABASE() as db_name");
        Object[] result = (Object[]) query.getSingleResult();

        return String.format("✓ EntityManager activo | Base de datos: %s | Test: %s",
                result[1], result[0]);
    }

    // ========== CE3.d, CE3.e: Operaciones CRUD ==========

    /**
     * ✅ EJEMPLO IMPLEMENTADO 2/6: INSERT con persist()
     *
     * Muestra cómo Hibernate simplifica INSERT:
     * - NO necesitas escribir SQL INSERT
     * - NO necesitas mapear parámetros manualmente
     * - NO necesitas getGeneratedKeys()
     * - Hibernate lo hace todo automáticamente
     *
     * DIFERENCIAS vs RA2 (JDBC):
     * - RA2: PreparedStatement.setString(), executeUpdate(), getGeneratedKeys()
     * - RA3: entityManager.persist(user), todo automático
     *
     * IMPORTANTE: @Transactional es obligatorio para operaciones que modifican BD
     */
    @Override
    @Transactional // ← CRÍTICO: Modifica BD, necesita transacción
    public User createUser(UserCreateDto dto) {
        // Crear entidad desde DTO
        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setDepartment(dto.getDepartment());
        user.setRole(dto.getRole());
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        // persist() guarda la entidad en el contexto de persistencia
        // Hibernate genera automáticamente:
        // INSERT INTO users (name, email, department, role, active, created_at,
        // updated_at)
        // VALUES (?, ?, ?, ?, ?, ?, ?)
        entityManager.persist(user);

        // Al finalizar el método, Spring hace commit automáticamente
        // Hibernate ejecuta el INSERT y setea el ID generado
        return user; // El ID ya está seteado
    }

    /**
     * ✅ EJEMPLO IMPLEMENTADO 3/6: SELECT por ID con find()
     *
     * Muestra la forma más simple de recuperar una entidad por ID.
     *
     * DIFERENCIAS vs RA2 (JDBC):
     * - RA2: PreparedStatement con "SELECT * FROM users WHERE id = ?", mapeo manual
     * de ResultSet
     * - RA3: entityManager.find(User.class, id), todo automático
     *
     * NOTA: find() retorna null si no existe (no lanza excepción)
     */
    @Override
    public User findUserById(Long id) {
        // find() es la forma más directa de buscar por ID
        // Hibernate genera: SELECT ... FROM users WHERE id = ?
        // y mapea automáticamente las columnas a los atributos de User
        return entityManager.find(User.class, id);
    }

    /**
     * ✅ EJEMPLO IMPLEMENTADO 4/6: UPDATE con merge()
     *
     * Muestra cómo actualizar una entidad existente.
     *
     * DIFERENCIAS vs RA2 (JDBC):
     * - RA2: PreparedStatement con "UPDATE users SET ... WHERE id = ?"
     * - RA3: entityManager.merge(user), Hibernate detecta cambios automáticamente
     *
     * PATRÓN IMPORTANTE:
     * 1. Buscar entidad existente
     * 2. Modificar atributos
     * 3. merge() sincroniza cambios con BD
     */
    @Override
    @Transactional // ← Modifica BD
    public User updateUser(Long id, UserUpdateDto dto) {
        // 1. Buscar entidad existente
        User existing = findUserById(id);
        if (existing == null) {
            throw new RuntimeException("No se encontró usuario con ID " + id);
        }

        // 2. Aplicar cambios del DTO
        if (dto.getName() != null) {
            existing.setName(dto.getName());
        }
        if (dto.getEmail() != null) {
            existing.setEmail(dto.getEmail());
        }
        if (dto.getDepartment() != null) {
            existing.setDepartment(dto.getDepartment());
        }
        if (dto.getRole() != null) {
            existing.setRole(dto.getRole());
        }
        if (dto.getActive() != null) {
            existing.setActive(dto.getActive());
        }
        existing.setUpdatedAt(LocalDateTime.now());

        // 3. merge() actualiza la entidad
        // Hibernate detecta qué campos cambiaron y genera UPDATE solo de esos campos
        return entityManager.merge(existing);
        // Al finalizar, Spring hace commit y Hibernate ejecuta el UPDATE
    }

    @Override
    @Transactional
    public boolean deleteUser(Long id) {
        User user = findUserById(id);
        if (user == null) {
            return false;
        }
        entityManager.remove(user);
        return true;
    }

    /**
     * ✅ EJEMPLO IMPLEMENTADO 5/6: SELECT all con Repository
     *
     * Muestra cómo usar Spring Data JPA Repository.
     * La forma más simple de obtener todas las entidades.
     *
     * DIFERENCIAS vs RA2 (JDBC):
     * - RA2: while(rs.next()) { mapResultSetToUser(rs); }
     * - RA3: userRepository.findAll(), todo automático
     */
    @Override
    public List<User> findAll() {
        // Spring Data JPA genera automáticamente:
        // SELECT u FROM User u
        // y mapea resultados a List<User>
        return userRepository.findAll();
    }

    // ========== CE3.f: Consultas JPQL ==========

    /**
     * ✅ EJEMPLO IMPLEMENTADO 6/6: JPQL básico
     *
     * Muestra cómo escribir consultas JPQL (Java Persistence Query Language).
     *
     * DIFERENCIAS vs RA2 (JDBC):
     * - RA2: SQL "SELECT * FROM users WHERE department = ?"
     * - RA3: JPQL "SELECT u FROM User u WHERE u.department = :dept"
     *
     * IMPORTANTE: JPQL usa nombres de entidades y atributos, NO tablas y columnas
     * - Correcto: "FROM User u" (entidad), "u.department" (atributo)
     * - Incorrecto: "FROM users u" (tabla), "u.department_name" (columna)
     */
    @Override
    public List<User> findUsersByDepartment(String department) {
        // JPQL: Query language orientado a objetos
        // - User (entidad) en lugar de users (tabla)
        // - u.department (atributo) en lugar de department (columna)
        String jpql = "SELECT u FROM User u WHERE u.department = :dept AND u.active = true ORDER BY u.name";

        // TypedQuery garantiza type-safety
        TypedQuery<User> query = entityManager.createQuery(jpql, User.class);
        query.setParameter("dept", department);

        // getResultList() retorna List<User>
        return query.getResultList();
    }

    @Override
    public List<User> searchUsers(UserQueryDto queryDto) {
        StringBuilder jpql = new StringBuilder("SELECT u FROM User u WHERE 1=1");

        if (queryDto.getDepartment() != null && !queryDto.getDepartment().isEmpty()) {
            jpql.append(" AND u.department = :dept");
        }
        if (queryDto.getRole() != null && !queryDto.getRole().isEmpty()) {
            jpql.append(" AND u.role = :role");
        }
        if (queryDto.getActive() != null) {
            jpql.append(" AND u.active = :active");
        }

        TypedQuery<User> query = entityManager.createQuery(jpql.toString(), User.class);

        if (queryDto.getDepartment() != null && !queryDto.getDepartment().isEmpty()) {
            query.setParameter("dept", queryDto.getDepartment());
        }
        if (queryDto.getRole() != null && !queryDto.getRole().isEmpty()) {
            query.setParameter("role", queryDto.getRole());
        }
        if (queryDto.getActive() != null) {
            query.setParameter("active", queryDto.getActive());
        }

        return query.getResultList();
    }

    // ========== CE3.g: Transacciones ==========

    @Override
    @Transactional
    public boolean transferData(List<User> users) {
        for (User user : users) {
            entityManager.persist(user);
        }
        return true;
    }

    @Override
    public long executeCountByDepartment(String department) {
        String jpql = "SELECT COUNT(u) FROM User u WHERE u.department = :dept AND u.active = true";
        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
        query.setParameter("dept", department);
        return query.getSingleResult();
    }
}
