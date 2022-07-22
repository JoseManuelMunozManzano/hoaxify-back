package com.jmunoz.hoaxify.hoax;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/1.0")
public class HoaxController {

    @Autowired
    HoaxService hoaxService;

    @PostMapping("/hoaxes")
    void createHoax(@Valid @RequestBody Hoax hoax) {
        hoaxService.save(hoax);
    }
}
