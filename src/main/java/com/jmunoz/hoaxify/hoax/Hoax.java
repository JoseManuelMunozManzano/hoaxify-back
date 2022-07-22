package com.jmunoz.hoaxify.hoax;

import com.jmunoz.hoaxify.user.User;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

@Data
@Entity
public class Hoax {

    @Id
    @GeneratedValue
    private long id;

    // Para indicar la longitud máxima del campo se usa la anotación de JPA @Column y se indica la propiedad
    // length
    @NotNull
    @Size(min = 10, max = 5000)
    @Column(length = 5000)
    private String content;

    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    // En la BD H2 no se puede usar el nombre de tabla User
    @Column(name = "Users")
    private User user;
}
