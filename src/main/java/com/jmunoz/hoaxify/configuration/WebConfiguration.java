package com.jmunoz.hoaxify.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

// Esta clase será chequeada por Spring para to-do lo relacionado con configuraciones web, como configurar
// paths de fuentes estáticas o configurar interceptores de peticiones Http.
// En nuestro caso vamos a comprobar la carpeta upload y la crearemos si no existe.

@Configuration
public class WebConfiguration implements WebMvcConfigurer {

    @Autowired
    AppConfiguration appConfiguration;

    // Spring usa ResourceHandlerRegistry para configurar las asignaciones de recursos
    // Lo que sea que empiece por /images se servirá por esta configuración, y se indica donde buscará
    // Spring cuando se reciba un request.
    // Es básicamente un mapeo de estructura de carpetas.
    // Si, por ejemplo se recibe un request a /images/profile/my-profile-picture.jpg buscará en la carpeta
    // profile bajo el path de upload definido y entonces buscará el nombre del archivo de imagen.
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + appConfiguration.getUploadPath() + "/");
    }

    // Se ejecuta este Bean para profiles que son dev
    // Se puede indicar no ejecutar para un profile en concreto de esta forma:
    // @Profile("!test")
    // Se ejecutaría el Bean para profiles que NO son test
    @Bean
    @Profile("dev")
    CommandLineRunner createUploadFolder() {
        return (args) -> {
            createNonExistingFolder(appConfiguration.uploadPath);
            createNonExistingFolder(appConfiguration.getFullProfileImagesPath());
            createNonExistingFolder(appConfiguration.getFullAttachmentsPath());
        };
    }

    private void createNonExistingFolder(String path) {
        File folder = new File(path);
        boolean folderExist = folder.exists() && folder.isDirectory();
        if (!folderExist) {
            folder.mkdir();
        }
    }
}
