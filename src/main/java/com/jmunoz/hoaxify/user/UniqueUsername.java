package com.jmunoz.hoaxify.user;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

// @Constraint Busca la implementación de la validación
// @Target es para definir donde se puede usar la anotación. En fields.
// @Retention. Indicamos a la JVM que la anotación se procesa en tiempo de ejecución.
@Constraint(validatedBy = UniqueUsernameValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueUsername {

    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
