package com.sarthak.library.author_book_management.service;

import com.sarthak.library.author_book_management.entity.Authority;
import com.sarthak.library.author_book_management.entity.User;
import com.sarthak.library.author_book_management.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    // ── helpers ──────────────────────────────────────────────────────────────

    private User buildUser(String username, String role, boolean enabled) {
        User user = User.builder()
                .username(username)
                .password("encoded_password")
                .enabled(enabled)
                .build();
        Authority authority = Authority.builder()
                .authority(role)
                .user(user)
                .build();
        user.setAuthorities(Set.of(authority));
        return user;
    }

    // ── loadUserByUsername ────────────────────────────────────────────────────

    @Test
    @DisplayName("loadUserByUsername: existing user — returns populated UserDetails")
    void loadUserByUsername_success() {
        User user = buildUser("admin", "ROLE_ADMIN", true);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        UserDetails details = customUserDetailsService.loadUserByUsername("admin");

        assertThat(details.getUsername()).isEqualTo("admin");
        assertThat(details.getPassword()).isEqualTo("encoded_password");
        assertThat(details.isEnabled()).isTrue();
        assertThat(details.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_ADMIN");

        verify(userRepository).findByUsername("admin");
    }

    @Test
    @DisplayName("loadUserByUsername: multiple roles — all roles present in UserDetails")
    void loadUserByUsername_multipleRoles() {
        User user = User.builder()
                .username("librarian")
                .password("pass")
                .enabled(true)
                .build();
        Authority r1 = Authority.builder().authority("ROLE_LIBRARIAN").user(user).build();
        Authority r2 = Authority.builder().authority("ROLE_ADMIN").user(user).build();
        user.setAuthorities(Set.of(r1, r2));
        when(userRepository.findByUsername("librarian")).thenReturn(Optional.of(user));

        UserDetails details = customUserDetailsService.loadUserByUsername("librarian");

        assertThat(details.getAuthorities()).hasSize(2);
        assertThat(details.getAuthorities())
                .extracting("authority")
                .containsExactlyInAnyOrder("ROLE_LIBRARIAN", "ROLE_ADMIN");
    }

    @Test
    @DisplayName("loadUserByUsername: disabled user — UserDetails.isEnabled() is false")
    void loadUserByUsername_disabledUser() {
        User user = buildUser("inactive", "ROLE_LIBRARIAN", false);
        when(userRepository.findByUsername("inactive")).thenReturn(Optional.of(user));

        UserDetails details = customUserDetailsService.loadUserByUsername("inactive");

        assertThat(details.isEnabled()).isFalse();
    }

    @Test
    @DisplayName("loadUserByUsername: unknown username — throws UsernameNotFoundException")
    void loadUserByUsername_notFound() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("ghost"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findByUsername("ghost");
    }
}

