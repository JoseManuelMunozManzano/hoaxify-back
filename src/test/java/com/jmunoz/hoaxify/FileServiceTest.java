package com.jmunoz.hoaxify;

import com.jmunoz.hoaxify.configuration.AppConfiguration;
import com.jmunoz.hoaxify.file.FileAttachment;
import com.jmunoz.hoaxify.file.FileAttachmentRepository;
import com.jmunoz.hoaxify.file.FileService;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

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

    // Como es un unit testing no se va a pedir a Spring que genere una instancia de FileAttachmentRepository y
    // la inyecte.
    // No usamos Spring.
    // Lo que se va a hacer es un mock de ese objeto.
    @MockBean
    FileAttachmentRepository fileAttachmentRepository;

    // Necesitamos inicializar nuestras instancias manualmente, ya que la app no se ejecutará para estos tests.
    @BeforeEach
    void setUp() {
        // Tampoco se va a poder leer application.yml, por lo que no podremos obtener los valores
        // que se configuraron ahí y tenemos que poner los valores que necesitemos.
        appConfiguration = new AppConfiguration();
        appConfiguration.setUploadPath("uploads-test");

        fileService = new FileService(appConfiguration, fileAttachmentRepository);

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

    // Eliminar adjuntos no asociados a ningún hoax si el fichero lleva una hora creado y no tiene hoax asociado.
    @Test
    void cleanupStorage_whenOldFilesExist_removesFilesFromStorage() throws IOException {
        String fileName = "random-file";
        String filePath = appConfiguration.getFullAttachmentsPath() + "/" + fileName;
        File source = new ClassPathResource("profile.png").getFile();
        File target = new File(filePath);
        FileUtils.copyFile(source, target);

        // Normalmente, esto se configuraría en DB, pero aquí no hay interacciones en BD porque solo hacemos un mock
        // de su comportamiento.
        FileAttachment fileAttachment = new FileAttachment();
        fileAttachment.setId(5);
        fileAttachment.setName(fileName);

        // También tenemos métodos en FileService pero para poder usarlos se tendría que cambiar el modo del test.
        // Como estamos haciendo unit testing, no tenemos BD ni repository.
        // Solo tenemos una instancia de FileService
        Mockito.when(fileAttachmentRepository.findByDateBeforeAndHoaxIsNull(Mockito.any(Date.class)))
                .thenReturn(Arrays.asList(fileAttachment));

        fileService.cleanupStorage();
        File storedImage = new File(filePath);

        assertThat(storedImage.exists()).isFalse();
    }

    @Test
    void cleanupStorage_whenOldFilesExist_removesFilesAttachmentFromDatabase() throws IOException {
        String fileName = "random-file";
        String filePath = appConfiguration.getFullAttachmentsPath() + "/" + fileName;
        File source = new ClassPathResource("profile.png").getFile();
        File target = new File(filePath);
        FileUtils.copyFile(source, target);

        // Normalmente, esto se configuraría en DB, pero aquí no hay interacciones en BD porque solo hacemos un mock
        // de su comportamiento.
        FileAttachment fileAttachment = new FileAttachment();
        fileAttachment.setId(5);
        fileAttachment.setName(fileName);

        // También tenemos métodos en FileService pero para poder usarlos se tendría que cambiar el modo del test.
        // Como estamos haciendo unit testing, no tenemos BD ni repository.
        // Solo tenemos una instancia de FileService
        Mockito.when(fileAttachmentRepository.findByDateBeforeAndHoaxIsNull(Mockito.any(Date.class)))
                .thenReturn(Arrays.asList(fileAttachment));

        fileService.cleanupStorage();
        File storedImage = new File(filePath);

        Mockito.verify(fileAttachmentRepository).deleteById(5L);
    }

}
