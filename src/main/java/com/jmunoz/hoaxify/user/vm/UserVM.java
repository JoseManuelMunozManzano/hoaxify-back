package com.jmunoz.hoaxify.user.vm;

import com.jmunoz.hoaxify.user.User;
import lombok.Data;
import lombok.NoArgsConstructor;

// Se añaden los campos que queremos que tenga este modelo
@Data
@NoArgsConstructor
public class UserVM {
    private long id;

    private String username;

    private String displayName;

    private String image;

    // Informamos un constructor que tomará el objeto User como parámetro
    public UserVM(User user) {
        this.setId(user.getId());
        this.setUsername(user.getUsername());
        this.setDisplayName(user.getDisplayName());
        this.setImage(user.getImage());
    }
}
