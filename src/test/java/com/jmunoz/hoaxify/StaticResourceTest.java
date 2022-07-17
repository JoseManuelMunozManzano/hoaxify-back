package com.jmunoz.hoaxify;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;

import static org.assertj.core.api.Assertions.*;

// Por ahora no hemos guardado el fichero subido a una carpeta.
// Vamos a ver como funcionará el backend cuando se trata de ficheros estáticos como imágenes.
// Primero, los ficheros subidos deben almacenarse en algún directorio, así que tenemos
// que decirle a Spring que carpeta se va a usar para almacenamiento de imágenes.
// También le diremos a Spring que cree la carpeta si no existe.

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class StaticResourceTest {

    // Definida propiedad personalizada en application.yml
    // Si se informan muchas propiedades se puede volver un lío, así que lo mejor es crear una clase de
    // Configuración que mantenga nuestras propiedades.
    @Value("${uploadpath}")
    String uploadPath;

    @Test
    void checkStaticFolder_whenAppIsInitialized_uploadFolderMustExist() {
        // El objeto File se usa en Java tanto para ficheros como para carpetas
        File uploadFolder = new File(uploadPath);
        boolean uploadFolderExist = uploadFolder.exists() && uploadFolder.isDirectory();

        assertThat(uploadFolderExist).isTrue();
    }
}
