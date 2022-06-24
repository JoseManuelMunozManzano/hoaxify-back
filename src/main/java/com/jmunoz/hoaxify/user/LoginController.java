package com.jmunoz.hoaxify.user;

import com.jmunoz.hoaxify.configuration.AuthUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    Map<String, Object> handleLogin() {
        // Necesitamos obtener el usuario que hace log. Podemos hacerlo de 2 formas:
        // 1. Recuperándolo de SecurityContextHolder
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userService.findUserByUsername(customUserDetails.getUsername());

        // Devolvemos el id para que pase el test
        return Collections.singletonMap("id", user.getId());
    }
}
