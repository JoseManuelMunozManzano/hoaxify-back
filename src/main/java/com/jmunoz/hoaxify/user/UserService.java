package com.jmunoz.hoaxify.user;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    UserRepository userRepository;

    // Para la seguridad vamos a usar bcrypt
    BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public User save(User user) {
        // verificar si tenemos el usuario en BD con este username
        // También se podría añadir un manejador de esta excepción para esta excepción y convertir en objeto en un ApiError.
        // Pero no lo vamos a hacer porque esta solución no es la ideal, ya que con esta implementación estamos añadiendo
        // la lógica de validación en nuestro UserService.class, con lo que esta clase estaría haciendo más que nuestros
        // requerimientos de negocio.
        // Ya estamos delegando este proceso de validación a Spring para @NotNull, @Size y @Pattern, pero la validación
        // de la unicidad del username se está haciendo aquí.
        // Estamos dividiendo en distintos sitios las validaciones.
        User inDB = userRepository.findByUsername(user.getUsername());
        if (inDB != null) {
            throw new DuplicateUsernameException();
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

}
