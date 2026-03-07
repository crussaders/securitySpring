package com.manish.spring.security.service;

import com.manish.spring.security.Entity.Role;
import com.manish.spring.security.Repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public Role getRole(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));
    }

    public Role createRole(Role role) {
        return roleRepository.save(role);
    }

    public Role updateRole(Long id, Role role) {
        Role existing = getRole(id);
        existing.setRoleName(role.getRoleName());
        return roleRepository.save(existing);
    }

    @Transactional
    public void deleteRole(Long id) {
        Role role = getRole(id);

        if (role.getUsers() != null && !role.getUsers().isEmpty()) {
            throw new IllegalStateException("Cannot delete role that is assigned to one or more users");
        }

        roleRepository.delete(role);
    }
}
