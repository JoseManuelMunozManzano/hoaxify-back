package com.jmunoz.hoaxify.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

// Esta clase será chequeada por Spring para to-do lo relacionado con configuraciones web, como configurar
// paths de fuentes estáticas o configurar interceptores de peticiones Http.
// En nuestro caso vamos a comprobar la carpeta upload y la crearemos si no existe.

@Configuration
public class WebConfiguration implements WebMvcConfigurer {

    @Autowired
    AppConfiguration appConfiguration;

    @Bean
    CommandLineRunner createUploadFolder() {
        return (args) -> {
            createNonExistingFolder(appConfiguration.uploadPath);
            createNonExistingFolder(appConfiguration.getFullProfileImagePath());
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
