package com.jmunoz.hoaxify.hoax;

import org.springframework.data.jpa.repository.JpaRepository;

public interface HoaxRepository extends JpaRepository<Hoax, Long> {

    long countByUserUsername(String username);
}
