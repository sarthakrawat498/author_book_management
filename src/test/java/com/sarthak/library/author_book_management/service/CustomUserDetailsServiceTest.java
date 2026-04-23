package com.sarthak.library.author_book_management.service;

import com.sarthak.library.author_book_management.entity.Authority;
import com.sarthak.library.author_book_management.entity.User;
import com.sarthak.library.author_book_management.repository.AuthorityRepository;
import com.sarthak.library.author_book_management.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthorityRepository authorityRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    // ── helpers ──────────────────────────────────────────────────────────────

    private User buildUser(String username, boolean enabled) {
        return User.builder()
                .username(username)
                .password("encoded_password")
                .enabled(enabled)
                .build();
    }

    private Authority buildAuthority(String username, String role) {
        return Authority.builder()
                .authority(role)
                .username(username)
                .build();
    }

    // ── loadUserByUsername ────────────────────────────────────────────────────

    @Test
    @DisplayName("loadUserByUsername: existing user — returns populated UserDetails")
    void loadUserByUsername_success() {
        User user = buildUser("admin", true);
        when(userRepository.findById("admin")).thenReturn(Optional.of(user));
        when(authorityRepository.findByUsername("admin"))
                .thenReturn(List.of(buildAuthority("admin", "ROLE_ADMIN")));

        UserDetails details = customUserDetailsService.loadUserByUsername("admin");

        assertThat(details.getUsername()).isEqualTo("admin");
        assertThat(details.getPassword()).isEqualTo("encoded_password");
        assertThat(details.isEnabled()).isTrue();
        assertThat(details.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_ADMIN");

        verify(userRepository).findById("admin");
        verify(authorityRepository).findByUsername("admin");
    }

    @Test
    @DisplayName("loadUserByUsername: multiple roles — all roles present in UserDetails")
    void loadUserByUsername_multipleRoles() {
        User user = buildUser("librarian", true);
        when(userRepository.findById("librarian")).thenReturn(Optional.of(user));
        when(authorityRepository.findByUsername("librarian"))
                .thenReturn(List.of(
                        buildAuthority("librarian", "ROLE_LIBRARIAN"),
                        buildAuthority("librarian", "ROLE_ADMIN")
                ));

        UserDetails details = customUserDetailsService.loadUserByUsername("librarian");

        assertThat(details.getAuthorities()).hasSize(2);
        assertThat(details.getAuthorities())
                .extracting("authority")
                .containsExactlyInAnyOrder("ROLE_LIBRARIAN", "ROLE_ADMIN");
    }

    @Test
    @DisplayName("loadUserByUsername: disabled user — UserDetails.isEnabled() is false")
    void loadUserByUsername_disabledUser() {
        User user = buildUser("inactive", false);
        when(userRepository.findById("inactive")).thenReturn(Optional.of(user));
        when(authorityRepository.findByUsername("inactive"))
                .thenReturn(List.of(buildAuthority("inactive", "ROLE_LIBRARIAN")));

        UserDetails details = customUserDetailsService.loadUserByUsername("inactive");

        assertThat(details.isEnabled()).isFalse();
    }

    @Test
    @DisplayName("loadUserByUsername: unknown username — throws UsernameNotFoundException")
    void loadUserByUsername_notFound() {
        when(userRepository.findById("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("ghost"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findById("ghost");
        verifyNoInteractions(authorityRepository);
    }
}
