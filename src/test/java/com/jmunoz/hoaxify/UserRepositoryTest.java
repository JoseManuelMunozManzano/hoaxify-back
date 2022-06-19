package com.jmunoz.hoaxify;

import com.jmunoz.hoaxify.user.User;
import com.jmunoz.hoaxify.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;

// @DataJpaTest
// Cuando se prueba un repository, esta anotación dice a la app que se inicialice de una forma customizada,
// haciendo que la inicialización de los tests sea un poco más rápida.
// Además, viene con una BD embebida en memoria, así que es como un entorno de app customizado solo para JPA.
@DataJpaTest
@ActiveProfiles("test")
public class UserRepositoryTest {

    // Proporciona una alternativa al standard JPA, específicamente para testing.
    @Autowired
    TestEntityManager testEntityManager;

    @Autowired
    UserRepository userRepository;

    @Test
    public void findByUsername_whenUserExists_returnsUser() {
        User user = new User();

        user.setUsername("test-user");
        user.setDisplayName("test-display");
        user.setPassword("P4ssword");

        // Guardamos el usuario en nuestro contexto de persistencia de test.
        testEntityManager.persist(user);

        // Ahora vemos si lo encuentra
        User inDB = userRepository.findByUsername("test-user");
        assertThat(inDB).isNotNull();
    }

    // No hace falta @BeforeEach para limpiar la BD porque DataJpa test provee de una limpieza de BD en cada método
    // de forma automática
    @Test
    public void findByUsername_whenUserDoesNotExists_returnsNull() {
        User inDB = userRepository.findByUsername("nonexistinguser");
        assertThat(inDB).isNull();
    }

}
