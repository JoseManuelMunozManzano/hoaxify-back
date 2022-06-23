package com.jmunoz.hoaxify.configuration;

import com.jmunoz.hoaxify.user.User;
import com.jmunoz.hoaxify.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthUserService implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    // Este servicio será responsable de cargar userDetails de la BD.
    // Proporcionaremos una instancia de esta clase a Spring Security y se llamará a este método
    // cuando se reciba una petición con las credenciales del usuario
    //
    // Vamos a necesitar convertir el usuario al tipo UserDetails. Para hacer esto:
    // 1. Podemos crear un objeto que implemente UserDetails y establecer sus valores aquí usando los valores
    //    que vienen del objeto User.
    // 2. Implementar UserDetails directamente en nuestro User entity.
    //
    // Vamos a implementar la segunda opción.
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        return user;
    }
}
