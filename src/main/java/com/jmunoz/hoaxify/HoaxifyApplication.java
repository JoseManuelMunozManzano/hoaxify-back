package com.jmunoz.hoaxify;

import com.jmunoz.hoaxify.user.User;
import com.jmunoz.hoaxify.user.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import java.util.stream.IntStream;

@SpringBootApplication
public class HoaxifyApplication {

	public static void main(String[] args) {
		SpringApplication.run(HoaxifyApplication.class, args);
	}

	// Vamos a crear 15 usuarios de forma temporal para poder listarlos. Luego eliminaremos esto.
	//
	// CommandLineRunner es un objeto especial de Spring. Cuando pidamos a Spring que cree este bean,
	// Spring lo ejecutará tras inicializar la aplicación.
	//
	// @Profile("!test")
	// No queremos que esta parte de código se ejecute cuando estamos ejecutando los tests, porque
	// se insertarían usuarios en BD y les afectaría.
	// No olvidar que en nuestros tests incluimos: @ActiveProfiles("test") y aquí decimos que esto se
	// ejecutará cuando no haya test
	@Bean
	@Profile("!test")
	CommandLineRunner run(UserService userService) {
		return (args) -> {
				IntStream.rangeClosed(1, 15)
						.mapToObj(i -> {
							User user = new User();
							user.setUsername("user" + i);
							user.setDisplayName("display" + i);
							user.setPassword("P4ssword" + i);
							return user;
						})
						.forEach(userService::save);
		};
	}
}
