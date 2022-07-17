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

    @Test
    void checkStaticFolder_WhenAppIsInitialized_attachmentsSubFolderMustExist() {
        String attachmentsFolderPath = appConfiguration.getFullAttachmentsPath();
        File attachmentFolder = new File(attachmentsFolderPath);
        boolean attachmentFolderExist = attachmentFolder.exists() && attachmentFolder.isDirectory();

        assertThat(attachmentFolderExist).isTrue();
    }

    // Tenemos ahora una configuración customizada mantenible para nuestra app, pero tenemos otro problema.
    // Queremos que el nombre de esta carpeta sea diferente en producción.
    //
    // Podemos hacer que estas propiedades sean dinámicas dividiendo application.yml en diferentes profiles.
    //
    // Hasta ahora, hemos indicado el nombre de profile solo para clases de test, pero para tiempo de ejecución no
    // hemos indicado ningún nombre.
    //
    // Cuando no se asigna un nombre de profile, Spring le asigna uno por defecto (default)
    // Se puede ver al ejecutar la app, en la consola:
    // 2022-07-17 07:40:47.523  INFO 6075 --- [           main] com.jmunoz.hoaxify.HoaxifyApplication    : No active profile set, falling back to 1 default profile: "default"
    //
    // En application.yml se pueden especificar múltiples profiles.
    // Se puede dividir el fichero en secciones con --- y configurar el nombre del perfil de cada sección.
    // Como se ha indicado, si no se indica nada, se usa default
    //
    // Ver application.yml
    // Se puede ver que para producción estamos usando MySql e indicando nuestro path para subida de imágenes.
    // Con esta configuración podemos usar el mismo código en diferentes entornos sin afectarse entre ellos.
    //
    // Cómo se puede configurar que profile se va a usar cuando se ejecuta la aplicación?
    // De nuevo, se usa el fichero de properties.yml
    //    profiles:
    //      active:
    //      - prov
    //      - dev
    // Pero hay que tener cuidado en este punto, ya que muchos profiles podrían tener las mismas propiedades
    // con distintos valores, así que se sobreescribirían los unos a los otros.
    // Por ejemplo, esto ocurre con la propiedad
    // hoaxify:
    //   upload-path:
    // Tanto el profile prod como el profile dev contienen esta propiedad y cada uno tiene un valor distinto.
    // En este caso se considera el orden en que están definidas. La sección profile siguiente sustituye el valor
    // de la sección profile anterior.
    // En este caso, la propiedad en conflicto tomará el valor que tiene el profile dev
    // Si queremos un orden diferente, tenemos que cambiar el orden de las secciones.
    // Antes de la versión 2.0 de Spring Boot el orden se basaba en los profiles activos (profiles: active:)
    // donde el último valor sustituía el valor del anterior. No se consideraban las secciones, solo el orden
    // de la lista.
    //
    // También se puede configurar el nombre del profile como argumento de línea de comandos.
    // Cuando nuestra app es empaquetada como fichero .jar, la podemos ejecutar usando diferentes profiles de esta
    // manera:
    // --spring.profiles.active=prod
    // De nuevo, se pueden añadir más profiles separándolos con coma.
    // --spring.profiles.active=prod,dev
    //
    // Ahora, si se ejecuta la app veremos que se ejecuta el profile dev
    // 2022-07-17 12:47:09.347  INFO 2558 --- [           main] com.jmunoz.hoaxify.HoaxifyApplication    : The following 1 profile is active: "dev"
    //
    // NOTA IMPORTANTE: Spring Boot 2.1.12 y 2.2.3 rompe los tests por bugs introducidos en estas releases.
    // Se soluciona usando otras versiones de Spring Boot.
}
