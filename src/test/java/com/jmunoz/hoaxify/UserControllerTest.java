package com.jmunoz.hoaxify;

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
// @ActiveProfiles sirve para darnos la flexibilidad de configurar el comportamiento de la app en tiempo de ejecución
// Cuando ejecutamos los tests, queremos que se usen bases de datos de test, y no queremos usar nada de producción.
// Queremos que nuestros test se ejecuten en un entorno controlado.

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class UserControllerTest {

    public static final String API_1_0_USERS = "/api/1.0/users";

    @Autowired
    TestRestTemplate testRestTemplate;

    @Autowired
    UserRepository userRepository;

    // Cada ejecución debe correr en un entorno controlado, así que debe comenzar en un estado
    // conocido y volver a ese estado tras ejecutar cada uno de los tests.
    // Para eso se usan las anotaciones @BeforeEach y @AfterEach
    @BeforeEach
    void cleanup() {
        userRepository.deleteAll();
    }

    private User createValidUser() {
        User user = new User();

        // Definamos que es un usuario válido
        // username al menos de 4 caracteres
        // displayName al menos 4 caracteres
        // password con un carácter mayúsculas, uno minúsculas, un número y debe ser al menos de 8 caracteres
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

        // Decidimos que método http y que url vamos a usar.
        // Como vamos a hacer una petición para crear un objeto, debemos usar el método POST.
        //
        // Para la url usamos users, en plural, porque es conveniente usar plurales en Rest.
        // También es buena práctica añadir api y la versión del endpoint, ya que nuestra API evolucionará
        // con el tiempo y queremos que los clientes que usen versiones anteriores puedan seguir usándola.
        //
        // El segundo parámetro es el objeto que vamos a crear (el POST)
        //
        // El tercero es la respuesta que esperamos recoger. Por ahora no lo sabemos, solo estamos
        // interesados en saber si el resultado es 200 OK o no.
        ResponseEntity<Object> response = testRestTemplate.postForEntity(API_1_0_USERS, user, Object.class);

        // Comprobamos si recibimos lo que queremos
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Tras crear lo mínimo necesario, la clase UserController con el endpoint, ya vemos que se pasa el test.
    }

    @Test
    public void postUser_whenUserIsValid_userSavedToDatabase() {
        User user = createValidUser();
        testRestTemplate.postForEntity(API_1_0_USERS, user, Object.class);

        // Tenemos que confirmar si el usuario se ha almacenado en la BBDD
        // Para ello, ejecutaremos nuestra query usando un objeto repository que crearemos
        // cuando se ejecute el test y este falle (primero falla, después implementamos lo necesario)
        assertThat(userRepository.count()).isEqualTo(1);
    }
}
