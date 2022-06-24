package com.jmunoz.hoaxify.shared;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// PARAMETER porque se va a usar en métodos
// @AuthenticationPrincipal obtiene el Principal de Authentication como se ha hecho en las formas 1 y 2
// en LoginController.
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@AuthenticationPrincipal
public @interface CurrentUser {
}
