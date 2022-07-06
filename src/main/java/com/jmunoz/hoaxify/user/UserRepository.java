package com.jmunoz.hoaxify.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);

    // Para usar UserProjection se necesita configurar la query manualmente porque ya existe un findAll en el
    // repository. Esto da error por eso.
    //Page<UserProjection> findAll(Pageable pageable);
    //
    // Le ponemos un nombre nuevo y hacemos el SQL manualmente. Se puede usar JPQL o SQL nativo.
    // Lo vamos a hacer con SQL nativo. Recodar que la tabla es Users porque H2 no permite User
    //@Query(value = "Select * from users", nativeQuery = true)
    // Ahora con JPQL
    @Query("Select u from Users u")
    Page<UserProjection> getAllUsersProjection(Pageable pageable);
}
