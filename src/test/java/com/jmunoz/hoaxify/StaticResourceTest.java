package com.jmunoz.hoaxify;

import com.jmunoz.hoaxify.configuration.AppConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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

    // Ahora en vez de tener tantos @Value por el código, se inyecta la clase AppConfiguration
    // y se usa en código para obtener los valores
    @Autowired
    AppConfiguration appConfiguration;

    @Test
    void checkStaticFolder_whenAppIsInitialized_uploadFolderMustExist() {
        // El objeto File se usa en Java tanto para ficheros como para carpetas
        File uploadFolder = new File(appConfiguration.getUploadPath());
        boolean uploadFolderExist = uploadFolder.exists() && uploadFolder.isDirectory();

        assertThat(uploadFolderExist).isTrue();
    }

    // Como vamos a tener las imágenes de perfil y más tarde imágenes adjuntas de los usuarios las vamos
    // a separar en subcarpetas
    @Test
    void checkStaticFolder_WhenAppIsInitialized_profileImageSubFolderMustExist() {
        String profileImageFolderPath = appConfiguration.getFullProfileImagePath();
        File profileImageFolder = new File(profileImageFolderPath);
        boolean profileImageFolderExist = profileImageFolder.exists() && profileImageFolder.isDirectory();

        assertThat(profileImageFolderExist).isTrue();
    }
}
