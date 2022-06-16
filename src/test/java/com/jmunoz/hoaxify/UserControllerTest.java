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

import static org.assertj.core.api.AssertionsForClassTypes.*;

// Test de integración

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

        // Esperamos recibir como respuesta un GenericResponse, que todavía no existe, y lo creamos.
        ResponseEntity<GenericResponse> response = testRestTemplate.postForEntity(API_1_0_USERS, user, GenericResponse.class);

        // De la response obtenemos el body y en concreto el mensaje
        assertThat(response.getBody().getMessage()).isNotNull();

    }
}
