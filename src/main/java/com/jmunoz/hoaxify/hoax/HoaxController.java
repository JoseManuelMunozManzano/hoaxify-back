package com.jmunoz.hoaxify.hoax;

import com.jmunoz.hoaxify.shared.CurrentUser;
import com.jmunoz.hoaxify.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/1.0")
public class HoaxController {

    @Autowired
    HoaxService hoaxService;

    @PostMapping("/hoaxes")
    HoaxVM createHoax(@Valid @RequestBody Hoax hoax, @CurrentUser User user) {
        return new HoaxVM(hoaxService.save(user, hoax));
    }

    @GetMapping("/hoaxes")
    Page<HoaxVM> getAllHoaxes(Pageable pageable) {
        return hoaxService.getAllHoaxes(pageable).map(HoaxVM::new);
    }

    @GetMapping("/users/{username}/hoaxes")
    Page<HoaxVM> getHoaxesOfUser(@PathVariable String username, Pageable pageable) {
        return hoaxService.getHoaxesOfUser(username, pageable).map(HoaxVM::new);
    }

    // Actualizamos tipo de retorno de Page<?> a ResponseEntity porque ahora también podemos devolver una lista.
    // Con ResponseEntity se pueden envolver diferentes tipos de response body.
    @GetMapping("/hoaxes/{id:[0-9]+}")
    ResponseEntity<?> getHoaxesRelative(@PathVariable long id, Pageable pageable,
                                        @RequestParam(name = "direction", defaultValue = "after") String direction) {
        if (!direction.equalsIgnoreCase("after")) {
            return ResponseEntity.ok(hoaxService.getOldHoaxes(id, pageable).map(HoaxVM::new));
        }

        List<Hoax> newHoaxes = hoaxService.getNewHoaxes(id, pageable);
        return ResponseEntity.ok(newHoaxes);
    }

    @GetMapping("/users/{username}/hoaxes/{id:[0-9]+}")
    Page<?> getHoaxesRelativeForUser(@PathVariable String username, @PathVariable Long id, Pageable pageable) {
        return hoaxService.getOldHoaxesOfUser(id, username, pageable).map(HoaxVM::new);
    }
}
