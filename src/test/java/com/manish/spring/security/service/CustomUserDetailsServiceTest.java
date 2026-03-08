package com.manish.spring.security.service;

import com.manish.spring.security.Entity.User;
import com.manish.spring.security.Repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    // ─── loadUserByUsername ───────────────────────────────────────────────────

    @Test
    void loadUserByUsername_returnsUserDetails_whenUserFound() {
        // Arrange
        User user = new User(1L, "Alice", "Smith", "alice@example.com", "encodedPass", null /* no role */);

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));

        // Act
        UserDetails result = customUserDetailsService.loadUserByUsername("alice@example.com");

        // Assert
        assertThat(result.getUsername()).isEqualTo("alice@example.com");
        assertThat(result.getPassword()).isEqualTo("encodedPass");
        assertThat(result.getAuthorities()).isEmpty();
        verify(userRepository).findByEmail("alice@example.com");
    }

    @Test
    void loadUserByUsername_throwsUsernameNotFoundException_whenUserNotFound() {
        // Arrange
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("unknown@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User Not Found");
        verify(userRepository).findByEmail("unknown@example.com");
    }
}
