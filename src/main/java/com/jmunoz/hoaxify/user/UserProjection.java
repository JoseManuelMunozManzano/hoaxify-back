package com.jmunoz.hoaxify.user;

// Projection proporciona opciones flexibles de generación de nuestros modelos.
// En esta interfase se añaden los getters de los campos que queremos y lo usamos en nuestro repository
public interface UserProjection {

    long getId();

    String getUsername();

    String getDisplayName();

    String getImage();
}
