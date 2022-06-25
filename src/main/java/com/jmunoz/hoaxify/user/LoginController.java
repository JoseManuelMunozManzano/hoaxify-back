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

    @PostMapping("/api/1.0/login")
    @JsonView(Views.Base.class)
    User handleLogin(@CurrentUser CustomUserDetails customUserDetails) {
        return userService.findUserByUsername(customUserDetails.getUsername());
    }
}
