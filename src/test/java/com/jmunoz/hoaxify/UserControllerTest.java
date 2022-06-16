package com.jmunoz.hoaxify;

import com.jmunoz.hoaxify.shared.GenericResponse;
import com.jmunoz.hoaxify.user.User;
import com.jmunoz.hoaxify.user.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.*;

// Test de integración

// Solo se hace una assertion en cada test, porque aunque esto signifique escribir más, también, más tarde
// permite que el tiempo dedicado al mantenimiento de los tests sea menor.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class UserControllerTest {

    public static final String API_1_0_USERS = "/api/1.0/users";

    @Autowired
    TestRestTemplate testRestTemplate;

    @Autowired
    UserRepository userRepository;

    @BeforeEach
    void cleanup() {
        userRepository.deleteAll();
    }

    private User createValidUser() {
        User user = new User();

        user.setUsername("test-user");
        user.setDisplayName("test-display");
        user.setPassword("P4ssword");

        return user;
    }

    // Para los nombres de los tests se va a usar el esquema siguiente:
    // methodName_condition_expectedBehavior
    @Test
    public void postUser_whenUserIsValid_receiveOk() {
        User user = createValidUser();
        ResponseEntity<Object> response = testRestTemplate.postForEntity(API_1_0_USERS, user, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void postUser_whenUserIsValid_userSavedToDatabase() {
        User user = createValidUser();
        testRestTemplate.postForEntity(API_1_0_USERS, user, Object.class);
        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    public void postUser_whenUserIsValid_receiveSuccessMessage() {
        User user = createValidUser();
        ResponseEntity<GenericResponse> response = testRestTemplate.postForEntity(API_1_0_USERS, user, GenericResponse.class);
        assertThat(response.getBody().getMessage()).isNotNull();
    }

    @Test
    void postUser_whenUserIsValid_passwordIsHashedInDatabase() {
        User user = createValidUser();
        testRestTemplate.postForEntity(API_1_0_USERS, user, Object.class);

        List<User> users = userRepository.findAll();
        User indB = users.get(0);

        // Tras incluir bcrypt para encriptar nuestro password, nuestros test fallan (las operaciones post)
        // porque Spring Boot está haciendo la autoconfiguración, y para Spring Security, la configuración
        // por defecto consiste en dar seguridad a todos los endpoints, lo que significa que todas
        // las peticiones de autorización son procesadas, y como en nuestro test no estamos indicando
        // ninguna autenticación (no headers en la petición) los test fallan.
        //
        // En este punto del desarrollo, solo se va a usar encriptación lógica, no se va a implementar todavía
        // la seguridad.
        // Es por eso que deshabilitamos la autoconfiguración (ver HoaxifyApplication.java)
        //
        // Si ahora ejecutamos los tests ya si funcionan.
        assertThat(indB.getPassword()).isNotEqualTo(user.getPassword());
    }
}
