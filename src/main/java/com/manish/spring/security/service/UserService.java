package com.manish.spring.security.service;

import com.manish.spring.security.Entity.User;
import com.manish.spring.security.Repository.OrderRepository;
import com.manish.spring.security.Repository.UserRepository;
import com.manish.spring.security.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public User createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    public User updateUser(Long id, User user) {
        User existing = getUser(id);
        existing.setFirstName(user.getFirstName());
        existing.setLastName(user.getLastName());
        existing.setEmail(user.getEmail());
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            existing.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        existing.setRole(user.getRole());
        return userRepository.save(existing);
    }

    public void deleteUser(Long id) {
        getUser(id);
        if (!orderRepository.findByUserId(id).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot delete user with associated orders");
        }
        userRepository.deleteById(id);
    }
}
