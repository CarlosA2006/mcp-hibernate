package com.dam.accesodatos.ra3;

import com.dam.accesodatos.model.User;
import com.dam.accesodatos.model.UserCreateDto;
import com.dam.accesodatos.model.UserUpdateDto;
import com.dam.accesodatos.model.UserQueryDto;
import com.dam.accesodatos.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de integración para los métodos IMPLEMENTADOS de
 * HibernateUserServiceImpl
 *
 * @SpringBootTest carga el contexto completo de Spring con base de datos H2
 *                 real.
 *                 Estos tests validan que los métodos de ejemplo funcionan
 *                 correctamente end-to-end.
 *
 *                 COBERTURA: 7 tests que validan los 6 métodos implementados:
 *                 1. testEntityManager() - 1 test
 *                 2. createUser() + findUserById() + updateUser() - 1 test de
 *                 flujo completo
 *                 3. findAll() - 1 test
 *                 4. findUsersByDepartment() - 1 test
 *                 5. Flujo CRUD completo - 1 test integrado
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Tests Integración - Métodos Implementados")
class HibernateUserServiceIntegrationTest {

    @Autowired
    private HibernateUserService service;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        // Limpiar BD antes de cada test
        userRepository.deleteAll();
    }

    // ========== Tests de conexión y configuración ==========

    @Test
    @DisplayName("testEntityManager() - Conexión real funciona")
    void testEntityManager_RealConnection_Success() {
        // When
        String result = service.testEntityManager();

        // Then
        assertNotNull(result);
        assertTrue(result.contains("EntityManager activo"));
        // Verificar que hay algún nombre de base de datos en el resultado
        assertTrue(result.length() > 20, "El resultado debe contener información de la BD");
    }

    // ========== Tests de flujo CRUD completo ==========

    @Test
    @DisplayName("Flujo CRUD completo - Create, Read, Update con métodos implementados")
    void crudFlow_CompleteLifecycle_Success() {
        // 1. CREATE - Crear usuario con createUser()
        UserCreateDto createDto = new UserCreateDto();
        createDto.setName("Integration Test User");
        createDto.setEmail("integration@test.com");
        createDto.setDepartment("IT");
        createDto.setRole("Developer");

        User created = service.createUser(createDto);
        assertNotNull(created.getId());
        assertEquals("Integration Test User", created.getName());
        assertEquals("IT", created.getDepartment());

        // 2. READ - Buscar usuario con findUserById()
        User found = service.findUserById(created.getId());
        assertNotNull(found);
        assertEquals(created.getId(), found.getId());
        assertEquals("Integration Test User", found.getName());

        // 3. UPDATE - Actualizar usuario con updateUser()
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setName("Updated Name");
        updateDto.setDepartment("HR");

        User updated = service.updateUser(created.getId(), updateDto);
        assertEquals("Updated Name", updated.getName());
        assertEquals("HR", updated.getDepartment());
        assertEquals(created.getId(), updated.getId());

        // 4. VERIFY - Verificar que los cambios persisten
        User verified = service.findUserById(created.getId());
        assertEquals("Updated Name", verified.getName());
        assertEquals("HR", verified.getDepartment());
    }

    // ========== Tests de findAll() ==========

    @Test
    @DisplayName("findAll() - Retorna todos los usuarios creados")
    void findAll_ReturnsAllUsers() {
        // Given - Crear varios usuarios
        createTestUser("User 1", "user1@test.com", "IT");
        createTestUser("User 2", "user2@test.com", "HR");
        createTestUser("User 3", "user3@test.com", "IT");

        // When
        List<User> allUsers = service.findAll();

        // Then
        assertNotNull(allUsers);
        assertEquals(3, allUsers.size());
    }

    // ========== Tests de findUsersByDepartment() ==========

    @Test
    @DisplayName("findUsersByDepartment() - Filtra por departamento correctamente")
    void findUsersByDepartment_WithRealData_Success() {
        // Given - Crear usuarios en diferentes departamentos
        createTestUser("Alice", "alice@test.com", "IT");
        createTestUser("Bob", "bob@test.com", "IT");
        createTestUser("Charlie", "charlie@test.com", "HR");
        createTestUser("Diana", "diana@test.com", "HR");
        createTestUser("Eve", "eve@test.com", "Finance");

        // When - Buscar solo IT
        List<User> itUsers = service.findUsersByDepartment("IT");

        // Then
        assertNotNull(itUsers);
        assertEquals(2, itUsers.size());
        assertTrue(itUsers.stream().allMatch(u -> "IT".equals(u.getDepartment())));
        assertTrue(itUsers.stream().anyMatch(u -> "Alice".equals(u.getName())));
        assertTrue(itUsers.stream().anyMatch(u -> "Bob".equals(u.getName())));
    }

    @Test
    @DisplayName("findUsersByDepartment() - Retorna lista vacía si no hay match")
    void findUsersByDepartment_NoMatch_EmptyList() {
        // Given
        createTestUser("User 1", "user1@test.com", "IT");

        // When
        List<User> result = service.findUsersByDepartment("NonExistent");

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ========== Tests de casos límite ==========

    @Test
    @DisplayName("findUserById() - Retorna null para ID inexistente")
    void findUserById_NonExistent_ReturnsNull() {
        // When
        User result = service.findUserById(999L);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("updateUser() - Falla con ID inexistente")
    void updateUser_NonExistent_ThrowsException() {
        // Given
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setName("New Name");

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            service.updateUser(999L, updateDto);
        });
    }

    // ========== Tests Nuevos Métodos (RA3) ==========

    @Test
    @DisplayName("deleteUser() - Elimina usuario correctamente en BD real")
    void deleteUser_RealDB_Success() {
        // Given
        User user = createTestUser("To Delete", "delete@test.com", "Temp");
        Long id = user.getId();

        // When
        boolean deleted = service.deleteUser(id);

        // Then
        assertTrue(deleted);
        assertNull(service.findUserById(id));
    }

    @Test
    @DisplayName("searchUsers() - Filtra correctamente en BD real")
    void searchUsers_RealDB_Success() {
        // Given
        createTestUser("Search 1", "s1@test.com", "DevOps");
        createTestUser("Search 2", "s2@test.com", "DevOps");
        createTestUser("Other", "other@test.com", "HR");

        UserQueryDto query = new UserQueryDto();
        query.setDepartment("DevOps");
        query.setActive(true);

        // When
        List<User> results = service.searchUsers(query);

        // Then
        assertEquals(2, results.size());
    }

    @Test
    @DisplayName("transferData() - Transacción guarda todos los usuarios")
    void transferData_RealDB_Success() {
        // Given
        User u1 = new User();
        u1.setName("Bulk 1");
        u1.setEmail("bulk1@test.com");
        u1.setDepartment("Bulk");
        u1.setRole("Developer");
        u1.setActive(true);

        User u2 = new User();
        u2.setName("Bulk 2");
        u2.setEmail("bulk2@test.com");
        u2.setDepartment("Bulk");
        u2.setRole("Tester");
        u2.setActive(true);

        // When
        boolean result = service.transferData(List.of(u1, u2));

        // Then
        assertTrue(result);
        assertEquals(2, service.executeCountByDepartment("Bulk"));
    }

    @Test
    @DisplayName("executeCountByDepartment() - Cuenta correctamente en BD real")
    void executeCountByDepartment_RealDB_Success() {
        // Given
        createTestUser("C1", "c1@test.com", "Sales");
        createTestUser("C2", "c2@test.com", "Sales");
        createTestUser("C3", "c3@test.com", "Sales");
        createTestUser("Other", "other@test.com", "Marketing");

        // When
        long count = service.executeCountByDepartment("Sales");

        // Then
        assertEquals(3, count);
    }

    // ========== Métodos auxiliares ==========

    private User createTestUser(String name, String email, String department) {
        UserCreateDto dto = new UserCreateDto();
        dto.setName(name);
        dto.setEmail(email);
        dto.setDepartment(department);
        dto.setRole("Developer");

        return service.createUser(dto);
    }
}
