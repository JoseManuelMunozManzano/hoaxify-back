package com.jmunoz.hoaxify.user;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

// Usando lombok para generar setters, getters, constructor vacío y método to toString
// Se pone el nombre Users porque en H2 User es palabra reservada y da error al hacer consultas sobre dicho nombre.
@Data
@Entity(name = "Users")
public class User {

    // Por defecto, el strategy en AUTO
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String username;

    private String displayName;

    private String password;

}