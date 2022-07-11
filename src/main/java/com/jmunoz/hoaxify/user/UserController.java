package com.jmunoz.hoaxify.user;

import com.jmunoz.hoaxify.error.ApiError;
import com.jmunoz.hoaxify.shared.CurrentUser;
import com.jmunoz.hoaxify.shared.GenericResponse;
import com.jmunoz.hoaxify.user.vm.UserVM;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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

    // Obtenemos el usuario que se ha loggeado
    @GetMapping("/users")
    Page<UserVM> getUsers(@CurrentUser User loggedInUser, @PageableDefault Pageable page) {
        return userService.getUsers(loggedInUser, page).map(UserVM::new);
    }

    @GetMapping("/users/{username}")
    UserVM getUserByName(@PathVariable String username) {
        User user = userService.getByUsername(username);
        return new UserVM(user);
    }

    // Se indica usando regex que el id es un numérico.
    //
    // @PreAuthorize usa un parámetro que es un SpEL (Spring Expression Language)
    // Se va a comprobar la igualdad del id del path Variable (#id) con el del usuario que hizo login (el
    // del principal.id)
    // Por tanto, antes de llamar al método Spring Security evaluará esta expresión. Si el id es el mismo
    // se evaluará el método. Si no, Spring Security rechazará la petición y se disparará la respuesta Forbidden.
    @PutMapping("/users/{id:[0-9]+}")
    @PreAuthorize("#id == principal.id")
    void updateUser(@PathVariable long id) {

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
