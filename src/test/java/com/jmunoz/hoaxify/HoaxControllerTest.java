package com.jmunoz.hoaxify;

import com.jmunoz.hoaxify.error.ApiError;
import com.jmunoz.hoaxify.hoax.Hoax;
import com.jmunoz.hoaxify.hoax.HoaxRepository;
import com.jmunoz.hoaxify.user.User;
import com.jmunoz.hoaxify.user.UserRepository;
import com.jmunoz.hoaxify.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class HoaxControllerTest {

    public static final String API_1_0_HOAXES = "/api/1.0/hoaxes";

    @Autowired
    TestRestTemplate testRestTemplate;

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    HoaxRepository hoaxRepository;

    @BeforeEach
    void setUp() {
        hoaxRepository.deleteAll();
        userRepository.deleteAll();
        testRestTemplate.getRestTemplate().getInterceptors().clear();
    }

    private boolean authenticate(String username) {
        return testRestTemplate
                .getRestTemplate().getInterceptors().add(new BasicAuthenticationInterceptor(username, "P4ssword"));
    }

    private <T> ResponseEntity<T> postHoax(Hoax hoax, Class<T> responseType) {
        return testRestTemplate.postForEntity(API_1_0_HOAXES, hoax, responseType);
    }

    @Test
    void postHoax_whenHoaxIsValidAndUserIsAuthorized_receiveOk() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");

        Hoax hoax = TestUtil.createValidHoax();
        ResponseEntity<Object> response = postHoax(hoax, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void postHoax_whenHoaxIsValidAndUserIsUnauthorized_receiveUnauthorized() {
        Hoax hoax = TestUtil.createValidHoax();
        ResponseEntity<Object> response = postHoax(hoax, Object.class);

        // Para que pase el test añadimos en SecurityConfiguration en antMatcher correspondiente
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void postHoax_whenHoaxIsValidAndUserIsUnauthorized_receiveApiError() {
        Hoax hoax = TestUtil.createValidHoax();
        ResponseEntity<ApiError> response = postHoax(hoax, ApiError.class);

        // No hay que hacer nada para que pase este test ya que el error se está manejando en nuestro
        // ErrorHandler (ver package error, clase ErrorHandler.java) genérico, que mapea los fallos a ApiError
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void postHoax_whenHoaxIsValidAndUserIsAuthorized_hoaxSavedToDatabase() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");

        Hoax hoax = TestUtil.createValidHoax();
        postHoax(hoax, Object.class);

        assertThat(hoaxRepository.count()).isEqualTo(1);
    }

    @Test
    void postHoax_whenHoaxIsValidAndUserIsAuthorized_hoaxSavedToDatabaseWithTimestamp() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Hoax hoax = TestUtil.createValidHoax();
        postHoax(hoax, Object.class);

        Hoax inDB = hoaxRepository.findAll().get(0);

        assertThat(inDB.getTimestamp()).isNotNull();
    }

    @Test
    void postHoax_whenHoaxContentNullAndUserIsAuthorized_receiveBadRequest() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");

        Hoax hoax = new Hoax();
        ResponseEntity<Object> response = postHoax(hoax, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void postHoax_whenHoaxContentLessThan10CharactersAndUserIsAuthorized_receiveBadRequest() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");

        Hoax hoax = new Hoax();
        hoax.setContent("123456789");
        ResponseEntity<Object> response = postHoax(hoax, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void postHoax_whenHoaxContentIs5000CharactersAndUserIsAuthorized_receiveOk() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");

        Hoax hoax = new Hoax();
        String veryLongString = IntStream.rangeClosed(1, 5000).mapToObj(i -> "x").collect(Collectors.joining());
        hoax.setContent(veryLongString);
        ResponseEntity<Object> response = postHoax(hoax, Object.class);

        // Si no se indica nada, la longitud máxima de un String en la BD es de 255 caracteres.
        // Para corregirlo se indicará la longitud de la columna content en la clase Hoax.java
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void postHoax_whenHoaxContentMoreThan5000CharactersAndUserIsAuthorized_receiveBadRequest() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");

        Hoax hoax = new Hoax();
        String veryLongString = IntStream.rangeClosed(1, 5001).mapToObj(i -> "x").collect(Collectors.joining());
        hoax.setContent(veryLongString);
        ResponseEntity<Object> response = postHoax(hoax, Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void postHoax_whenHoaxContentNullAndUserIsAuthorized_receiveApiErrorWithValidationErrors() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");

        // content es null
        Hoax hoax = new Hoax();
        ResponseEntity<ApiError> response = postHoax(hoax, ApiError.class);
        Map<String, String> validationErrors = response.getBody().getValidationErrors();

        // Para que pase el test de una manera "sucia", vamos a copiar de la clase UserController
        // el método handleValidationException en la clase HoaxController.
        // El problema es que ahora tenemos código duplicado, y si en el futuro se quisiera cambiar
        // este comportamiento, habría que modificar ambas clases.
        // Además, si añadimos un controlador nuevo para otro endpoint, vamos a necesitar introducir
        // el mismo comportamiento.
        //
        // Por tanto, lo que se va a hacer es poner la lógica en un lugar común.
        // En el package shared vamos a crear la clase ExceptionHandlerAdvice donde recogeremos
        // todos los ExceptionHandler y estos se aplicarán a todos los @RestController
        assertThat(validationErrors.get("content")).isNotNull();
    }

    @Test
    void postHoax_whenHoaxIsValidAndUserIsAuthorized_hoaxSavedWithAuthenticatedUserInfo() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Hoax hoax = TestUtil.createValidHoax();
        postHoax(hoax, Object.class);

        Hoax inDB = hoaxRepository.findAll().get(0);

        assertThat(inDB.getUser().getUsername()).isEqualTo("user1");
    }

    @Test
    void postHoax_whenHoaxIsValidAndUserIsAuthorized_hoaxCanBeAccessedFromUserEntity() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Hoax hoax = TestUtil.createValidHoax();
        postHoax(hoax, Object.class);

        User inDBUser = userRepository.findByUsername("user1");

        // Un hoax puede tener 1 usuario, pero un usuario puede postear muchos hoaxes
        // Cuando añadimos la lista de hoaxes en la tabla User fallan todos los tests.
        // Cómo se puede almacenar una lista en una llamada? Hibernate no sabe como convertir
        // esos datos en una columna con valor.
        //
        // Tras informar las relaciones entre entidades, sigue fallando solo este test con el error
        // LazyInitializationException.
        // Cuando se carga el objeto User únicamente carga ese objeto. Esto se hace por rendimiento.
        // Esto falla solo en el test, para nuestra query cuando se obtienen los hoaxes.
        // La ejecución de nuestra app no daría este error, al menos por ahora.
        assertThat(inDBUser.getHoaxes().size()).isEqualTo(1);
    }
}
