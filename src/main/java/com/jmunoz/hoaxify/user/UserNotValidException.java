package com.jmunoz.hoaxify.user;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// Cuando se lance esta excepción, Spring la tratará y generará un campo response genérico como resultado.
// Y por defecto devolverá un status code 500
// Para obtener el status code que queremos (en el test buscamos BAD_REQUEST) se anota con @ResponseStatus

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UserNotValidException extends RuntimeException {
}
