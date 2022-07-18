package com.jmunoz.hoaxify.user.vm;

import com.jmunoz.hoaxify.shared.ProfileImage;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class UserUpdateVM {

    @NotNull
    @Size(min = 4, max = 255)
    private String displayName;

    @ProfileImage
    private String image;
}
