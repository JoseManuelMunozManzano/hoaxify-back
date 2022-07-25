package com.jmunoz.hoaxify.hoax;

import com.jmunoz.hoaxify.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HoaxRepository extends JpaRepository<Hoax, Long> {

    Page<Hoax> findByUser(User user, Pageable pageable);

    Page<Hoax> findByIdLessThan(Long id, Pageable pageable);

    List<Hoax> findByIdGreaterThan(Long id, Sort sort);

    Page<Hoax> findByIdLessThanAndUser(Long id, User user, Pageable pageable);
}
