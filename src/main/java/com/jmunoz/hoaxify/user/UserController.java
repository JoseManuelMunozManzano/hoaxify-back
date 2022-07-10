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
    Page<UserVM> getUsers(@CurrentUser CustomUserDetails loggedInUser, @PageableDefault Pageable page) {
        User user = null;
        if (loggedInUser != null) {
            user = userService.findUserByUsername(loggedInUser.getUsername());
        }
        return userService.getUsers(user, page).map(UserVM::new);
    }

    @GetMapping("/users/{username}")
    UserVM getUserByName(@PathVariable String username) {
        User user = userService.getByUsername(username);
        return new UserVM(user);
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
