package com.jmunoz.hoaxify.user;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Collection;

// La interfaz UserDetails viene con métodos para el manejo de la cuenta de un usuario genérico.
// Tenemos usuario y password y nos faltan los roles.
// Con los cambios realizados, nuestro Entity User es también del tipo UserDetails.
@Data
@Entity(name = "Users")
public class User implements UserDetails {

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

    @NotNull
    @Size(min = 8, max = 255)
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", message = "{hoaxify.constraints.password.Pattern.message}")
    private String password;

    // Como esta clase User se usa para mapear a tabla de BD, estos nuevos métodos afectarán a dicha tabla User.
    // Tenemos que excluirlos.
    // Para no tener en BD un rol se usa la anotación @Transient.
    @Override
    @Transient
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return AuthorityUtils.createAuthorityList("Role_USER");
    }

    // Estos campos se usan en Spring Security para verificar si la cuenta esta activa, ha expirado...
    // Por ahora devolvemos true y también añadimos @Transient.
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
