package com.jmunoz.hoaxify.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);

    // Devolvemos todos los usuarios salvo el que hizo login
    Page<User> findByUsernameNot(String username, Pageable page);
}
