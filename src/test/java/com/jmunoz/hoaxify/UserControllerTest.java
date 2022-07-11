package com.jmunoz.hoaxify;

import com.jmunoz.hoaxify.error.ApiError;
import com.jmunoz.hoaxify.shared.GenericResponse;
import com.jmunoz.hoaxify.user.User;
import com.jmunoz.hoaxify.user.UserRepository;
import com.jmunoz.hoaxify.user.UserService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
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

    // Para autenticar el usuario
    @Autowired
    UserService userService;

    @BeforeEach
    void cleanup() {
        userRepository.deleteAll();
        testRestTemplate.getRestTemplate().getInterceptors().clear();
    }

    public <T> ResponseEntity<T> postSignup(Object request, Class<T> response) {
        return testRestTemplate.postForEntity(API_1_0_USERS, request, response);
    }

    public <T> ResponseEntity<T> getUsers(ParameterizedTypeReference<T> responseType) {
        return testRestTemplate.exchange(API_1_0_USERS, HttpMethod.GET, null, responseType);
    }

    public <T> ResponseEntity<T> getUsers(String path, ParameterizedTypeReference<T> responseType) {
        return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
    }

    public <T> ResponseEntity<T> getUser(String username, Class<T> responseType) {
        String path = API_1_0_USERS + "/" + username;
        return testRestTemplate.getForEntity(path, responseType);
    }

    public <T> ResponseEntity<T> putUser(long id, HttpEntity<?> requestEntity, Class<T> responseType) {
        String path = API_1_0_USERS + "/" + id;
        return testRestTemplate.exchange(path, HttpMethod.PUT, requestEntity, responseType);
    }

    private boolean authenticate(String username) {
        return testRestTemplate
                .getRestTemplate().getInterceptors().add(new BasicAuthenticationInterceptor(username, "P4ssword"));
    }

    // Para los nombres de los tests se va a usar el esquema siguiente:
    // methodName_condition_expectedBehavior
    @Test
    public void postUser_whenUserIsValid_receiveOk() {
        User user = TestUtil.createValidUser();
        ResponseEntity<Object> response = postSignup(user, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void postUser_whenUserIsValid_userSavedToDatabase() {
        User user = TestUtil.createValidUser();
        postSignup(user, Object.class);
        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    public void postUser_whenUserIsValid_receiveSuccessMessage() {
        User user = TestUtil.createValidUser();
        ResponseEntity<GenericResponse> response = postSignup(user, GenericResponse.class);
        assertThat(response.getBody().getMessage()).isNotNull();
    }

    @Test
    void postUser_whenUserIsValid_passwordIsHashedInDatabase() {
        User user = TestUtil.createValidUser();
        postSignup(user, Object.class);

        List<User> users = userRepository.findAll();
        User indB = users.get(0);

        assertThat(indB.getPassword()).isNotEqualTo(user.getPassword());
    }

    @Test
    void postUser_whenUserHasNullUsername_receiveBadRequest() {
        User user = TestUtil.createValidUser();
        user.setUsername(null);
        ResponseEntity<Object> response = postSignup(user, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void postUser_whenUserHasNullDisplayName_receiveBadRequest() {
        User user = TestUtil.createValidUser();
        user.setDisplayName(null);
        ResponseEntity<Object> response = postSignup(user, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void postUser_whenUserHasNullPassword_receiveBadRequest() {
        User user = TestUtil.createValidUser();
        user.setPassword(null);
        ResponseEntity<Object> response = postSignup(user, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void postUser_whenUserHasUsernameWithLessThanRequired_receiveBadRequest() {
        User user = TestUtil.createValidUser();
        // El mínimo deben ser 4 caracteres
        user.setUsername("abc");
        ResponseEntity<Object> response = postSignup(user, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void postUser_whenUserHasDisplayNameWithLessThanRequired_receiveBadRequest() {
        User user = TestUtil.createValidUser();
        // El mínimo deben ser 4 caracteres
        user.setDisplayName("abc");
        ResponseEntity<Object> response = postSignup(user, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void postUser_whenUserHasPasswordWithLessThanRequired_receiveBadRequest() {
        User user = TestUtil.createValidUser();
        // El mínimo deben ser 8 caracteres, con mayúsculas, minúsculas y números
        user.setPassword("P4sswd");
        ResponseEntity<Object> response = postSignup(user, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void postUser_whenUserHasUsernameExceedsTheLengthLimit_receiveBadRequest() {
        User user = TestUtil.createValidUser();
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
        User user = TestUtil.createValidUser();
        String valueOf256Chars = IntStream.rangeClosed(1, 256).mapToObj(x -> "a").collect(Collectors.joining());
        user.setDisplayName(valueOf256Chars);
        ResponseEntity<Object> response = postSignup(user, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void postUser_whenUserHasPasswordExceedsTheLengthLimit_receiveBadRequest() {
        User user = TestUtil.createValidUser();
        String valueOf256Chars = IntStream.rangeClosed(1, 256).mapToObj(x -> "a").collect(Collectors.joining());
        // Se añade A1 porque el password requiere al menos una mayúscula y un número
        user.setPassword(valueOf256Chars + "A1");
        ResponseEntity<Object> response = postSignup(user, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void postUser_whenUserHasPasswordWithAllLowercase_receiveBadRequest() {
        User user = TestUtil.createValidUser();
        user.setPassword("alllowercase");
        ResponseEntity<Object> response = postSignup(user, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void postUser_whenUserHasPasswordWithAllUppercase_receiveBadRequest() {
        User user = TestUtil.createValidUser();
        user.setPassword("ALLUPPERCASE");
        ResponseEntity<Object> response = postSignup(user, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void postUser_whenUserHasPasswordWithAllNumbers_receiveBadRequest() {
        User user = TestUtil.createValidUser();
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
        User user = TestUtil.createValidUser();
        user.setUsername(null);
        ResponseEntity<ApiError> response = postSignup(user, ApiError.class);
        Map<String, String> validationErrors = response.getBody().getValidationErrors();
        
        assertThat(validationErrors.get("username")).isEqualTo("Username no puede ser nulo");
    }

    @Test
    void postUser_whenUserHasNullPassword_receiveGenericMessageOfNullError() {
        User user = TestUtil.createValidUser();
        user.setPassword(null);
        ResponseEntity<ApiError> response = postSignup(user, ApiError.class);
        Map<String, String> validationErrors = response.getBody().getValidationErrors();
        assertThat(validationErrors.get("password")).isEqualTo("No puede ser nulo");
    }

    @Test
    void postUser_whenUserHasInvalidLengthUsername_receiveGenericMessageOfSizeError() {
        User user = TestUtil.createValidUser();
        user.setUsername("abc");
        ResponseEntity<ApiError> response = postSignup(user, ApiError.class);
        Map<String, String> validationErrors = response.getBody().getValidationErrors();
        assertThat(validationErrors.get("username")).isEqualTo("Debe estar entre el mínimo 4 y el máximo 255 de caracteres");
    }

    @Test
    void postUser_whenUserHasInvalidPasswordPattern_receiveMessageOfPasswordPatternError() {
        User user = TestUtil.createValidUser();
        user.setPassword("alllowercase");
        ResponseEntity<ApiError> response = postSignup(user, ApiError.class);
        Map<String, String> validationErrors = response.getBody().getValidationErrors();
        // Para patrones, el mensaje por defecto es la expresión regular que hemos puesto
        assertThat(validationErrors.get("password"))
                .isEqualTo("El password debe tener al menos una letra mayúscula, una letra minúscula y un número");
    }

    @Test
    void postUser_whenAnotherUserHasSameUsername_receiveBadRequest() {
        userRepository.save(TestUtil.createValidUser());

        User user = TestUtil.createValidUser();
        ResponseEntity<Object> response = postSignup(user, Object.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void postUser_whenAnotherUserHasSameUsername_receiveMessageOfDuplicateUsername() {
        userRepository.save(TestUtil.createValidUser());

        User user = TestUtil.createValidUser();
        ResponseEntity<ApiError> response = postSignup(user, ApiError.class);
        Map<String, String> validationErrors = response.getBody().getValidationErrors();
        assertThat(validationErrors.get("username")).isEqualTo("Este nombre se está usando");
    }

    @Test
    void getUsers_whenThereAreNoUsersInDB_receiveOK() {
        ResponseEntity<Object> response = getUsers(new ParameterizedTypeReference<Object>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // Como nuestra app podría tener millones de usuarios :D, se va a implementar paginación usando
    // el objeto Page de Spring Data
    @Test
    void getUsers_whenThereAreNoUsersInDB_receivePageWithZeroItems() {
        // Usamos nuestra implementación de Page y el test pasa
        ResponseEntity<TestPage<Object>> response = getUsers(new ParameterizedTypeReference<TestPage<Object>>() {});

        // El total de elementos de usuario en BD
        assertThat(response.getBody().getTotalElements()).isEqualTo(0);
    }

    @Test
    void getUsers_whenThereIsAUserInDB_receivePageWithUser() {
        userRepository.save(TestUtil.createValidUser());

        ResponseEntity<TestPage<Object>> response = getUsers(new ParameterizedTypeReference<TestPage<Object>>() {});

        // Aquí usamos getNumberOfElements, que es el número de usuarios en la página actual que viene en la response
        assertThat(response.getBody().getNumberOfElements()).isEqualTo(1);
    }

    @Test
    void getUsers_whenThereIsAUserInDB_receiveUserWithoutPassword() {
        userRepository.save(TestUtil.createValidUser());

        // Aquí ya podríamos usar User, pero por ahora no vamos a ser muy estrictos con lo que esperamos
        ResponseEntity<TestPage<Map<String, Object>>> response =
                getUsers(new ParameterizedTypeReference<TestPage<Map<String, Object>>>() {});

        // Muy parecido a lo que se hizo en LoginController, salvo que ahora estamos recibiendo un Objeto Page
        Map<String, Object> entity = response.getBody().getContent().get(0);

        // Para solucionar el problema de que venga el password en el response, en LoginController incluimos
        // la anotación @JsonView. Lo mismo vamos a hacer en UserController.
        // Pero esto por si solo falla porque no solo estamos devolviendo el objeto User. En este caso
        // estamos devolviendo un objeto Page y este no tiene anotaciones @JsonView, así que Jackson
        // está descartando todos los campos y la aplicación está devolviendo un objeto vacío.
        // Page no es un objeto nuestro, así que no podemos añadir la anotación @JsonView a sus campos,
        // pero podemos configurar el comportamiento de serialización de Jackson para objeto Page.
        //
        // 1. Se crea en el paquete configuration la clase SerializationConfiguration.
        // Notar que si UserController devolviera un objeto List en vez de Page, esta configuración de la
        // serialización no sería necesaria.
        // Esta solución aportada no siempre es la mejor. Si los objetos del modelo de dominio se hace más
        // grande con relaciones con otras entidades, manejar la salida de JSON se hace más difícil.
        //
        // Se va a ofrecer alternativas de serialización.
        //
        // 2. Se puede resolver este problema en el repository. Podemos pedirle que devuelva
        // el objeto que queremos. Ver clase UseProjection en package user.
        // Projection proporciona opciones flexibles de generación de nuestros modelos.
        // Como desventaja, indicar que nos fuerza a escribir funciones Projection en nuestro repository.
        // Tampoco se va a utilizar esta solución por el "problema" indicado en LoginController.
        //
        // 3. Para el lado View existe un enfoque mås común cuando se trata de modelar el objeto de dominio.
        // Se le suele llamar DTO (Data Transfer Object) o VM (View Model)
        // Ver UserVM
        // Este es el enfoque que se va a usar y se ha cambiado también LoginController.
        // Se elimina Views.java y SerializationConfiguration.java porque ya no hacen falta y se corrigen los errores.
        assertThat(entity.containsKey("password")).isFalse();
    }

    @Test
    void getUsers_whenPageIsRequestedFor3ItemsPerPageWhereTheDatabaseHas20Users_receive3Users() {
        IntStream.rangeClosed(1, 20).mapToObj(i -> "test-user-" + i).map(TestUtil::createValidUser)
                .forEach(userRepository::save);

        String path = API_1_0_USERS + "?page=0&size=3";
        ResponseEntity<TestPage<Object>> response =
                getUsers(path, new ParameterizedTypeReference<TestPage<Object>>() {});

        assertThat(response.getBody().getContent().size()).isEqualTo(3);
    }

    @Test
    void getUsers_whenPageSizeNotProvided_receivePageSizeAs10() {
        ResponseEntity<TestPage<Object>> response = getUsers(new ParameterizedTypeReference<TestPage<Object>>() {});
        // Este test falla porque por defecto, en SpringDataWebProperties se indica que defaultPageSize es 20.
        // 1. Se puede sobreescribir este valor en application.yml y queda para toda la aplicación.
        // 2. A nivel de controlador se sobreescribe el defaultPageSize
        assertThat(response.getBody().getSize()).isEqualTo(10);
    }

    @Test
    void getUsers_whenPageSizeIsGreaterThan100_receivePageSizeAs100() {
        String path = API_1_0_USERS + "?size=500";
        ResponseEntity<TestPage<Object>> response = getUsers(path, new ParameterizedTypeReference<TestPage<Object>>() {});
        // El valor por defecto en la clase SpringDataWebProperties es maxPageSize = 2000
        // Lo sobreescribimos en application.yml
        assertThat(response.getBody().getSize()).isEqualTo(100);
    }

    @Test
    void getUsers_whenPageSizeIsNegative_receivePageSizeAs10() {
        String path = API_1_0_USERS + "?size=-5";
        ResponseEntity<TestPage<Object>> response = getUsers(path, new ParameterizedTypeReference<TestPage<Object>>() {});
        // Esto ya funciona porque el default es 10
        assertThat(response.getBody().getSize()).isEqualTo(10);
    }

    @Test
    void getUsers_whenPageIsNegative_receiveFirstPage() {
        String path = API_1_0_USERS + "?page=-5";
        ResponseEntity<TestPage<Object>> response = getUsers(path, new ParameterizedTypeReference<TestPage<Object>>() {});
        // Esto ya funciona porque el default es 0 (primera página)
        assertThat(response.getBody().getNumber()).isEqualTo(0);
    }

    @Test
    void getUsers_whenUserLoggedIn_receivePageWithoutLoggedInUser() {
        userService.save(TestUtil.createValidUser("user1"));
        userService.save(TestUtil.createValidUser("user2"));
        userService.save(TestUtil.createValidUser("user3"));
        authenticate("user1");

        ResponseEntity<TestPage<Object>> response = getUsers(new ParameterizedTypeReference<TestPage<Object>>() {});

        assertThat(response.getBody().getTotalElements()).isEqualTo(2);
    }

    @Test
    void getUserByUsername_whenUserExist_receiveOk() {
        String username = "test-user";
        userService.save(TestUtil.createValidUser(username));
        ResponseEntity<Object> response = getUser(username, Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getUserByUsername_whenUserExist_receiveUserWithoutPassword() {
        String username = "test-user";
        userService.save(TestUtil.createValidUser(username));
        ResponseEntity<String> response = getUser(username, String.class);

        assertThat(response.getBody().contains("password")).isFalse();
    }

    @Test
    void getUserByUsername_whenUserDoesNotExist_receiveNotFound() {
        ResponseEntity<Object> response = getUser("unknown-user", Object.class);
        // Falla porque user es un null object y lo estamos intentando convertir a UserVM.
        // Esto dispara la excepción NullPointerException y no estamos convirtiendo el response status
        // de esta excepción, por lo que por defecto Spring devuelve status 500.
        // Se va a corregir con un Custom Exception. Ver el paquete error, clase NotFoundException
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getUserByUsername_whenUserDoesNotExist_receiveApiError() {
        ResponseEntity<ApiError> response = getUser("unknown-user", ApiError.class);
        // Al lanzar NotFoundException estamos enviando como mensaje el username, de ahí que tiene que contenerlo.
        // Esto ya funciona sin meter código porque las excepciones no manejadas se manejan en la clase ErrorHandler.
        assertThat(response.getBody().getMessage().contains("unknown-user")).isTrue();
    }

    @Test
    void putUser_whenUnauthorizedUserSendsTheRequest_receiveUnauthorized() {
        ResponseEntity<Object> response = putUser(123, null, Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void putUser_whenAuthorizedUserSendsUpdateForAnotherUser_receiveForbidden() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        authenticate(user.getUsername());

        long anotherUserId = user.getId() + 123;
        ResponseEntity<Object> response = putUser(anotherUserId, null, Object.class);

        // Para pasar este test, hay 2 posibilidades:
        // 1. Podemos revisar en la BD si User es el mismo que el usuario que ha hecho login. En caso contrario
        //    se lanza una excepción.
        // 2. Enfoque más simple. Spring Security provee de una característica llamada Autorización a nivel de método.
        //    Hay que activar esta característica (ver SecurityConfiguration)
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
