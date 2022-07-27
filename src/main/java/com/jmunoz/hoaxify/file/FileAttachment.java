package com.jmunoz.hoaxify.file;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

// Como hemos creado el repository esta Clase la necesitamos como @Entity y creamos el campo id
// También guardamos la fecha como Timestamp
@Data
@Entity
public class FileAttachment {

    @Id
    @GeneratedValue
    private long id;

    @Temporal(TemporalType.TIMESTAMP)
    private Date date;

    private String name;

    private String fileType;
}
