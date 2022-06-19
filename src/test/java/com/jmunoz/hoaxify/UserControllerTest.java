package com.jmunoz.hoaxify;

import com.jmunoz.hoaxify.error.ApiError;
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
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;

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

    public <T> ResponseEntity<T> postSignup(Object request, Class<T> response) {
        return testRestTemplate.postForEntity(API_1_0_USERS, request, response);
    }

    // Para los nombres de los tests se va a usar el esquema siguiente:
    // methodName_condition_expectedBehavior
    @Test
    public void postUser_whenUserIsValid_receiveOk() {
        User user = createValidUser();
        ResponseEntity<Object> response = postSignup(user, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void postUser_whenUserIsValid_userSavedToDatabase() {
        User user = createValidUser();
        postSignup(user, Object.class);
        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    public void postUser_whenUserIsValid_receiveSuccessMessage() {
        User user = createValidUser();
        ResponseEntity<GenericResponse> response = postSignup(user, GenericResponse.class);
        assertThat(response.getBody().getMessage()).isNotNull();
    }

    @Test
    void postUser_whenUserIsValid_passwordIsHashedInDatabase() {
        User user = createValidUser();
        postSignup(user, Object.class);

        List<User> users = userRepository.findAll();
        User indB = users.get(0);

        assertThat(indB.getPassword()).isNotEqualTo(user.getPassword());
    }

    @Test
    void postUser_whenUserHasNullUsername_receiveBadRequest() {
        User user = createValidUser();
        user.setUsername(null);
        ResponseEntity<Object> response = postSignup(user, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void postUser_whenUserHasNullDisplayName_receiveBadRequest() {
        User user = createValidUser();
        user.setDisplayName(null);
        ResponseEntity<Object> response = postSignup(user, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void postUser_whenUserHasNullPassword_receiveBadRequest() {
        User user = createValidUser();
        user.setPassword(null);
        ResponseEntity<Object> response = postSignup(user, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void postUser_whenUserHasUsernameWithLessThanRequired_receiveBadRequest() {
        User user = createValidUser();
        // El mínimo deben ser 4 caracteres
        user.setUsername("abc");
        ResponseEntity<Object> response = postSignup(user, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void postUser_whenUserHasDisplayNameWithLessThanRequired_receiveBadRequest() {
        User user = createValidUser();
        // El mínimo deben ser 4 caracteres
        user.setDisplayName("abc");
        ResponseEntity<Object> response = postSignup(user, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void postUser_whenUserHasPasswordWithLessThanRequired_receiveBadRequest() {
        User user = createValidUser();
        // El mínimo deben ser 8 caracteres, con mayúsculas, minúsculas y números
        user.setPassword("P4sswd");
        ResponseEntity<Object> response = postSignup(user, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void postUser_whenUserHasUsernameExceedsTheLengthLimit_receiveBadRequest() {
        User user = createValidUser();
        // El máximo son 255 caracteres
        // Como estamos generando 256 caracteres va a dar error 500, no 404, por lo que el test falla.
        // Una vez corregido en la clase User indicando que el máximo es 255 caracteres, no pasa la
        // validación, y eso es un error 404, con lo que pasa el test.
        String valueOf256Chars = IntStream.rangeClosed(1, 256).mapToObj(x -> "a").collect(Collectors.joining());
        user.setUsername(valueOf256Chars);
        ResponseEntity<Object> response = postSignup(user, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void postUser_whenUserHasDisplayNameExceedsTheLengthLimit_receiveBadRequest() {
        User user = createValidUser();
        String valueOf256Chars = IntStream.rangeClosed(1, 256).mapToObj(x -> "a").collect(Collectors.joining());
        user.setDisplayName(valueOf256Chars);
        ResponseEntity<Object> response = postSignup(user, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void postUser_whenUserHasPasswordExceedsTheLengthLimit_receiveBadRequest() {
        User user = createValidUser();
        String valueOf256Chars = IntStream.rangeClosed(1, 256).mapToObj(x -> "a").collect(Collectors.joining());
        // Se añade A1 porque el password requiere al menos una mayúscula y un número
        user.setPassword(valueOf256Chars + "A1");
        ResponseEntity<Object> response = postSignup(user, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void postUser_whenUserHasPasswordWithAllLowercase_receiveBadRequest() {
        User user = createValidUser();
        user.setPassword("alllowercase");
        ResponseEntity<Object> response = postSignup(user, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void postUser_whenUserHasPasswordWithAllUppercase_receiveBadRequest() {
        User user = createValidUser();
        user.setPassword("ALLUPPERCASE");
        ResponseEntity<Object> response = postSignup(user, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void postUser_whenUserHasPasswordWithAllNumbers_receiveBadRequest() {
        User user = createValidUser();
        user.setPassword("123456789");
        ResponseEntity<Object> response = postSignup(user, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void postUser_whenUserIsInvalid_receiveApiError() {
        User user = new User();
        ResponseEntity<ApiError> response = postSignup(user, ApiError.class);
        assertThat(response.getBody().getUrl()).isEqualTo(API_1_0_USERS);
    }

    @Test
    void postUser_whenUserIsInvalid_receiveApiErrorWithValidationErrors() {
        User user = new User();
        ResponseEntity<ApiError> response = postSignup(user, ApiError.class);
        assertThat(response.getBody().getValidationErrors().size()).isEqualTo(3);
    }

    @Test
    void postUser_whenUserHasNullUsername_receiveMessageOfNullErrorForUsername() {
        User user = createValidUser();
        user.setUsername(null);
        ResponseEntity<ApiError> response = postSignup(user, ApiError.class);
        Map<String, String> validationErrors = response.getBody().getValidationErrors();
        // Esto da error porque los mensajes de error de Spring validation están en distintos idiomas.
        // Internacionalización.
        // Para que el test no falle hay distintas soluciones:
        // 1. Hacer override del error en la clase User, indicando el texto que queremos enviar en el error.
        //    No es ideal porque es un valor hardcode que nos hace perder la posibilidad de internacionalización.
        //    Para ver este error, en Postman, en la cabecera indicar:
        //    Accept-Language    y el valor     tr
        //    Ahora los mensajes aparecen en turco, salvo el nuestro que sale en inglés con el texto que hemos puesto.
        //    Si solo queremos hacer la app en un idioma, entonces si valdría.
        //
        // 2. Llevarnos el texto con el error a un fichero de properties separado en vez de establecerlo directamente
        //    en la anotación @NotNull.
        //    Creamos en resources el fichero (el nombre es obligatorio) ValidationMessages.properties
        //    Esta solución conlleva un problema. Estamos sobreescribiendo el mensaje NotNull con el valor añadido
        //    en nuestro fichero properties de validaciones. Pero esto es PARA TODOS LOS CAMPOS NOT NULL.
        //    Es decir, si el Password es Null también aparece ese mensaje indicando que el usuario no puede ser nulo.
        //    Si optamos por esta solución, tenemos que ser muy genéricos con los mensajes.
        //
        // 3. Mixto. Indicamos en el fichero de properties de mensajes el nombre indicando NotNull.message
        //    con el texto concreto, y en la clase, en la anotación @NotNull indicamos que coja esa property.
        //    Podemos tener distintos ficheros ValidationMessages_en.properties para mensajes en español,
        //    o ValidationMessages_tr.properties para mensajes en turco y así tenemos la internacionalización
        //    de los mensajes en la app.
        //    Si probamos en postman con un lenguaje para el que no tenemos traducción nos saldrá el del fichero
        //    por defecto (ValidationMessages.properties de Spring, no el nuestro!)
        assertThat(validationErrors.get("username")).isEqualTo("Username no puede ser nulo");
    }

    @Test
    void postUser_whenUserHasNullPassword_receiveGenericMessageOfNullError() {
        User user = createValidUser();
        user.setPassword(null);
        ResponseEntity<ApiError> response = postSignup(user, ApiError.class);
        Map<String, String> validationErrors = response.getBody().getValidationErrors();
        assertThat(validationErrors.get("password")).isEqualTo("No puede ser nulo");
    }

    @Test
    void postUser_whenUserHasInvalidLengthUsername_receiveGenericMessageOfSizeError() {
        User user = createValidUser();
        user.setUsername("abc");
        ResponseEntity<ApiError> response = postSignup(user, ApiError.class);
        Map<String, String> validationErrors = response.getBody().getValidationErrors();
        assertThat(validationErrors.get("username")).isEqualTo("Debe estar entre el mínimo 4 y el máximo 255 de caracteres");
    }

    @Test
    void postUser_whenUserHasInvalidPasswordPattern_receiveMessageOfPasswordPatternError() {
        User user = createValidUser();
        user.setPassword("alllowercase");
        ResponseEntity<ApiError> response = postSignup(user, ApiError.class);
        Map<String, String> validationErrors = response.getBody().getValidationErrors();
        // Para patrones, el mensaje por defecto es la expresión regular que hemos puesto
        assertThat(validationErrors.get("password"))
                .isEqualTo("El password debe tener al menos una letra mayúscula, una letra minúscula y un número");
    }

    @Test
    void postUser_whenAnotherUserHasSameUsername_receiveBadRequest() {
        userRepository.save(createValidUser());

        User user = createValidUser();
        ResponseEntity<Object> response = postSignup(user, Object.class);

        // Ahora mismo no hay nada que evite que se grabe el mismo usuario 2 veces.
        // Formas de solucionar el problema:
        // 1. Establecer esta restricción a nivel de BD. Ver User.java
        //    Esto devuelve un error 500, no el definido en el test (404), porque no hemos definido una manera de
        //    manejar esta excepción, y entonces Spring automáticamente mapea esta respuesta al status 500.
        //    Para solucionar esto se puede añadir una exception handler y generar nuestro propio error customizado
        //    con el status code que queramos.
        //    Pero no vamos a coger esta solución 1.
        //    Esto sería muy útil cuando muchas aplicaciones deben acceder a la misma BD. En este caso, la integridad
        //    de la BD podría ser un gran problema y para salvar ese problema debemos manejar las restricciones en BD.
        // 2. Vamos a manejar las restricciones con código en la app. Ver UserService.java y UserRepository.java
        //    En vez de un custom exception handler, vamos a crear una custom annotation para tratar las validaciones
        //    de User en el mismo sitio.
        //    Vemos que falla porque hay un detalle importante en el comportamiento de las validaciones.
        //    Por defecto, los validadores se usan 2 veces.
        //    Una, decimos a Spring que valide antes de pasar al controlador.
        //    Segunda, valida Hibernate antes de que se persista la información en BD.
        //    En nuestro UniqueUsernameValidator le estamos indicando a Spring que inyecte userRepository.
        //    Esto está bien cuando la primera validación la hace Spring.
        //    Pero cuando llega la segunda validación, la que hace Hibernate, él creará la instancia de esta clase,
        //    y no puede inyectar userRepository. Como no hay instancia, no se pueden ejecutar métodos definidos por
        //    el usuario y se lanza el NullPointerException.
        //    Solución:
        //    Como estamos manejando la validación en la aplicación y no tenemos constraints en BD, vamos a deshabilitar
        //    la validación Hibernate. Ver application.yml
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void postUser_whenAnotherUserHasSameUsername_receiveMessageOfDuplicateUsername() {
        userRepository.save(createValidUser());

        User user = createValidUser();
        ResponseEntity<ApiError> response = postSignup(user, ApiError.class);
        Map<String, String> validationErrors = response.getBody().getValidationErrors();
        assertThat(validationErrors.get("username")).isEqualTo("Este nombre se está usando");
    }
}
