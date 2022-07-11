package com.jmunoz.hoaxify.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
public class SecurityConfiguration {

    @Autowired
    AuthUserService authUserService;

    // Se ha deprecado extends WebSecurityConfiguredAdapter. Ver:
    // https://spring.io/blog/2022/02/21/spring-security-without-the-websecurityconfigureradapter
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws  Exception {
        http.csrf().disable();

        // Con esto ya no se manda en el header WWW-Authenticate.
        // Hemos sobreescrito el comportamiento de BasicAuthentication.
        // Por defecto Spring usa BasicAuthenticationEntryPoint. Si miramos su implementación, vemos que
        // añade a la cabecera de la respuesta WWW-Authenticate.
        // Esto NO FUNCIONA.
        // Tenemos que crear nuestro EntryPoint customizado. Ver en el package configuration la clase
        // BasicAuthenticationEntryPoint
        // Y se sustituye como parámetro
        http.httpBasic().authenticationEntryPoint(new BasicAuthenticationEntryPoint());

        http
                .authorizeRequests()
                .antMatchers(HttpMethod.POST, "/api/1.0/login").authenticated()
                // Se añade a la configuración de la seguridad el endpoint con PUT
                .antMatchers(HttpMethod.PUT, "/api/1.0/users/{id:[0-9]+}").authenticated()
                .and()
                .authorizeRequests().anyRequest().permitAll();

        // Un servidor REST debería ser stataless, es decir, la petición actual no se tiene que ve afectada por
        // una petición anterior.
        // No esperamos ninguna petición HTTP para hacer ninguna tarea. Es orientado a una sola tarea.
        // Esta definición de stateless es desde el punto de vista de negocio.
        // Pero esto no significa que no podamos ser echados desde el backend si generamos un tráfico abusivo.
        //
        // Spring Security crea una sesión por cada usuario y comienza userDetails en este objeto de sesión.
        // No necesitamos sesión porque no estamos construyendo una app tradicional de renderizado en cliente.
        // Nos centramos en un comportamiento stateless y no implementamos nuestra app usando on objeto de sesión.
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        return http.build();
    }

    // Creamos este bean porque solo nos hace falta una instancia para usar en la app
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // El método configure se ha convertido en este bean
    // https://stackoverflow.com/questions/72381114/spring-security-upgrading-the-deprecated-websecurityconfigureradapter-in-spring
    // In the old version you inject AuthenticationManagerBuilder, set userDetailsService, passwordEncoder and build it.
    // But authenticationManager is already created in this step.
    // It is created the way we wanted (with userDetailsService and the passwordEncoder).
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
//    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//        // Para que Spring compare las credenciales de entrada con la almacenada en BD tenemos que tener
//        // el mismo encoding (bcrypt)
//        auth.userDetailsService(authUserService).passwordEncoder(passwordEncoder());
//    }
}
