package ec.edu.ups.icc.fundamentos01.users.models;

import ec.edu.ups.icc.fundamentos01.users.dtos.PartialUpdateUserDto;
import ec.edu.ups.icc.fundamentos01.users.dtos.UpdateUserDto;

public class User {

    private int id;
    private String name;
    private String email;
    private String password; // no se expone en la API
    private String createdAt;

    // Constructor privado para forzar uso de factory methods
    public User(int id, String name, String email, String password) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.createdAt = java.time.LocalDateTime.now().toString();
    }


    // ==================== FACTORY METHODS ====================
   /**
     * Crea un User desde una entidad persistente
     * @param entity Entidad recuperada de la BD
     * @return instancia de User para lógica de negocio
     */
    public static User fromEntity(UserEntity entity) {
        return new User(
            entity.getId().intValue(),
            entity.getName(),
            entity.getEmail(),
            entity.getPassword()
        );
    }

        /**
     * Convierte este User a una entidad persistente
     * @return UserEntity lista para guardar en BD
     */
    public UserEntity toEntity() {
        UserEntity entity = new UserEntity();
        if (this.id > 0) {
            entity.setId((long) this.id);
        }
        entity.setName(this.name);
        entity.setEmail(this.email);
        entity.setPassword(this.password);
        return entity;
    }


    public User update(UpdateUserDto dto) {
    this.name = dto.name;
    this.email = dto.email;
    return this;
}
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    } 

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }


    public User partialUpdate(PartialUpdateUserDto dto) {
       
    if (dto.name != null) {
        this.name = dto.name;
    }

    if (dto.email != null) {
        this.email = dto.email;
    }

      if (dto.password != null) {
        this.password = dto.password;
    }

    return this;
    }


}