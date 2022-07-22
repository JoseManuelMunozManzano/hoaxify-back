package com.jmunoz.hoaxify.hoax;

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

    @NotNull
    @Size(min = 10)
    private String content;

    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;
}
