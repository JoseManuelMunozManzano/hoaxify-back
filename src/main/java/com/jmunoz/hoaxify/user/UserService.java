package com.jmunoz.hoaxify.user;

import org.springframework.stereotype.Service;

@Service
public class UserService {

    UserRepository userRepository;

    // Autowiring usando inyección en el constructor
    // La ventaja de usar este tipo de inyección sobre inyección en la declaración de la variable, es que si decidimos
    // escribir unit tests para UserService, podemos crear una instancia de la clase nosotros mismos y
    // pasarle un mock de UserRepository al constructor, así que podemos controlar las dependencias externas de
    // esta clase.
    // Por tanto, la inyección en el constructor hace la clase más fácil de probar.
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User save(User user) {
        return userRepository.save(user);
    }

}
