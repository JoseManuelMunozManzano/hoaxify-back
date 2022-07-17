package com.jmunoz.hoaxify.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

// En esta clase mantendremos todas nuestras propiedades personalizadas
// @ConfigurationProperties sirve para atar las propiedades externas a este objeto. En este caso también definimos
// un prefijo
// Ahora, si en application.yml creamos la propiedad hoaxify.upload-path se configurará el valor de nuestra
// propiedad aquí.
//
// Para @ConfigurationProperties se recomienda añadir la dependencia spring-boot-configuration-processor al pom.
// Sirve como asistente para completar código en application.yml (pero no veo que funcione en IntelliJ Idea
// el autocompletado aunque haya hecho un Build > Rebuild Project)
@Configuration
@ConfigurationProperties(prefix = "hoaxify")
@Data
public class AppConfiguration {

    // Este valor aparece en application.yml como upload=path
    String uploadPath;

    // En este caso se establece aquí el valor. Sirve como valor por defecto
    String profileImagesFoder = "profile";

    String attachmentsFolder = "attachments";

    public String getFullProfileImagesPath() {
        return this.uploadPath + "/" + this.profileImagesFoder;
    }

    public String getFullAttachmentsPath() {
        return this.uploadPath + "/" + this.attachmentsFolder;
    }
}
