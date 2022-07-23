package com.jmunoz.hoaxify.hoax;

import com.jmunoz.hoaxify.user.vm.UserVM;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HoaxVM {

    private long id;

    private String content;

    // En objetos Hoax tenemos timestamp
    private long date;

    private UserVM user;

    public HoaxVM(Hoax hoax) {
        this.setId(hoax.getId());
        this.setContent(hoax.getContent());
        this.setDate(hoax.getTimestamp().getTime());
        this.setUser(new UserVM(hoax.getUser()));
    }
}
