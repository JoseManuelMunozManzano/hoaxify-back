package com.jmunoz.hoaxify.hoax;

import com.jmunoz.hoaxify.shared.CurrentUser;
import com.jmunoz.hoaxify.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/1.0")
public class HoaxController {

    @Autowired
    HoaxService hoaxService;

    @PostMapping("/hoaxes")
    void createHoax(@Valid @RequestBody Hoax hoax, @CurrentUser User user) {
        hoaxService.save(user, hoax);
    }

    @GetMapping("/hoaxes")
    Page<HoaxVM> getAllHoaxes(Pageable pageable) {
        return hoaxService.getAllHoaxes(pageable).map(HoaxVM::new);
    }
}
