package com.jmunoz.hoaxify.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    // Aquí si podemos inyectar en el campo porque no hace falta hacer unit testing sobre clases controller.
    @Autowired
    UserService userService;

    // Con @RequestBody, Spring tomará el body JSON recibido durante la petición y lo convertirá en objeto User.
    // Para realizar estas conversiones Spring usa la biblioteca Jackson.
    @PostMapping("/api/1.0/users")
    void createUser(@RequestBody User user) {
        userService.save(user);
    }
}
