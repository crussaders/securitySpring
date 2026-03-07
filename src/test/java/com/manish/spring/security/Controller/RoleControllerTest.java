package com.manish.spring.security.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manish.spring.security.Entity.Role;
import com.manish.spring.security.exception.ResourceNotFoundException;
import com.manish.spring.security.service.RoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RoleControllerTest {

    @Mock
    private RoleService roleService;

    @InjectMocks
    private RoleController roleController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(roleController).build();
        objectMapper = new ObjectMapper();
    }

    private Role buildRole(Long id, String name) {
        return new Role(id, name, Collections.emptyList());
    }

    @Test
    void getRoles_returnsListOfRoles() throws Exception {
        Role role = buildRole(1L, "ADMIN");
        when(roleService.getAllRoles()).thenReturn(List.of(role));

        mockMvc.perform(get("/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].roleName").value("ADMIN"));
    }

    @Test
    void getRole_returnsRole_whenFound() throws Exception {
        Role role = buildRole(1L, "USER");
        when(roleService.getRole(1L)).thenReturn(role);

        mockMvc.perform(get("/roles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.roleName").value("USER"));
    }

    @Test
    void getRole_returns404_whenNotFound() throws Exception {
        when(roleService.getRole(99L)).thenThrow(new ResourceNotFoundException("Role", 99L));

        mockMvc.perform(get("/roles/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createRole_returnsCreatedRole() throws Exception {
        Role input = buildRole(null, "MODERATOR");
        Role saved = buildRole(3L, "MODERATOR");
        when(roleService.createRole(any(Role.class))).thenReturn(saved);

        mockMvc.perform(post("/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.roleName").value("MODERATOR"));
    }

    @Test
    void updateRole_returnsUpdatedRole() throws Exception {
        Role input = buildRole(null, "SUPER_ADMIN");
        Role updated = buildRole(1L, "SUPER_ADMIN");
        when(roleService.updateRole(eq(1L), any(Role.class))).thenReturn(updated);

        mockMvc.perform(put("/roles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.roleName").value("SUPER_ADMIN"));
    }

    @Test
    void updateRole_returns404_whenNotFound() throws Exception {
        Role input = buildRole(null, "SUPER_ADMIN");
        when(roleService.updateRole(eq(99L), any(Role.class)))
                .thenThrow(new ResourceNotFoundException("Role", 99L));

        mockMvc.perform(put("/roles/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteRole_returns200_onSuccess() throws Exception {
        doNothing().when(roleService).deleteRole(1L);

        mockMvc.perform(delete("/roles/1"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteRole_returns409_whenRoleHasUsers() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.CONFLICT,
                "Cannot delete role that is assigned to one or more users"))
                .when(roleService).deleteRole(1L);

        mockMvc.perform(delete("/roles/1"))
                .andExpect(status().isConflict());
    }
}
