package com.manish.spring.security.service;

import com.manish.spring.security.Entity.Order;
import com.manish.spring.security.Entity.Role;
import com.manish.spring.security.Entity.User;
import com.manish.spring.security.Repository.OrderRepository;
import com.manish.spring.security.Repository.UserRepository;
import com.manish.spring.security.exception.ResourceNotFoundException;
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
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private UserService userService;

    // ─── getUsers ────────────────────────────────────────────────────────────

    @Test
    void getUsers_returnsAllUsers() {
        // Arrange
        Role role = new Role(1L, "ADMIN", Collections.emptyList());
        User user1 = new User(1L, "Alice", "Smith", "alice@example.com", "pass1", role);
        User user2 = new User(2L, "Bob", "Jones", "bob@example.com", "pass2", role);
        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        // Act
        List<User> result = userService.getUsers();

        // Assert
        assertThat(result).hasSize(2).containsExactly(user1, user2);
        verify(userRepository).findAll();
    }

    @Test
    void getUsers_returnsEmptyListWhenNoUsers() {
        // Arrange
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<User> result = userService.getUsers();

        // Assert
        assertThat(result).isEmpty();
        verify(userRepository).findAll();
    }

    // ─── createUser ──────────────────────────────────────────────────────────

    @Test
    void createUser_savesAndReturnsUser() {
        // Arrange
        User input = new User(null, "Alice", "Smith", "alice@example.com", "password", null);
        User saved = new User(1L, "Alice", "Smith", "alice@example.com", "password", null);
        when(userRepository.save(input)).thenReturn(saved);

        // Act
        User result = userService.createUser(input);

        // Assert
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("alice@example.com");
        verify(userRepository).save(input);
    }

    // ─── getUser ─────────────────────────────────────────────────────────────

    @Test
    void getUser_returnsUserWhenFound() {
        // Arrange
        User user = new User(1L, "Alice", "Smith", "alice@example.com", "pass", null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        User result = userService.getUser(1L);

        // Assert
        assertThat(result).isEqualTo(user);
        verify(userRepository).findById(1L);
    }

    @Test
    void getUser_throwsResourceNotFoundExceptionWhenMissing() {
        // Arrange
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.getUser(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User")
                .hasMessageContaining("99");
        verify(userRepository).findById(99L);
    }

    // ─── updateUser ──────────────────────────────────────────────────────────

    @Test
    void updateUser_updatesFieldsAndReturnsUpdatedUser() {
        // Arrange
        Role role = new Role(1L, "USER", Collections.emptyList());
        User existing = new User(1L, "Old", "Name", "old@example.com", "oldpass", null);
        User update = new User(null, "New", "Name", "new@example.com", "newpass", role);
        User saved = new User(1L, "New", "Name", "new@example.com", "newpass", role);
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenReturn(saved);

        // Act
        User result = userService.updateUser(1L, update);

        // Assert
        assertThat(result.getFirstName()).isEqualTo("New");
        assertThat(result.getEmail()).isEqualTo("new@example.com");
        assertThat(result.getRole()).isEqualTo(role);
        verify(userRepository).findById(1L);
        verify(userRepository).save(existing);
    }

    @Test
    void updateUser_throwsResourceNotFoundExceptionWhenMissing() {
        // Arrange
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.updateUser(99L, new User()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User");
        verify(userRepository, never()).save(any());
    }

    // ─── deleteUser ──────────────────────────────────────────────────────────

    @Test
    void deleteUser_deletesSuccessfullyWhenNoOrders() {
        // Arrange
        User user = new User(1L, "Alice", "Smith", "alice@example.com", "pass", null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.findByUserId(1L)).thenReturn(Collections.emptyList());

        // Act
        userService.deleteUser(1L);

        // Assert
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_throwsResourceNotFoundExceptionWhenMissing() {
        // Arrange
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.deleteUser(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User");
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    void deleteUser_throwsConflictWhenUserHasOrders() {
        // Arrange
        User user = new User(1L, "Alice", "Smith", "alice@example.com", "pass", null);
        Order order = new Order();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.findByUserId(1L)).thenReturn(List.of(order));

        // Act & Assert
        assertThatThrownBy(() -> userService.deleteUser(1L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT));
        verify(userRepository, never()).deleteById(any());
    }
}
