package ec.edu.ups.icc.fundamentos01.users.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateUserDto {

    @NotBlank
    @Size(min = 3, max = 150)
    public String name;

    @NotBlank
    @Email
    @Size(max = 150)
    public String email;

    @NotBlank
    @Size(min = 8)
    public String password;
}