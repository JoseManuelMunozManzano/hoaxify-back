package com.jmunoz.hoaxify.user;

import com.jmunoz.hoaxify.error.NotFoundException;
import com.jmunoz.hoaxify.user.vm.UserUpdateVM;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

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

    public User update(long id, UserUpdateVM userUpdate) {
        User inDB = userRepository.getReferenceById(id);
        inDB.setDisplayName(userUpdate.getDisplayName());

        // Todavía no estamos guardando la imagen. Estamos proporcionando la imagen como una cadena.
        // Podríamos haberla guardado directamente en BD pero no se hará eso.
        // Se almacenará la imagen como fichero en un almacenamiento.
        String savedImageName = inDB.getUsername() + UUID.randomUUID().toString().replaceAll("-", "");
        inDB.setImage(savedImageName);

        return userRepository.save(inDB);
    }
}
