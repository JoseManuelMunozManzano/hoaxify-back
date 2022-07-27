package com.jmunoz.hoaxify.hoax;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jmunoz.hoaxify.file.FileAttachment;
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

    // Relación muchos Hoaxes pueden pertenecer a un User
    @ManyToOne
    private User user;

    // Se establecen las relaciones entre Hoax y FileAttachment
    // Un fichero adjunto pertenece a un hoax
    @OneToOne(mappedBy = "hoax")
    private FileAttachment attachment;
}
