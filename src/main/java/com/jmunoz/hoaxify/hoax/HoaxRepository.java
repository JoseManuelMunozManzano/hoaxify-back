package com.jmunoz.hoaxify.hoax;

import com.jmunoz.hoaxify.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

// Generando queries dinámicas con JPA
// Para poder usar Specification (ver HoaxService) tenemos que extender también de JPASpecificationExecutor
public interface HoaxRepository extends JpaRepository<Hoax, Long>, JpaSpecificationExecutor<Hoax> {

    Page<Hoax> findByUser(User user, Pageable pageable);
}
