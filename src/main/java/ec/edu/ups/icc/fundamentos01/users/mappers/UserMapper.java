package ec.edu.ups.icc.fundamentos01.users.mappers;


import ec.edu.ups.icc.fundamentos01.users.dtos.CreateUserDto;
import ec.edu.ups.icc.fundamentos01.users.dtos.UpdateUserDto;
import ec.edu.ups.icc.fundamentos01.users.dtos.UserResponseDto;
import ec.edu.ups.icc.fundamentos01.users.models.User;

public class UserMapper {

    public static User toModel(int id, String name, String email) {
        return new User(id, name, email, "secret");
    }

    // DTO -> Model
    public static User fromCreateDto(CreateUserDto dto) {
        return new User(0, dto.name, dto.email, dto.password);
    }   
    public static User fromUpdateDto(UpdateUserDto dto) {
        return new User(0, dto.name, dto.email, dto.password);
    }   


    public static UserResponseDto toResponse(User user) {
         UserResponseDto dto = new UserResponseDto();
        dto.id = user.getId();
        dto.name = user.getName();
        dto.email = user.getEmail();
        return dto;
    }
}