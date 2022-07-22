package com.jmunoz.hoaxify.hoax;

import org.springframework.stereotype.Service;

@Service
public class HoaxService {

    HoaxRepository hoaxRepository;

    // Inyectado en el constructor.
    // En las clases Service escogimos inyección en constructor, ya que Spring creará una instancia de esta clase
    // HoaxService, llamará a este constructor y verá que el constructor busca HoaxRepository y suministrará la
    // instancia de HoaxRepository
    public HoaxService(HoaxRepository hoaxRepository) {
        this.hoaxRepository = hoaxRepository;
    }

    public void save(Hoax hoax) {
        hoaxRepository.save(hoax);
    }
}
