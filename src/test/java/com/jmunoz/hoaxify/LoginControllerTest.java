package com.jmunoz.hoaxify;

import com.jmunoz.hoaxify.error.ApiError;
import com.jmunoz.hoaxify.user.User;
import com.jmunoz.hoaxify.user.UserRepository;
import com.jmunoz.hoaxify.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class LoginControllerTest {

    private static final String API_1_0_LOGIN = "/api/1.0/login";

    @Autowired
    TestRestTemplate testRestTemplate;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        testRestTemplate.getRestTemplate().getInterceptors().clear();
    }

    public <T> ResponseEntity<T> login(Class<T> responseType) {
        return testRestTemplate.postForEntity(API_1_0_LOGIN, null, responseType);
    }

    public <T> ResponseEntity<T> login(ParameterizedTypeReference<T> responseType) {
        return testRestTemplate.exchange(API_1_0_LOGIN,  HttpMethod.POST,null, responseType);
    }

    // Actualizamos nuestro testRestTemplate para enviar una petición con BasicAuthentication
    // Usamos interceptores.
    // Como ahora mismo no tenemos usuarios en BD, cualquier usuario será incorrecto.
    private boolean authenticate() {
        return testRestTemplate
                .getRestTemplate().getInterceptors().add(new BasicAuthenticationInterceptor("test-user", "P4ssword"));
    }

    @Test
    void postLogin_withoutUserCredentials_receiveUnauthorized() {
        ResponseEntity<Object> response = login(Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void postLogin_witIncorrectCredentials_receiveUnauthorized() {
        authenticate();

        ResponseEntity<Object> response = login(Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void postLogin_withoutUserCredentials_receiveApiError() {
        ResponseEntity<ApiError> response = login(ApiError.class);
        assertThat(response.getBody().getUrl()).isEqualTo(API_1_0_LOGIN);
    }

    // Como estábamos usando ApiError para casos de errores de validaciones, recibimos
    // el campo validationErrors, y no lo queremos para login.
    @Test
    void postLogin_withoutUserCredentials_receiveApiErrorWithoutValidationErrors() {
        ResponseEntity<String> response = login(String.class);
        assertThat(response.getBody().contains("validationErrors")).isFalse();
    }

    // Cuando enviamos la respuesta 401, tenemos que tener cuidado con los headers enviados por nuestra app.
    // Si está el header WWW-Authenticate en la respuesta de 401, entonces el browser automáticamente hará saltar
    // su propio formulario de log in.
    // Tenemos que asegurarnos de no mandarlo.
    @Test
    void postLogin_witIncorrectCredentials_receiveUnauthorizedWithoutWWWAuthenticationHeader() {
        authenticate();

        ResponseEntity<Object> response = login(Object.class);
        assertThat(response.getHeaders().containsKey("WWW-Authenticate")).isFalse();
    }

    @Test
    void postLogin_withValidCredentials_receiveOk() {
        userService.save(TestUtil.createValidUser());
        authenticate();
        ResponseEntity<Object> response = login(Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void postLogin_withValidCredentials_receiveLoggedInUserId() {
        User inDb = userService.save(TestUtil.createValidUser());
        authenticate();

        // Como no tenemos una implementación definida de la repuesta vamos a ser más genéricos con
        // el tipo de respuesta que estamos esperando.
        // Pero no podemos proporcionar HashMap class como parámetro.
        // Por eso creamos un nuevo método login
        ResponseEntity<Map<String, Object>> response = login(new ParameterizedTypeReference<Map<String, Object>>() {});

        // Ahora obtenemos el cuerpo de la respuesta
        Map<String, Object> body = response.getBody();
        Integer id = (Integer) body.get("id");

        assertThat(id).isEqualTo(inDb.getId());
    }
}
