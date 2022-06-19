package com.jmunoz.hoaxify.user;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    // Query methods.
    // Se pueden crear muchas queries con convenciones de nombres.
    // Las palabras findBy y Containing son palabras clave para Spring Data y Username es el campo de nuestro
    // objeto User.
    // List<User> findByUsernameContaining(String username);
    // Se pueden combinar múltiples campos
    // User findByUsernameAndDisplayName(String username, String displayName);

    // Para saber si esto funciona correctamente, también se puede probar.
    // Ver UserRepositoryTest
    User findByUsername(String username);
}
