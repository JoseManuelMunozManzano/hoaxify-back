package com.jmunoz.hoaxify.hoax;

import com.jmunoz.hoaxify.user.User;
import com.jmunoz.hoaxify.user.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class HoaxService {

    HoaxRepository hoaxRepository;

    UserService userService;

    // Inyectado en el constructor.
    // En las clases Service escogimos inyección en constructor, ya que Spring creará una instancia de esta clase
    // HoaxService, llamará a este constructor y verá que el constructor busca HoaxRepository y suministrará la
    // instancia de HoaxRepository
    public HoaxService(HoaxRepository hoaxRepository, UserService userService) {
        this.hoaxRepository = hoaxRepository;
        this.userService = userService;
    }

    public Hoax save(User user, Hoax hoax) {
        hoax.setTimestamp(new Date());
        hoax.setUser(user);
        return hoaxRepository.save(hoax);
    }

    public Page<Hoax> getAllHoaxes(Pageable pageable) {
        return hoaxRepository.findAll(pageable);
    }

    public Page<Hoax> getHoaxesOfUser(String username, Pageable pageable) {
        User inDB = userService.getByUsername(username);
        return hoaxRepository.findByUser(inDB, pageable);
    }

    public Page<Hoax> getOldHoaxes(long id, Pageable pageable) {
        return hoaxRepository.findByIdLessThan(id, pageable);
    }
}
