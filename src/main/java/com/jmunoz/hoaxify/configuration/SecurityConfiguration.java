package com.jmunoz.hoaxify.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
public class SecurityConfiguration {

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
                .authorizeRequests().antMatchers(HttpMethod.POST, "/api/1.0/login").authenticated()
                .and()
                .authorizeRequests().anyRequest().permitAll();

        return http.build();
    }
}
