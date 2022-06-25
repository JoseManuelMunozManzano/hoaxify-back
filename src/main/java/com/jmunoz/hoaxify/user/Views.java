package com.jmunoz.hoaxify.user;

// Estas Views solo contienen las interfaces que usaremos para definir los tipos view.
// Podemos tener relaciones jerárquicas entre estas interfaces.
public class Views {

    public interface Base{}

    // Esto significa que si decimos a Jackson que serialice los campos de la view Sensitive,
    // los campos de la view Base también se van a serializar en el proceso.
    public interface Sensitive extends Base {}
}
