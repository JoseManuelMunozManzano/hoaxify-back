package com.jmunoz.hoaxify.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
@Entity(name = "Users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    // Usamos nuestro custom Constraint @UniqueUsername
    @NotNull(message = "{hoaxify.constraints.username.NotNull.message}")
    @Size(min = 4, max = 255)
    @UniqueUsername
    private String username;

    @NotNull
    @Size(min = 4, max = 255)
    private String displayName;

    // SOLUCION PARA NO ENVIAR EL PASSWORD (NO FUNCIONA)
    // 1. Con @JsonIgnore no enviamos el password
    // Pero esto rompe algunos tests porque Jackson no solo ignora el campo para la conversión de Object a Json,
    // sino que también ignora el campo para la conversión de Json a Object.
    // Cuando mapeamos Json a nuestro objeto user, como el campo password es ignorado, Json lo descarta y deja
    // con null su valor en la instancia de usuario.
    // Como en nuestros tests hemos indicado que password no puede ser nulo, falla.
    // Esta solución funciona cuando realmente no queremos ni enviar ni recibir un campo en Json.
    // En este caso no queremos el password cuando construimos el Json en el servidor desde el objeto, pero
    // lo necesitamos cuando el cliente lo envía en la petición sign up.
    @NotNull
    @Size(min = 8, max = 255)
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", message = "{hoaxify.constraints.password.Pattern.message}")
    @JsonIgnore
    private String password;

    private String image;
}
