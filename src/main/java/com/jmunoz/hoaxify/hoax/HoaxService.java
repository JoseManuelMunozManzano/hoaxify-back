package com.jmunoz.hoaxify.hoax;

import com.jmunoz.hoaxify.user.User;
import com.jmunoz.hoaxify.user.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Date;
import java.util.List;

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

    public Page<Hoax> getOldHoaxes(long id, String username, Pageable pageable) {
        // Usando Specification
        Specification<Hoax> spec = Specification.where(idLessThan(id));
        if (username == null) {
//            return hoaxRepository.findByIdLessThan(id, pageable);
            return hoaxRepository.findAll(spec, pageable);
        }

        User inDB = userService.getByUsername(username);
        // Combinando Specifications
//        return hoaxRepository.findByIdLessThanAndUser(id, inDB, pageable);
        return hoaxRepository.findAll(spec.and(userIs(inDB)), pageable);
    }

//    public Page<Hoax> getOldHoaxesOfUser(long id, String username, Pageable pageable) {
//        User inDB = userService.getByUsername(username);
//        return hoaxRepository.findByIdLessThanAndUser(id, inDB, pageable);
//    }

    public List<Hoax> getNewHoaxes(long id, String username, Pageable pageable) {
        if (username == null) {
            return hoaxRepository.findByIdGreaterThan(id, pageable.getSort());
        }

        User inDB = userService.getByUsername(username);
        return hoaxRepository.findByIdGreaterThanAndUser(id, inDB, pageable.getSort());
    }

//    public List<Hoax> getNewHoaxesOfUser(long id, String username, Pageable pageable) {
//        User inDB = userService.getByUsername(username);
//        return hoaxRepository.findByIdGreaterThanAndUser(id, inDB, pageable.getSort());
//    }

    public long getNewHoaxesCount(long id, String username) {
        if (username == null) {
            return hoaxRepository.countByIdGreaterThan(id);
        }

        User inDB = userService.getByUsername(username);
        return hoaxRepository.countByIdGreaterThanAndUser(id, inDB);
    }

//    public long getNewHoaxesCountOfUser(Long id, String username) {
//        User inDB = userService.getByUsername(username);
//        return hoaxRepository.countByIdGreaterThanAndUser(id, inDB);
//    }

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

}
