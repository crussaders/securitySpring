package com.manish.spring.security.service;

import com.manish.spring.security.Entity.Role;
import com.manish.spring.security.Entity.User;
import com.manish.spring.security.Repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleService roleService;

    // ─── getAllRoles ──────────────────────────────────────────────────────────

    @Test
    void getAllRoles_returnsAllRoles() {
        // Arrange
        Role admin = new Role(1L, "ADMIN", Collections.emptyList());
        Role user = new Role(2L, "USER", Collections.emptyList());
        when(roleRepository.findAll()).thenReturn(List.of(admin, user));

        // Act
        List<Role> result = roleService.getAllRoles();

        // Assert
        assertThat(result).hasSize(2).containsExactly(admin, user);
        verify(roleRepository).findAll();
    }

    @Test
    void getAllRoles_returnsEmptyListWhenNoRoles() {
        // Arrange
        when(roleRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<Role> result = roleService.getAllRoles();

        // Assert
        assertThat(result).isEmpty();
        verify(roleRepository).findAll();
    }

    // ─── getRole ─────────────────────────────────────────────────────────────

    @Test
    void getRole_returnsRoleWhenFound() {
        // Arrange
        Role role = new Role(1L, "ADMIN", Collections.emptyList());
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));

        // Act
        Role result = roleService.getRole(1L);

        // Assert
        assertThat(result).isEqualTo(role);
        assertThat(result.getRoleName()).isEqualTo("ADMIN");
        verify(roleRepository).findById(1L);
    }

    @Test
    void getRole_throwsRuntimeExceptionWhenMissing() {
        // Arrange
        when(roleRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> roleService.getRole(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Role not found");
        verify(roleRepository).findById(99L);
    }

    // ─── createRole ──────────────────────────────────────────────────────────

    @Test
    void createRole_savesAndReturnsRole() {
        // Arrange
        Role input = new Role(null, "MANAGER", null);
        Role saved = new Role(3L, "MANAGER", Collections.emptyList());
        when(roleRepository.save(input)).thenReturn(saved);

        // Act
        Role result = roleService.createRole(input);

        // Assert
        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getRoleName()).isEqualTo("MANAGER");
        verify(roleRepository).save(input);
    }

    // ─── updateRole ──────────────────────────────────────────────────────────

    @Test
    void updateRole_updatesRoleNameAndReturnsRole() {
        // Arrange
        Role existing = new Role(1L, "OLD_ROLE", Collections.emptyList());
        Role updateRequest = new Role(null, "NEW_ROLE", null);
        Role saved = new Role(1L, "NEW_ROLE", Collections.emptyList());
        when(roleRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(roleRepository.save(existing)).thenReturn(saved);

        // Act
        Role result = roleService.updateRole(1L, updateRequest);

        // Assert
        assertThat(existing.getRoleName()).isEqualTo("NEW_ROLE");
        assertThat(result.getRoleName()).isEqualTo("NEW_ROLE");
        verify(roleRepository).findById(1L);
        verify(roleRepository).save(existing);
    }

    @Test
    void updateRole_throwsRuntimeExceptionWhenMissing() {
        // Arrange
        when(roleRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> roleService.updateRole(99L, new Role()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Role not found");
        verify(roleRepository, never()).save(any());
    }

    // ─── deleteRole ──────────────────────────────────────────────────────────

    @Test
    void deleteRole_deletesSuccessfullyWhenRoleHasNoUsers() {
        // Arrange
        Role role = new Role(1L, "ADMIN", Collections.emptyList());
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));

        // Act
        roleService.deleteRole(1L);

        // Assert
        verify(roleRepository).delete(role);
    }

    @Test
    void deleteRole_throwsRuntimeExceptionWhenMissing() {
        // Arrange
        when(roleRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> roleService.deleteRole(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Role not found");
        verify(roleRepository, never()).delete(any());
    }

    @Test
    void deleteRole_throwsConflictWhenRoleIsAssignedToUsers() {
        // Arrange
        User user = new User(1L, "Alice", "Smith", "alice@example.com", "pass", null);
        Role role = new Role(1L, "ADMIN", List.of(user));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));

        // Act & Assert
        assertThatThrownBy(() -> roleService.deleteRole(1L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT));
        verify(roleRepository, never()).delete(any());
    }
}
