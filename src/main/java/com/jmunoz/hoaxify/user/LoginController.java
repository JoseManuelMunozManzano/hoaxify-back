package com.jmunoz.hoaxify.user;

import com.fasterxml.jackson.annotation.JsonView;
import com.jmunoz.hoaxify.shared.CurrentUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class LoginController {

    @Autowired
    UserService userService;

    // Sobre Projection, indicar que estamos devolviendo aquí esta versión limitada de nuestro objeto User.
    // Si descartamos la opción JsonView y continuamos con Projection, necesitaremos convertir nuestro objeto User
    // en nuestra implementación UserProjection, así que necesitaremos una implementación concreta de UserProjection.
    @PostMapping("/api/1.0/login")
    @JsonView(Views.Base.class)
    User handleLogin(@CurrentUser CustomUserDetails customUserDetails) {
        return userService.findUserByUsername(customUserDetails.getUsername());
    }
}
