package com.jmunoz.hoaxify.user;

import com.jmunoz.hoaxify.error.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.AccessDeniedException;

@RestController
public class LoginController {

    @PostMapping("/api/1.0/login")
    void handleLogin() {

    }

    // La solución con Exception Handler no funciona.
    // El test sigue fallando porque SpringSecurity esta manejando esta excepción en su
    // cadena de filtro de seguridad interno.
    // Y esa cadena ocurre mucho antes que la petición llegue a nuestro controller, por lo que esta
    // excepción nunca se llega a disparar.
    @ExceptionHandler({AccessDeniedException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    ApiError handleAccessDeniedException() {
        return new ApiError(401, "Access error", "/api/1.0/login");
    }
}
