package com.jmunoz.hoaxify.user;

import com.jmunoz.hoaxify.error.ApiError;
import com.jmunoz.hoaxify.shared.GenericResponse;
import com.jmunoz.hoaxify.user.vm.UserVM;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/1.0")
public class UserController {

    @Autowired
    UserService userService;

    @PostMapping("/users")
    GenericResponse createUser(@Valid @RequestBody User user) {
        userService.save(user);
        return new GenericResponse("User saved");
    }

    // La página actual y el número de elementos por página se pasa por parámetro en el request. Vendrá en la URL
    // Los hacemos no requeridos y damos valor por defecto para evitar el null con tipos primitivos.
    // Pasar el tipo a Integer es peor porque tenemos que tener en cuenta validaciones con null
    // Así no fallan los tests que no provean estos valores.
    //
    // Tenemos requerimientos en la lógica de la Paginación como la página máxima o valores negativos.
    // Spring nos ayuda a manejar estos requerimientos. Hay objetos configurables para este propósito como
    // Pageable que ya hemos usado en nuestro service.
    // Vamos a refactorizar este método.
    // Pageable viene de Spring Data
    // Si entramos al fuente de Pageable veremos que espera page y size, pero nuestros parámetros se llaman
    // currentPage y pageSize.
    // Hay 2 opciones:
    // 1. Cambiar la configuración Pageable y usar nuestros nombres de parámetros. Esto se hace en
    //    application.yml
    // 2. Sustituimos en el test (y en la app) los parámetros a los por defecto
    @GetMapping("/users")
    Page<UserVM> getUsers(Pageable page) {
        return userService.getUsers(page).map(UserVM::new);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ApiError handleValidationException(MethodArgumentNotValidException exception, HttpServletRequest request) {
        ApiError apiError = new ApiError(400, "Validation error", request.getServletPath());

        // Para obtener los errores de validación
        // A partir de este BindingResult podemos acceder a campos de error
        BindingResult result = exception.getBindingResult();

        Map<String, String> validationErrors = new HashMap<>();

        for(FieldError fieldError : result.getFieldErrors()) {
            validationErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        apiError.setValidationErrors(validationErrors);

        return apiError;
    }
}
