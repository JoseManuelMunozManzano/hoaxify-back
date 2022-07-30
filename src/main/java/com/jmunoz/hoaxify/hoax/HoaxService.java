package com.jmunoz.hoaxify.hoax;

import com.jmunoz.hoaxify.file.FileAttachment;
import com.jmunoz.hoaxify.file.FileAttachmentRepository;
import com.jmunoz.hoaxify.user.User;
import com.jmunoz.hoaxify.user.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class HoaxService {

    HoaxRepository hoaxRepository;

    UserService userService;

    FileAttachmentRepository fileAttachmentRepository;

    // Inyectado en el constructor.
    // En las clases Service escogimos inyección en constructor, ya que Spring creará una instancia de esta clase
    // HoaxService, llamará a este constructor y verá que el constructor busca HoaxRepository y suministrará la
    // instancia de HoaxRepository
    public HoaxService(HoaxRepository hoaxRepository, UserService userService, FileAttachmentRepository fileAttachmentRepository) {
        this.hoaxRepository = hoaxRepository;
        this.userService = userService;
        this.fileAttachmentRepository = fileAttachmentRepository;
    }

    public Hoax save(User user, Hoax hoax) {
        hoax.setTimestamp(new Date());
        hoax.setUser(user);
        // Se utiliza hoax.getAttachment para saber que hay un adjunto, pero no se puede utilizar
        // para actualizar.
        // Tenemos que recoger el FileAttachment que hay en BD, con todos los campos cargados, y ese
        // se asigna a hoax.
        if (hoax.getAttachment() != null) {
            FileAttachment inDB = fileAttachmentRepository.findById(hoax.getAttachment().getId()).get();
            inDB.setHoax(hoax);
            hoax.setAttachment(inDB);
        }
        return hoaxRepository.save(hoax);
    }

    public Page<Hoax> getAllHoaxes(Pageable pageable) {
        return hoaxRepository.findAll(pageable);
    }

    public Page<Hoax> getHoaxesOfUser(String username, Pageable pageable) {
        User inDB = userService.getByUsername(username);
        return hoaxRepository.findByUser(inDB, pageable);
    }

    public Page<Hoax> getOldHoaxes(long id, String username, Pageable pageable) {
        Specification<Hoax> spec = Specification.where(idLessThan(id));
        if (username != null) {
            User inDB = userService.getByUsername(username);
            spec = spec.and(userIs(inDB));
        }
        return hoaxRepository.findAll(spec, pageable);
    }

    public List<Hoax> getNewHoaxes(long id, String username, Pageable pageable) {
        Specification<Hoax> spec = Specification.where(idGreaterThan(id));
        if (username != null) {
            User inDB = userService.getByUsername(username);
            spec = spec.and(userIs(inDB));
        }
        return hoaxRepository.findAll(spec, pageable.getSort());
    }

    public long getNewHoaxesCount(long id, String username) {
        Specification<Hoax> spec = Specification.where(idGreaterThan(id));
        if (username != null) {
            User inDB = userService.getByUsername(username);
            spec = spec.and(userIs(inDB));
        }

        return hoaxRepository.count(spec);
    }

    // Definiendo Specification
    // Se crean especificaciones para id y username para usarlo en queries dinámicas en HoaxRepository
    //
    // Chequeamos la igualdad de user.
    // El primer parámetro en el valor que obtenemos de nuestros resultados de la query.
    //    root es el objeto Hoax, y obtenemos el campo user.
    // El segundo parámetro es el objeto user que tenemos como parámetro en el método
    private Specification<Hoax> userIs(User user) {
        // Como función lambda
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("user"), user);

        // Sin usar lambda
//        return new Specification<Hoax>() {
//            @Override
//            public Predicate toPredicate(Root<Hoax> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
//
//                return criteriaBuilder.equal(root.get("user"), user);
//            }
//        };
    }

    private Specification<Hoax> idLessThan(long id) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.lessThan(root.get("id"), id);
    }

    private Specification<Hoax> idGreaterThan(long id) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThan(root.get("id"), id);
    }

    public void deleteHoax(long id) {
        hoaxRepository.deleteById(id);
    }
}
