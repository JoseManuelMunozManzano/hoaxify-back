package com.jmunoz.hoaxify;

import com.jmunoz.hoaxify.configuration.AppConfiguration;
import com.jmunoz.hoaxify.file.FileService;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.*;

// Hasta ahora hemos creado tests de integración donde la app era inicializada y se realizaban acciones con
// HttpRequest.
// Pero en este caso se va a realizar unit testing sobre la clase FileService
// Solo se van a permitir ficheros de imágenes del tipo jpeg y png
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class FileServiceTest {

    FileService fileService;

    AppConfiguration appConfiguration;

    // Necesitamos inicializar nuestras instancias manualmente, ya que la app no se ejecutará para estos tests.
    @BeforeEach
    void setUp() {
        // Tampoco se va a poder leer application.yml, por lo que no podremos obtener los valores
        // que se configuraron ahí y tenemos que poner los valores que necesitemos.
        appConfiguration = new AppConfiguration();
        appConfiguration.setUploadPath("uploads-test");

        fileService = new FileService(appConfiguration);

        // Recordar que las carpetas de subida las crea nuestra app durante la inicialización.
        // Lo hace la clase WebConfiguration, pero como esa clase no será inicializada por Spring, la creación
        // lógica de nuestras carpetas no se disparará. Por tanto, hay que crear el directorio de subida.
        new File(appConfiguration.getUploadPath()).mkdir();
        new File(appConfiguration.getFullProfileImagesPath()).mkdir();
        new File(appConfiguration.getFullAttachmentsPath()).mkdir();
    }

    // Tras cada test se limpian los ficheros generados
    @AfterEach
    void tearDown() throws IOException {
        FileUtils.cleanDirectory(new File(appConfiguration.getFullProfileImagesPath()));
        FileUtils.cleanDirectory(new File(appConfiguration.getFullAttachmentsPath()));
    }

    // El unit test se centra en una unidad específica, no se considera la app en su totalidad, sino que
    // nos focalizamos en un solo método que está en FileService.

    @Test
    void detectType_whenPngFileProvided_returnsImagePng() throws IOException {
        ClassPathResource resourceFile = new ClassPathResource("test-png.png");
        byte[] fileArr = FileUtils.readFileToByteArray(resourceFile.getFile());
        String fileType = fileService.detectType(fileArr);

        assertThat(fileType).isEqualToIgnoringCase("image/png");
    }
}
