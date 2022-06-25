package com.jmunoz.hoaxify.user;

import com.jmunoz.hoaxify.shared.CurrentUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
public class LoginController {

    @Autowired
    UserService userService;

    @PostMapping("/api/1.0/login")
    Map<String, Object> handleLogin(@CurrentUser CustomUserDetails customUserDetails) {
        User loggedInUser = userService.findUserByUsername(customUserDetails.getUsername());

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", loggedInUser.getId());
        userMap.put("image", loggedInUser.getImage());
        userMap.put("displayName", loggedInUser.getDisplayName());

        return userMap;
    }
}
