package com.jmunoz.hoaxify.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    UserRepository userRepository;

    // Sustituimos BCryptPasswordEncoder por PasswordEncoder genérico e inyectamos en el constructor
    // nuestro passwordEncoder
    PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User save(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Page<?> getUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        // En vez de usar findAll se va a usar nuestra SQL nativa que guarda datos en nuestra interface UserProjection
        return userRepository.getAllUsersProjection(pageable);
    }
}
