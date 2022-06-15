package com.jmunoz.hoaxify;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

// Test de integración
// @ActiveProfiles sirve para darnos la flexibilidad de configurar el comportamiento de la app en tiempo de ejecución
// Cuando ejecutamos los tests, queremos que se usen bases de datos de test, y no queremos usar nada de producción.
// Queremos que nuestros test se ejecuten en un entorno controlado.

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class UserControllerTest {

}
