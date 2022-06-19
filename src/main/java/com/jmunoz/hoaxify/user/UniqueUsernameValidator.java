package com.jmunoz.hoaxify.user;

import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

// Indicamos la anotación y el tipo de objeto que se va a validar
public class UniqueUsernameValidator implements ConstraintValidator<UniqueUsername, String> {

    @Autowired
    UserRepository userRepository;

    // El primer parámetro es nuestro campo username del request
    // El segundo parámetro es el contexto. Se puede usar para actualizar el mensaje a generar para casos inválidos.
    // Si to-do va bien devuelve true. Si algo va devuelve false.
    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        User inDB = userRepository.findByUsername(value);
        if (inDB == null) {
            return true;
        }

        return false;
    }
}
