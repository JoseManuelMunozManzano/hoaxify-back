package com.jmunoz.hoaxify.user;

import com.jmunoz.hoaxify.error.NotFoundException;
import com.jmunoz.hoaxify.user.vm.UserUpdateVM;
import org.springframework.data.domain.Page;
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
        super();
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User save(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public Page<User> getUsers(User loggedInUser, Pageable pageable) {
        if (loggedInUser != null) {
            return userRepository.findByUsernameNot(loggedInUser.getUsername(), pageable);
        }
        return userRepository.findAll(pageable);
    }

    public User getByUsername(String username) {
        User inDB = userRepository.findByUsername(username);
        if (inDB == null) {
            throw new NotFoundException(username + " not found");
        }

        return inDB;
    }

    public void update(long id, UserUpdateVM userUpdate) {
        // No se usa definedById porque devuelve un Optional User.
        // No hace falta ese control porque sabemos que el request viene de un usuario autorizado que
        // existe en BD y cuyo ID es el usuario actualmente registrado. Por tanto, el id existe en BD.
        // En otro caso, esto fallaría en los controles de seguridad.
        User inDB = userRepository.getReferenceById(id);
        inDB.setDisplayName(userUpdate.getDisplayName());
        userRepository.save(inDB);
    }
}
