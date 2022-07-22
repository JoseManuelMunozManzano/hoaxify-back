package com.jmunoz.hoaxify.hoax;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

// Como vamos a crear la interface HoaxRepository, actualizamos nuestra clase de Hoax indicando que es una entidad JPA
// para la persistencia y creamos nuestro id
@Data
@Entity
public class Hoax {

    @Id
    @GeneratedValue
    private long id;

    private String content;

    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;
}
