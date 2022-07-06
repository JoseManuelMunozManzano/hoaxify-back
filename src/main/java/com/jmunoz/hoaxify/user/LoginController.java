package com.jmunoz.hoaxify.user;

import com.jmunoz.hoaxify.shared.CurrentUser;
import com.jmunoz.hoaxify.user.vm.UserVM;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class LoginController {

    @Autowired
    UserService userService;

    // Vamos a utilizar el enfoque VM (o DTO) y eliminar @JsonView
    @PostMapping("/api/1.0/login")
    UserVM handleLogin(@CurrentUser CustomUserDetails customUserDetails) {
        return new UserVM(userService.findUserByUsername(customUserDetails.getUsername()));
    }
}
