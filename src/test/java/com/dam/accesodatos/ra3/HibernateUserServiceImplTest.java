package com.dam.accesodatos.ra3;

import com.dam.accesodatos.model.User;
import com.dam.accesodatos.model.UserCreateDto;
import com.dam.accesodatos.model.UserUpdateDto;
import com.dam.accesodatos.model.UserQueryDto;
import com.dam.accesodatos.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para los métodos IMPLEMENTADOS de HibernateUserServiceImpl
 *
 * Estos tests cubren los 6 métodos de ejemplo implementados.
 * Los estudiantes pueden usarlos como guía para testear sus propias
 * implementaciones.
 *
 * COBERTURA: 10 tests que validan los 6 métodos implementados:
 * 1. testEntityManager() - 2 tests
 * 2. createUser() - 1 test
 * 3. findUserById() - 2 tests
 * 4. updateUser() - 2 tests
 * 5. findAll() - 1 test
 * 6. findUsersByDepartment() - 2 tests
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Unitarios - Métodos Implementados")
class HibernateUserServiceImplTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private HibernateUserServiceImpl service;

    private User testUser;
    private UserCreateDto createDto;
    private UserUpdateDto updateDto;

    @BeforeEach
    void setUp() {
        // Usuario de prueba
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setDepartment("IT");
        testUser.setRole("Developer");
        testUser.setActive(true);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());

        // DTO para crear
        createDto = new UserCreateDto();
        createDto.setName("New User");
        createDto.setEmail("new@example.com");
        createDto.setDepartment("HR");
        createDto.setRole("Manager");

        // DTO para actualizar
        updateDto = new UserUpdateDto();
        updateDto.setName("Updated Name");
        updateDto.setDepartment("Finance");
    }

    // ========== Tests para testEntityManager() ==========

    @Test
    @DisplayName("testEntityManager() - Verifica EntityManager activo")
    void testEntityManager_Success() {
        // Given
        when(entityManager.isOpen()).thenReturn(true);
        Query query = mock(Query.class);
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.getSingleResult()).thenReturn(new Object[] { 1, "H2" });

        // When
        String result = service.testEntityManager();

        // Then
        assertNotNull(result);
        assertTrue(result.contains("EntityManager activo"));
        assertTrue(result.contains("H2"));
        verify(entityManager).isOpen();
        verify(entityManager).createNativeQuery(anyString());
    }

    @Test
    @DisplayName("testEntityManager() - Falla si EntityManager cerrado")
    void testEntityManager_ClosedEntityManager() {
        // Given
        when(entityManager.isOpen()).thenReturn(false);

        // When & Then
        assertThrows(RuntimeException.class, () -> service.testEntityManager());
        verify(entityManager).isOpen();
        verify(entityManager, never()).createNativeQuery(anyString());
    }

    // ========== Tests para createUser() ==========

    @Test
    @DisplayName("createUser() - Crea usuario correctamente")
    void createUser_Success() {
        // Given
        doNothing().when(entityManager).persist(any(User.class));

        // When
        User result = service.createUser(createDto);

        // Then
        assertNotNull(result);
        assertEquals(createDto.getName(), result.getName());
        assertEquals(createDto.getEmail(), result.getEmail());
        assertEquals(createDto.getDepartment(), result.getDepartment());
        assertEquals(createDto.getRole(), result.getRole());
        assertTrue(result.getActive());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
        verify(entityManager).persist(any(User.class));
    }

    // ========== Tests para findUserById() ==========

    @Test
    @DisplayName("findUserById() - Encuentra usuario existente")
    void findUserById_Found() {
        // Given
        when(entityManager.find(User.class, 1L)).thenReturn(testUser);

        // When
        User result = service.findUserById(1L);

        // Then
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getName(), result.getName());
        verify(entityManager).find(User.class, 1L);
    }

    @Test
    @DisplayName("findUserById() - Retorna null si no existe")
    void findUserById_NotFound() {
        // Given
        when(entityManager.find(User.class, 999L)).thenReturn(null);

        // When
        User result = service.findUserById(999L);

        // Then
        assertNull(result);
        verify(entityManager).find(User.class, 999L);
    }

    // ========== Tests para updateUser() ==========

    @Test
    @DisplayName("updateUser() - Actualiza usuario existente")
    void updateUser_Success() {
        // Given
        when(entityManager.find(User.class, 1L)).thenReturn(testUser);
        when(entityManager.merge(any(User.class))).thenReturn(testUser);

        // When
        User result = service.updateUser(1L, updateDto);

        // Then
        assertNotNull(result);
        assertEquals(updateDto.getName(), testUser.getName());
        assertEquals(updateDto.getDepartment(), testUser.getDepartment());
        verify(entityManager).find(User.class, 1L);
        verify(entityManager).merge(any(User.class));
    }

    @Test
    @DisplayName("updateUser() - Lanza excepción si usuario no existe")
    void updateUser_NotFound() {
        // Given
        when(entityManager.find(User.class, 999L)).thenReturn(null);

        // When & Then
        assertThrows(RuntimeException.class, () -> service.updateUser(999L, updateDto));
        verify(entityManager).find(User.class, 999L);
        verify(entityManager, never()).merge(any(User.class));
    }

    // ========== Tests para findAll() ==========

    @Test
    @DisplayName("findAll() - Retorna todos los usuarios")
    void findAll_Success() {
        // Given
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findAll()).thenReturn(users);

        // When
        List<User> result = service.findAll();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUser.getId(), result.get(0).getId());
        verify(userRepository).findAll();
    }

    // ========== Tests para findUsersByDepartment() ==========

    @Test
    @DisplayName("findUsersByDepartment() - Busca por departamento con JPQL")
    void findUsersByDepartment_Success() {
        // Given
        TypedQuery<User> query = mock(TypedQuery.class);
        List<User> users = Arrays.asList(testUser);
        when(entityManager.createQuery(anyString(), eq(User.class))).thenReturn(query);
        when(query.setParameter(anyString(), anyString())).thenReturn(query);
        when(query.getResultList()).thenReturn(users);

        // When
        List<User> result = service.findUsersByDepartment("IT");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(entityManager).createQuery(anyString(), eq(User.class));
        verify(query).setParameter("dept", "IT");
        verify(query).getResultList();
    }

    @Test
    @DisplayName("findUsersByDepartment() - Retorna lista vacía si no hay resultados")
    void findUsersByDepartment_EmptyResult() {
        // Given
        TypedQuery<User> query = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(User.class))).thenReturn(query);
        when(query.setParameter(anyString(), anyString())).thenReturn(query);
        when(query.getResultList()).thenReturn(Arrays.asList());

        // When
        List<User> result = service.findUsersByDepartment("NonExistent");

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(query).getResultList();
    }

    // ========== Tests para deleteUser() ==========

    @Test
    @DisplayName("deleteUser() - Elimina usuario existente")
    void deleteUser_Success() {
        // Given
        when(entityManager.find(User.class, 1L)).thenReturn(testUser);

        // When
        boolean result = service.deleteUser(1L);

        // Then
        assertTrue(result);
        verify(entityManager).find(User.class, 1L);
        verify(entityManager).remove(testUser);
    }

    @Test
    @DisplayName("deleteUser() - Retorna false si usuario no existe")
    void deleteUser_NotFound() {
        // Given
        when(entityManager.find(User.class, 999L)).thenReturn(null);

        // When
        boolean result = service.deleteUser(999L);

        // Then
        assertFalse(result);
        verify(entityManager).find(User.class, 999L);
        verify(entityManager, never()).remove(any());
    }

    // ========== Tests para searchUsers() ==========

    @Test
    @DisplayName("searchUsers() - Busca con filtros dinámicos")
    void searchUsers_WithFilters() {
        // Given
        UserQueryDto queryDto = new UserQueryDto();
        queryDto.setDepartment("IT");
        queryDto.setActive(true);

        TypedQuery<User> query = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(User.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(Arrays.asList(testUser));

        // When
        List<User> result = service.searchUsers(queryDto);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(entityManager).createQuery(contains("AND u.department = :dept"), eq(User.class));
        verify(entityManager).createQuery(contains("AND u.active = :active"), eq(User.class));
        verify(query).setParameter("dept", "IT");
        verify(query).setParameter("active", true);
    }

    @Test
    @DisplayName("searchUsers() - Busca sin filtros")
    void searchUsers_NoFilters() {
        // Given
        UserQueryDto queryDto = new UserQueryDto();
        TypedQuery<User> query = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(User.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(Arrays.asList(testUser));

        // When
        List<User> result = service.searchUsers(queryDto);

        // Then
        assertNotNull(result);
        verify(entityManager).createQuery(anyString(), eq(User.class));
        verify(query, never()).setParameter(anyString(), any());
    }

    // ========== Tests para transferData() ==========

    @Test
    @DisplayName("transferData() - Persiste lista de usuarios")
    void transferData_Success() {
        // Given
        List<User> users = Arrays.asList(testUser, new User());

        // When
        boolean result = service.transferData(users);

        // Then
        assertTrue(result);
        verify(entityManager, times(2)).persist(any(User.class));
    }

    // ========== Tests para executeCountByDepartment() ==========

    @Test
    @DisplayName("executeCountByDepartment() - Cuenta usuarios por departamento")
    void executeCountByDepartment_Success() {
        // Given
        TypedQuery<Long> query = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query); // Fluent API mock
        when(query.getSingleResult()).thenReturn(5L);

        // When
        long count = service.executeCountByDepartment("IT");

        // Then
        assertEquals(5L, count);
        verify(entityManager).createQuery(contains("COUNT(u)"), eq(Long.class));
        verify(entityManager).createQuery(contains("active = true"), eq(Long.class));
        verify(query).setParameter("dept", "IT");
    }
}
