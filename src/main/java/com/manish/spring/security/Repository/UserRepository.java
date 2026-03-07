package com.manish.spring.security.Repository;

import com.manish.spring.security.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Useful for login (Spring Security / JWT)
    Optional<User> findByEmail(String email);

}