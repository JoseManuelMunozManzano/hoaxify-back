package com.jmunoz.hoaxify.user;

import com.jmunoz.hoaxify.hoax.Hoax;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
// IMPORTANTE ESTE IMPORT
import java.beans.Transient;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Collection;
import java.util.List;

@Data
@Entity(name = "Users")
public class User implements UserDetails {

    private static final long serialVersionUID = 4074374728582967483L;

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

    // SOLUCION PARA NO ENVIAR EL PASSWORD (FORMA 2 FUNCIONA)
    // 1. Con @JsonIgnore no enviamos el password
    // Pero esto rompe algunos tests porque Jackson no solo ignora el campo para la conversión de Object a Json,
    // sino que también ignora el campo para la conversión de Json a Object.
    // Cuando mapeamos Json a nuestro objeto user, como el campo password es ignorado, Json lo descarta y deja
    // con null su valor en la instancia de usuario.
    // Como en nuestros tests hemos indicado que password no puede ser nulo, falla.
    // Esta solución funciona cuando realmente no queremos ni enviar ni recibir un campo en Json.
    // En este caso no queremos el password cuando construimos el Json en el servidor desde el objeto, pero
    // lo necesitamos cuando el cliente lo envía en la petición sign up.
    //
    // 2. JSONView. Se usa para indicar a la vista en el que el campo se incluirá en la serialización y deserialización.
    // Es muy útil cuando se necesita incluir diferentes campos de serialización de archivos de objetos para diferentes
    // casos de uso.
    // Para usarlo tenemos que definir tipos View, que son básicamente interfaces vacías. Ver la interface Views.java.
    // También anotar con @JsonView los campos indicando a que interface corresponden.
    // Y también hace falta en nuestro LoginController, donde por ahora estamos enviando to-do nuestro user, decirle
    // a Spring que queremos que esta respuesta sea serializada de una forma customizada (usando JsonView de nuevo)
    @NotNull
    @Size(min = 8, max = 255)
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", message = "{hoaxify.constraints.password.Pattern.message}")
    private String password;

    private String image;

    // Un User puede tener mucho Hoaxes
    // Estableciendo la carga de datos como EAGER (no LAZY) nuestro test funciona
    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    private List<Hoax> hoaxes;

    @Override
    @Transient
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return AuthorityUtils.createAuthorityList("Role_USER");
    }

    @Override
    @Transient
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @Transient
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @Transient
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @Transient
    public boolean isEnabled() {
        return true;
    }
}
