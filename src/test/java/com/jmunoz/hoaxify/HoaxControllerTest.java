package com.jmunoz.hoaxify;

import com.jmunoz.hoaxify.configuration.AppConfiguration;
import com.jmunoz.hoaxify.error.ApiError;
import com.jmunoz.hoaxify.file.FileAttachment;
import com.jmunoz.hoaxify.file.FileAttachmentRepository;
import com.jmunoz.hoaxify.file.FileService;
import com.jmunoz.hoaxify.hoax.Hoax;
import com.jmunoz.hoaxify.hoax.HoaxRepository;
import com.jmunoz.hoaxify.hoax.HoaxService;
import com.jmunoz.hoaxify.hoax.HoaxVM;
import com.jmunoz.hoaxify.user.User;
import com.jmunoz.hoaxify.user.UserRepository;
import com.jmunoz.hoaxify.user.UserService;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import java.io.File;
import java.io.IOException;
import java.util.List;
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

    @Autowired
    HoaxService hoaxService;

    @Autowired
    FileAttachmentRepository fileAttachmentRepository;

    @Autowired
    AppConfiguration appConfiguration;

    @Autowired
    FileService fileService;

    @PersistenceUnit
    private EntityManagerFactory entityManagerFactory;

    @BeforeEach
    void setUp() throws IOException {
        fileAttachmentRepository.deleteAll();
        hoaxRepository.deleteAll();
        userRepository.deleteAll();
        testRestTemplate.getRestTemplate().getInterceptors().clear();
        FileUtils.cleanDirectory(new File(appConfiguration.getFullAttachmentsPath()));
    }

    // Para evitar errores en otras clases de tests, ya que para el último test de esta clase
    // no se está eliminando correctamente la tabla User que posee un Hoax en BD.
    //
    // Como LoginControllerTest tiene un @BeforeEach que hace un deleteAll de userRepository,
    // como se ha establecido una relación entre las tablas User y Hoax, y como en Hoax queda un registro
    // el delete de User no va a funcionar.
    //
    // Aquí borramos el hoax para que en los otros test ya se puede borrar User.
    //
    // Como tenemos en @BeforeEach deleteAll de fileAttachmentRepository,
    // como se ha establecido una relación entre las tablas FileAttachment y Hoax, y como en FileAttachment
    // queda un registro, el delete de Hoax no va a funcionar. Se añade el fileAttachmentRepository primero.
    @AfterEach
    void tearDown() {
        fileAttachmentRepository.deleteAll();
        hoaxRepository.deleteAll();
    }

    private boolean authenticate(String username) {
        return testRestTemplate
                .getRestTemplate().getInterceptors().add(new BasicAuthenticationInterceptor(username, "P4ssword"));
    }

    private <T> ResponseEntity<T> postHoax(Hoax hoax, Class<T> responseType) {
        return testRestTemplate.postForEntity(API_1_0_HOAXES, hoax, responseType);
    }

    public <T> ResponseEntity<T> getHoaxes(ParameterizedTypeReference<T> responseType) {
        return testRestTemplate.exchange(API_1_0_HOAXES, HttpMethod.GET, null, responseType);
    }

    public <T> ResponseEntity<T> getHoaxesOfUser(String username, ParameterizedTypeReference<T> responseType) {
        String path = "/api/1.0/users/" + username + "/hoaxes";
        return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
    }

    public <T> ResponseEntity<T> getOldHoaxes(long hoaxId, ParameterizedTypeReference<T> responseType) {
        // Es necesario el hoaxId para en las siguientes cargas no llevar información errónea porque se han grabado
        // más hoaxes
        String path = API_1_0_HOAXES + "/" + hoaxId + "?direction=before&page=0&size=5&sort=id,desc";
        return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
    }

    public <T> ResponseEntity<T> getOldHoaxesOfUser(long hoaxId, String username, ParameterizedTypeReference<T> responseType) {
        String path = "/api/1.0/users/" + username + "/hoaxes/" + hoaxId
                + "?direction=before&page=0&size=5&sort=id,desc";
        return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
    }

    public <T> ResponseEntity<T> getNewHoaxes(long hoaxId, ParameterizedTypeReference<T> responseType) {
        // Se traen todos los hoaxes nuevos
        String path = API_1_0_HOAXES + "/" + hoaxId + "?direction=after&sort=id,desc";
        return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
    }

    public <T> ResponseEntity<T> getNewHoaxesOfUser(long hoaxId, String username, ParameterizedTypeReference<T> responseType) {
        String path = "/api/1.0/users/" + username + "/hoaxes/" + hoaxId
                + "?direction=after&sort=id,desc";
        return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
    }

    // Util en la parte cliente para saber la cuenta de hoaxes nuevos y mostrar un botón para cargar nuevos hoaxes
    public <T> ResponseEntity<T> getNewHoaxCount(long hoaxId, ParameterizedTypeReference<T> responseType) {
        String path = API_1_0_HOAXES + "/" + hoaxId + "?direction=after&count=true";
        return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
    }

    public <T> ResponseEntity<T> getNewHoaxCountOfUser(long hoaxId, String username, ParameterizedTypeReference<T> responseType) {
        String path = "/api/1.0/users/" + username + "/hoaxes/" + hoaxId + "?direction=after&count=true";
        return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
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
        User user = userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Hoax hoax = TestUtil.createValidHoax();
        postHoax(hoax, Object.class);

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        // Pasamos el tipo de objeto que buscamos y su primary key
        User inDBUser = entityManager.find(User.class, user.getId());

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
        //
        // Para corregir el problema hay que configurar la anotación @OneToMany. Ver User.java
        // Estableciendo la carga de datos como EAGER (no LAZY) nuestro test funciona
        //
        // Pero tiene un problema, que EAGER toma más tiempo de procesador, ya que al cargar Users también
        // se cargarán sus Hoaxes, y un usuario podría tener miles de Hoaxes.
        // El rendimiento de nuestra app se ve muy impactado.
        // Por tanto, lo vamos a eliminar.
        // Opciones para corregir este problema:
        // 1. Añadir un query customizado a nuestro HoaxRespository. Ver método countByUserUsername
        //    Esto arregla el problema, pero tampoco es la forma ideal de manejar este test, porque se está
        //    añadiendo a la app una query que solo se va a usar en tests.
        // 2. Ejecutar nuestro método test de manera transaccional. Esto significa que, cuando ejecutamos
        //    un método del repository, antes Spring creará una transacción, que estará abierta hasta
        //    que la ejecución del método se complete.
        //    Como la transacción estará abierta tras la carga del usuario desde BD, cuando intentemos
        //    obtener la lista de Hoax, Hibernate podrá cargar de forma lazy las entradas Hoax desde BD,
        //    obteniendo el tamaño de esa lista.
        // 3. El problema empieza con nuestro userRepository.findByUsername() que devuelve un
        //    objeto User, y la correspondiente transacción termina inmediatamente.
        //    En vez de usar repository podemos usar Jpa EntityManager y ejecutar la query
        //    para cargar el usuario.
        assertThat(inDBUser.getHoaxes().size()).isEqualTo(1);
    }

    @Test
    void getHoaxes_whenThereAreNoHoaxes_receiveOk() {
        ResponseEntity<Object> response = getHoaxes(new ParameterizedTypeReference<Object>() {});
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getHoaxes_whenThereAreNoHoaxes_receivePageWithZeroItems() {
        ResponseEntity<TestPage<Object>> response = getHoaxes(new ParameterizedTypeReference<TestPage<Object>>() {});
        assertThat(response.getBody().getTotalElements()).isEqualTo(0);
    }

    @Test
    void getHoaxes_whenThereAreHoaxes_receivePageWithItems() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());

        // En este test se produce un bucle infinito cuando tratamos de construir el JSON a partir del objeto Hoax.
        // Cuando se obtiene el objeto Hoax, Spring pide a Jackson que lo convierta en JSON, y cuando lo hace,
        // comprueba los campos de Hoax y encuentra el campo User. Jackson lo intenta convertir a JSON, así que
        // comprueba los campos de User y encuentra el campo Hoax, así que lo intenta convertir a JSON y sigue
        // y sigue...
        // Esto se puede corregir:
        // 1. Usando la propiedad @JsonIgnore en la clase Hoax, campo User.
        //    De esta forma no sabemos qué usuario creo el Hoax, por lo que será problemático en la
        //    implementación en el cliente cuando mostremos los hoaxes, porque queremos informar también
        //    los datos del usuario.
        // 2. Implementando Hoax View Model Object.
        //    Se crea una clase HoaxVM donde sí que tenemos los datos de usuario.
        //    Ver el siguiente test
        ResponseEntity<TestPage<Object>> response = getHoaxes(new ParameterizedTypeReference<TestPage<Object>>() {});
        assertThat(response.getBody().getTotalElements()).isEqualTo(3);
    }

    @Test
    void getHoaxes_whenThereAreHoaxes_receivePageWithHoaxVM() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        hoaxService.save(user, TestUtil.createValidHoax());

        ResponseEntity<TestPage<HoaxVM>> response = getHoaxes(new ParameterizedTypeReference<TestPage<HoaxVM>>() {});
        HoaxVM storedHoax = response.getBody().getContent().get(0);
        assertThat(storedHoax.getUser().getUsername()).isEqualTo("user1");
    }

    @Test
    void postHoax_whenHoaxIsValidAndUserIsAuthorized_receiveHoaxVM() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");

        Hoax hoax = TestUtil.createValidHoax();
        ResponseEntity<HoaxVM> response = postHoax(hoax, HoaxVM.class);
        assertThat(response.getBody().getUser().getUsername()).isEqualTo("user1");
    }

    // Se va a establecer la relación entre hoax y ficheros adjuntados.
    // Desde el lado del cliente se subirá la imagen y se dará la información del archivo almacenado
    // a la petición submit del hoax.
    // Podemos permitir que un hoax pueda tener muchos ficheros adjuntos, pero se limita a 1.
    // Es decir, un hoax puede tener solo un fichero adjunto.
    @Test
    void postHoax_whenHoaxHasFileAttachmentAndUserIsAuthorized_fileAttachmentHoaxRelationIsUpdatedInDatabase() throws IOException {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");

        MultipartFile file = createFile();

        FileAttachment savedFile = fileService.saveAttachment(file);

        Hoax hoax = TestUtil.createValidHoax();
        hoax.setAttachment(savedFile);
        ResponseEntity<HoaxVM> response = postHoax(hoax, HoaxVM.class);

        FileAttachment inDB = fileAttachmentRepository.findAll().get(0);
        assertThat(inDB.getHoax().getId()).isEqualTo(response.getBody().getId());
    }

    @Test
    void postHoax_whenHoaxHasFileAttachmentAndUserIsAuthorized_hoaxFileAttachmentRelationIsUpdatedInDatabase() throws IOException {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");

        MultipartFile file = createFile();

        FileAttachment savedFile = fileService.saveAttachment(file);

        Hoax hoax = TestUtil.createValidHoax();
        hoax.setAttachment(savedFile);
        ResponseEntity<HoaxVM> response = postHoax(hoax, HoaxVM.class);

        Hoax inDB = hoaxRepository.findById(response.getBody().getId()).get();
        assertThat(inDB.getAttachment().getId()).isEqualTo(savedFile.getId());
    }

    private MultipartFile createFile() throws IOException {
        ClassPathResource imageResource = new ClassPathResource("profile.png");
        byte[] fileAsByte = FileUtils.readFileToByteArray(imageResource.getFile());

        MultipartFile file = new MockMultipartFile("profile.png", fileAsByte);
        return file;
    }

    @Test
    void getHoaxesOfUser_whenUserExists_receiveOk() {
        userService.save(TestUtil.createValidUser("user1"));
        ResponseEntity<Object> response = getHoaxesOfUser("user1", new ParameterizedTypeReference<Object>() {});
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getHoaxesOfUser_whenUserDoesNotExist_receiveNotFound() {
        ResponseEntity<Object> response = getHoaxesOfUser("unknown-user", new ParameterizedTypeReference<Object>() {});
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getHoaxesOfUser_whenUserExists_receivePageWithZeroHoaxes() {
        userService.save(TestUtil.createValidUser("user1"));
        ResponseEntity<TestPage<Object>> response =
                getHoaxesOfUser("user1", new ParameterizedTypeReference<TestPage<Object>>() {});
        assertThat(response.getBody().getTotalElements()).isEqualTo(0);
    }

    @Test
    void getHoaxesOfUser_whenUserExistWithHoax_receivePageWithHoaxVM() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        hoaxService.save(user, TestUtil.createValidHoax());

        ResponseEntity<TestPage<HoaxVM>> response =
                getHoaxesOfUser("user1", new ParameterizedTypeReference<TestPage<HoaxVM>>() {});
        HoaxVM storedHoax = response.getBody().getContent().get(0);
        assertThat(storedHoax.getUser().getUsername()).isEqualTo("user1");
    }

    @Test
    void getHoaxesOfUser_whenUserExistWithMultipleHoaxes_receivePageWithMatchingHoaxesCount() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());

        ResponseEntity<TestPage<HoaxVM>> response =
                getHoaxesOfUser("user1", new ParameterizedTypeReference<TestPage<HoaxVM>>() {});
        assertThat(response.getBody().getTotalElements()).isEqualTo(3);
    }

    // Testeando el comportamiento de nuestro query method en HoaxRepository
    // Se hace en esta clase en vez de crear una nueva como se hizo con UserRepositoryTest
    @Test
    void getHoaxesOfUser_whenMultipleUserExistWithMultipleHoaxes_receivePageWithMatchingHoaxesCount() {
        User userWithThreeHoaxes = userService.save(TestUtil.createValidUser("user1"));
        IntStream.rangeClosed(1, 3).forEach(i -> {
            hoaxService.save(userWithThreeHoaxes, TestUtil.createValidHoax());
        });

        User userWithFiveHoaxes = userService.save(TestUtil.createValidUser("user2"));
        IntStream.rangeClosed(1, 5).forEach(i -> {
            hoaxService.save(userWithFiveHoaxes, TestUtil.createValidHoax());
        });

        ResponseEntity<TestPage<HoaxVM>> response =
                getHoaxesOfUser(userWithFiveHoaxes.getUsername(), new ParameterizedTypeReference<TestPage<HoaxVM>>() {});
        assertThat(response.getBody().getTotalElements()).isEqualTo(5);
    }

    @Test
    void getOldHoaxes_whenThereAreNoHoaxes_receiveOk() {
        ResponseEntity<Object> response = getOldHoaxes(5, new ParameterizedTypeReference<Object>() {});
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // NOTA: Se ha cambiado el nombre de este test
    @Test
    void getOldHoaxes_whenThereAreHoaxes_receivePageWithItemsBeforeProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());
        Hoax fourth = hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());

        ResponseEntity<TestPage<Object>> response =
                getOldHoaxes(fourth.getId(), new ParameterizedTypeReference<TestPage<Object>>() {});

        // Falla porque en respuesta se devuelven Hoax y eso causa stack overflow con Jackson.
        // Se va a corregir devolviendo una respuesta HoaxVM, en el siguiente test
        assertThat(response.getBody().getTotalElements()).isEqualTo(3);
    }

    @Test
    void getOldHoaxes_whenThereAreHoaxes_receivePageWithHoaxVMBeforeProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());
        Hoax fourth = hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());

        ResponseEntity<TestPage<HoaxVM>> response =
                getOldHoaxes(fourth.getId(), new ParameterizedTypeReference<TestPage<HoaxVM>>() {});

        assertThat(response.getBody().getContent().get(0).getDate()).isGreaterThan(0);
    }

    @Test
    void getOldHoaxesOfUser_whenUserExistThereAreNoHoaxes_receiveOk() {
        userService.save(TestUtil.createValidUser("user1"));
        ResponseEntity<Object> response =
                getOldHoaxesOfUser(5, "user1", new ParameterizedTypeReference<Object>() {});
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getOldHoaxesOfUser_whenUserExistAndThereAreHoaxes_receivePageWithItemsProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());
        Hoax fourth = hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());

        ResponseEntity<TestPage<Object>> response =
                getOldHoaxesOfUser(fourth.getId(), "user1", new ParameterizedTypeReference<TestPage<Object>>() {});

        // Falla porque en respuesta se devuelven Hoax y eso causa stack overflow con Jackson (infinite loop)
        // Se va a corregir devolviendo una respuesta HoaxVM, en el siguiente test
        assertThat(response.getBody().getTotalElements()).isEqualTo(3);
    }

    @Test
    void getOldHoaxesOfUser_whenUserExistAndThereAreHoaxes_receivePageWithHoaxVMBeforeProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());
        Hoax fourth = hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());

        ResponseEntity<TestPage<HoaxVM>> response =
                getOldHoaxesOfUser(fourth.getId(), "user1", new ParameterizedTypeReference<TestPage<HoaxVM>>() {});

        assertThat(response.getBody().getContent().get(0).getDate()).isGreaterThan(0);
    }

    @Test
    void getOldHoaxesOfUser_whenUserDoesNotExistAndThereAreNoHoaxes_receiveNotFound() {
        ResponseEntity<Object> response =
                getOldHoaxesOfUser(5, "user1", new ParameterizedTypeReference<Object>() {});
        // Este test ya se pasa porque en UserService tenemos el método getByUsername y si no se encuentra usuario
        // ya hay un Custom Exception (NotFoundException) que devuelve 404
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getOldHoaxesOfUser_whenUserExistAndThereAreNoHoaxes_receivePageWitZeroItemsBeforeProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());
        Hoax fourth = hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());

        userService.save(TestUtil.createValidUser("user2"));

        ResponseEntity<TestPage<HoaxVM>> response =
                getOldHoaxesOfUser(fourth.getId(), "user2", new ParameterizedTypeReference<TestPage<HoaxVM>>() {});

        assertThat(response.getBody().getTotalElements()).isEqualTo(0);
    }

    @Test
    void getNewHoaxes_whenThereAreHoaxes_receiveListOfItemsAfterProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());
        Hoax fourth = hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());

        ResponseEntity<List<Object>> response =
                getNewHoaxes(fourth.getId(), new ParameterizedTypeReference<List<Object>>() {});

        // Esperamos 1 que es el posterior al hoax fourth
        // Falla porque Jackson no puede convertir el tipo Page JSON a List.
        // Se crea un nuevo query method en HoaxRepository y en el controller se devuelve ResponseEntity en vez de Page.
        // Pero sigue fallando porque en la response devolvemos Hoax y causa stack overflow en la serialización Jackson
        // JSON. Se corrige usando una response HoaxVM en el siguiente test.
        assertThat(response.getBody().size()).isEqualTo(1);
    }

    @Test
    void getNewHoaxes_whenThereAreHoaxes_receiveListOfHoaxVMAfterProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());
        Hoax fourth = hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());

        ResponseEntity<List<HoaxVM>> response =
                getNewHoaxes(fourth.getId(), new ParameterizedTypeReference<List<HoaxVM>>() {});

        assertThat(response.getBody().get(0).getDate()).isGreaterThan(0);
    }

    @Test
    void getNewHoaxesOfUser_whenUserExistThereAreNoHoaxes_receiveOk() {
        userService.save(TestUtil.createValidUser("user1"));
        ResponseEntity<Object> response =
                getNewHoaxesOfUser(5, "user1", new ParameterizedTypeReference<Object>() {});
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getNewHoaxesOfUser_whenUserExistAndThereAreHoaxes_receiveListWithItemsAfterProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());
        Hoax fourth = hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());

        ResponseEntity<List<Object>> response =
                getNewHoaxesOfUser(fourth.getId(), "user1", new ParameterizedTypeReference<List<Object>>() {});

        // Falla porque no estamos chequeando la dirección en el método getHoaxesRelativeForUser, ya que solo
        // estamos devolviendo una response para BeforeId del tipo Page<Object>
        // Se crea un nuevo query method en HoaxRepository y en el controller se devuelve ResponseEntity en vez de Page.
        // Pero sigue fallando porque en la response devolvemos Hoax y causa stack overflow en la serialización Jackson
        // JSON. Se corrige usando una response HoaxVM en el siguiente test.
        assertThat(response.getBody().size()).isEqualTo(1);
    }

    @Test
    void getNewHoaxesOfUser_whenUserExistAndThereAreHoaxes_receiveListWithHoaxVMAfterProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());
        Hoax fourth = hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());

        ResponseEntity<List<HoaxVM>> response =
                getNewHoaxesOfUser(fourth.getId(), "user1", new ParameterizedTypeReference<List<HoaxVM>>() {});

        assertThat(response.getBody().get(0).getDate()).isGreaterThan(0);
    }

    @Test
    void getNewHoaxesOfUser_whenUserDoesNotExistAndThereAreNoHoaxes_receiveNotFound() {
        ResponseEntity<Object> response =
                getNewHoaxesOfUser(5, "user1", new ParameterizedTypeReference<Object>() {});
        // Este test ya se pasa porque en UserService tenemos el método getByUsername y si no se encuentra usuario
        // ya hay un Custom Exception (NotFoundException) que devuelve 404
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getNewHoaxesOfUser_whenUserExistAndThereAreNoHoaxes_receiveListWitZeroItemsAfterProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());
        Hoax fourth = hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());

        userService.save(TestUtil.createValidUser("user2"));

        ResponseEntity<List<HoaxVM>> response =
                getNewHoaxesOfUser(fourth.getId(), "user2", new ParameterizedTypeReference<List<HoaxVM>>() {});

        assertThat(response.getBody().size()).isEqualTo(0);
    }

    @Test
    void getNewHoaxCount_whenThereAreHoaxes_receiveCountAfterProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());
        Hoax fourth = hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());

        // Por ahora no definimos un modelo para el body response. Es generalista
        ResponseEntity<Map<String, Long>> response =
                getNewHoaxCount(fourth.getId(), new ParameterizedTypeReference<Map<String, Long>>() {});

        assertThat(response.getBody().get("count")).isEqualTo(1);
    }

    @Test
    void getNewHoaxCountOfUser_whenThereAreHoaxes_receiveCountAfterProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());
        Hoax fourth = hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());

        ResponseEntity<Map<String, Long>> response =
                getNewHoaxCountOfUser(fourth.getId(), "user1", new ParameterizedTypeReference<Map<String, Long>>() {});

        assertThat(response.getBody().get("count")).isEqualTo(1);
    }
}
