package com.jmunoz.hoaxify.hoax;

import com.jmunoz.hoaxify.user.User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class HoaxSecurityService {

    HoaxRepository hoaxRepository;

    // De nuevo, para facilitar los tests, se inyecta en el constructor el repository
    public HoaxSecurityService(HoaxRepository hoaxRepository) {
        this.hoaxRepository = hoaxRepository;
    }

    public boolean isAllowedToDelete(long hoaxId, User loggedInUser) {
        Optional<Hoax> optionalHoax = hoaxRepository.findById(hoaxId);
        if (optionalHoax.isPresent()) {
            Hoax inDB = optionalHoax.get();
            return inDB.getUser().getId() == loggedInUser.getId();
        }

        return false;
    }
}
