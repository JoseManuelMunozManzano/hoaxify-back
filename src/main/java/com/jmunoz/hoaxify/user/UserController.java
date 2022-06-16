package com.jmunoz.hoaxify.user;

import com.jmunoz.hoaxify.shared.GenericResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @Autowired
    UserService userService;

    // Se corrige para que pase el test
    @PostMapping("/api/1.0/users")
    GenericResponse createUser(@RequestBody User user) {
        userService.save(user);

        GenericResponse body = new GenericResponse();
        body.setMessage("User saved");
        return body;
    }
}
