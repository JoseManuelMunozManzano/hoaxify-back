package com.jmunoz.hoaxify.user;

import com.jmunoz.hoaxify.configuration.AuthUserService;
import com.jmunoz.hoaxify.shared.CurrentUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@RestController
@Slf4j
public class LoginController {

    @Autowired
    UserService userService;

    @PostMapping("/api/1.0/login")
    Map<String, Object> handleLogin(@CurrentUser CustomUserDetails customUserDetails) {
        // Necesitamos obtener el usuario que hace log. Podemos hacerlo de 2 formas:
        // 1. Recuperándolo de SecurityContextHolder
        // 2. Pidiendo a Spring que inyecte la autenticación a nuestro método
        // 3. Inyectando el objeto de usuario a nuestros métodos usando una anotación que creamos
        // (Ver CurrentUser en el package shared).
        User user = userService.findUserByUsername(customUserDetails.getUsername());

        // Devolvemos el id para que pase el test
        return Collections.singletonMap("id", user.getId());
    }
}
