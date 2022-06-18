package com.jmunoz.hoaxify.user;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

// Usando lombok para generar setters, getters, constructor vacío y método to toString
// Se pone el nombre Users porque en H2 User es palabra reservada y da error al hacer consultas sobre dicho nombre.
@Data
@Entity(name = "Users")
public class User {

    // Por defecto, el strategy en AUTO
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    // Haciendo override del mensaje de error para poner el que queramos
    @NotNull(message = "Username cannot be null")
    @Size(min = 4, max = 255)
    private String username;

    @NotNull
    @Size(min = 4, max = 255)
    private String displayName;

    @NotNull
    @Size(min = 8, max = 255)
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$")
    private String password;

}
