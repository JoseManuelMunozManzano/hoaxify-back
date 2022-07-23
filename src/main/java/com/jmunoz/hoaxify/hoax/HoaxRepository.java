package com.jmunoz.hoaxify.hoax;

import com.jmunoz.hoaxify.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HoaxRepository extends JpaRepository<Hoax, Long> {

    // Cargamos los hoaxes para un usuario específico
    Page<Hoax> findByUser(User user, Pageable pageable);

    // Cuando se crean método JPA, podemos añadir un campo de objetos anidados a la definición del método.
    // Pero no se va a usar este
    // Page<Hoax> findByUserUsername(String username, Pageable pageable);
}
