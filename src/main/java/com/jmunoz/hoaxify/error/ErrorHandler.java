package com.jmunoz.hoaxify.error;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

// La solución con Exception Handler no funcionaba.
// Para manejar estos errores usamos el mecanismo de reenvío de error interno de Spring.
// Creamos un controller customizado para manejar estos errores internos.
//
// Spring internamente redirige errores no manejados a este ErrorController

@RestController
public class ErrorHandler implements ErrorController {

    // Para poder obtener los errores del request
    @Autowired
    private ErrorAttributes errorAttributes;

    // Como necesitamos obtener la data de la petición, pedimos a Spring que nos proporcione el objeto WebRequest.
    // Con esto podemos obtener los errores del request.
    @RequestMapping("/error")
    ApiError handleError(WebRequest webRequest) {
        Map<String, Object> attributes = errorAttributes.getErrorAttributes(webRequest,
                ErrorAttributeOptions.of(ErrorAttributeOptions.Include.MESSAGE));

        String message = (String) attributes.get("message");
        String url = (String) attributes.get("path");
        int status = (Integer) attributes.get("status");

        return new ApiError(status, message, url);
    }
}
