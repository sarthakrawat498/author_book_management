package com.sarthak.library.author_book_management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sarthak.library.author_book_management.dto.user.CreateUserRequest;
import com.sarthak.library.author_book_management.dto.user.UpdateRoleRequest;
import com.sarthak.library.author_book_management.dto.user.UserResponse;
import com.sarthak.library.author_book_management.exception.GlobalExceptionHandler;
import com.sarthak.library.author_book_management.exception.ResourceNotFoundException;
import com.sarthak.library.author_book_management.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private CreateUserRequest buildCreateRequest() {
        CreateUserRequest req = new CreateUserRequest();
        req.setUsername("john_doe");
        req.setPassword("password123");
        req.setRole("ROLE_LIBRARIAN");
        return req;
    }

    private UserResponse buildUserResponse(String username, String role) {
        UserResponse res = new UserResponse();
        res.setUsername(username);
        res.setEnabled(true);
        res.setRole(role);
        return res;
    }

    // ── GET /api/admin/users ──────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/admin/users: returns list of users")
    void getAllUsers_returnsList() throws Exception {
        when(userService.getAllUsers())
                .thenReturn(List.of(
                        buildUserResponse("john_doe", "ROLE_LIBRARIAN"),
                        buildUserResponse("jane_doe", "ROLE_ADMIN")
                ));

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].username").value("john_doe"))
                .andExpect(jsonPath("$[1].username").value("jane_doe"));
    }

    @Test
    @DisplayName("GET /api/admin/users: empty list")
    void getAllUsers_emptyList() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ── POST /api/admin/users ─────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/admin/users: success — 200")
    void createUser_success() throws Exception {
        CreateUserRequest req = buildCreateRequest();
        doNothing().when(userService).createUser(any(CreateUserRequest.class));

        mockMvc.perform(post("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(userService).createUser(any(CreateUserRequest.class));
    }

    @Test
    @DisplayName("POST /api/admin/users: user not found during creation — 404")
    void createUser_notFound_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Role not found"))
                .when(userService).createUser(any(CreateUserRequest.class));

        mockMvc.perform(post("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildCreateRequest())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // ── PUT /api/admin/users/{username}/role ──────────────────────────────────

    @Test
    @DisplayName("PUT /api/admin/users/{username}/role: success — 200")
    void updateRole_success() throws Exception {
        UpdateRoleRequest req = new UpdateRoleRequest();
        req.setRole("ROLE_ADMIN");

        doNothing().when(userService).updateRole(eq("john_doe"), eq("ROLE_ADMIN"));

        mockMvc.perform(put("/api/admin/users/john_doe/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(userService).updateRole("john_doe", "ROLE_ADMIN");
    }

    @Test
    @DisplayName("PUT /api/admin/users/{username}/role: user not found — 404")
    void updateRole_userNotFound_returns404() throws Exception {
        UpdateRoleRequest req = new UpdateRoleRequest();
        req.setRole("ROLE_ADMIN");

        doThrow(new ResourceNotFoundException("User not found: unknown_user"))
                .when(userService).updateRole(eq("unknown_user"), any());

        mockMvc.perform(put("/api/admin/users/unknown_user/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}

