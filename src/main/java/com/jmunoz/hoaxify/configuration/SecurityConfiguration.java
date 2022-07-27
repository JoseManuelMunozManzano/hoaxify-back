package com.jmunoz.hoaxify.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

// Se usa @EnableGlobalMethodSecurity para habilitar seguridad a nivel de método. Ver UserController
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration {

    @Autowired
    AuthUserService authUserService;

    // Se ha deprecado extends WebSecurityConfiguredAdapter. Ver:
    // https://spring.io/blog/2022/02/21/spring-security-without-the-websecurityconfigureradapter
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws  Exception {
        http.csrf().disable();

        // Queremos ver como quedan los datos de User y Hoax en la BD H2.
        // H2 usa frames, pero ahora que usamos SpringSecurity, por defecto la configuración de SpringSecurity
        // bloquea las peticiones de carga de frames.
        // Actualizamos la configuración de nuestra seguridad para poder ver los datos en la consola H2
        // y ejecutamos la app.
        // Vamos a la ruta:
        // http://localhost:8080/h2-console
        // Y ejecutamos el SELECT de la tabla HOAX
        // Y en Postman ejecutamos Create Hoax
        // Volvemos a la consola H2 y volvemos a ejecutar el SELECT de la tabla HOAX.
        // Tras los arreglos ahora vemos en USER_ID de la tabla HOAX el valor 1 (el id del user). Correcto
        http.headers().disable();

        http.httpBasic().authenticationEntryPoint(new BasicAuthenticationEntryPoint());

        http
                .authorizeRequests()
                .antMatchers(HttpMethod.POST, "/api/1.0/login").authenticated()
                .antMatchers(HttpMethod.PUT, "/api/1.0/users/{id:[0-9]+}").authenticated()
                .antMatchers(HttpMethod.POST, "/api/1.0/hoaxes/**").authenticated()
                .and()
                .authorizeRequests().anyRequest().permitAll();

        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
