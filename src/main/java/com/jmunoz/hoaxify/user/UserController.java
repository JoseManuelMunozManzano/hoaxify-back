package com.jmunoz.hoaxify.user;

import com.jmunoz.hoaxify.shared.CurrentUser;
import com.jmunoz.hoaxify.shared.GenericResponse;
import com.jmunoz.hoaxify.user.vm.UserUpdateVM;
import com.jmunoz.hoaxify.user.vm.UserVM;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

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

    @PutMapping("/users/{id:[0-9]+}")
    @PreAuthorize("#id == principal.id")
    UserVM updateUser(@PathVariable long id, @Valid @RequestBody(required = false) UserUpdateVM userUpdate) {
        User updated = userService.update(id, userUpdate);
        return new UserVM((updated));
    }
}
