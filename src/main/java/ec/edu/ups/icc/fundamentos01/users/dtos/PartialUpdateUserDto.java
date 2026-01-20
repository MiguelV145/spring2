package ec.edu.ups.icc.fundamentos01.users.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public class PartialUpdateUserDto {

    @Size(min = 3, max = 150)
    public String name;

    @Email
    @Size(max = 150)
    public String email;

    @Size(min = 8)
    public String password;
}