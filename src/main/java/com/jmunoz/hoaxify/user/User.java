package com.jmunoz.hoaxify.user;

import lombok.Data;

// Usando lombok para generar setters, getters, constructor vacío y método to toString
@Data
public class User {

    private String username;

    private String displayName;

    private String password;

}
