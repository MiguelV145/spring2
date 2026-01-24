package ec.edu.ups.icc.fundamentos01.security.models;

import java.util.HashSet;
import java.util.Set;

import ec.edu.ups.icc.fundamentos01.core.entities.BaseModel;
import ec.edu.ups.icc.fundamentos01.users.models.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

/**
 * ENTIDAD: Role (Rol de usuario)
 * 
 * Representa un rol en el sistema (ROLE_USER, ROLE_ADMIN, ROLE_MODERATOR).
 * Se relaciona ManyToMany con usuarios → Un usuario puede tener múltiples roles.
 * 
 * Tabla en BD: roles
 * Tabla intermedia: user_roles (creada automáticamente por JPA)
 */
@Entity
@Table(name = "roles")  // Nombre de la tabla en PostgreSQL
public class RoleEntity  extends BaseModel{

    
    /**
     * Nombre del rol (enum para type-safety)
     * 
     * @Enumerated(EnumType.STRING): Guarda "ROLE_USER" en lugar de ordinal (0, 1, 2)
     * nullable = false: Campo obligatorio
     * unique = true: No pueden existir roles duplicados
     * length = 50: Máximo 50 caracteres en BD
     * 
     * Ejemplo en BD: "ROLE_USER", "ROLE_ADMIN"
     */
    @Column(nullable = false, unique = true, length = 50)
    @Enumerated(EnumType.STRING)  // Guardar nombre del enum, no el número
    private RoleName name;

    /**
     * Descripción del rol (opcional)
     * 
     * Ejemplo: "Usuario estándar con permisos básicos"
     */
    @Column(length = 200)
    private String description;

    /**
     * Relación INVERSA con usuarios (bidireccional)
     * 
     * @ManyToMany(mappedBy = "roles"): 
     *   - mappedBy indica que UserEntity es el DUEÑO de la relación
     *   - UserEntity tiene @JoinTable que crea la tabla intermedia user_roles
     * 
     * fetch = FetchType.LAZY: 
     *   - NO carga los usuarios automáticamente al consultar un rol
     *   - Se cargan solo cuando se accede a role.getUsers()
     *   - Mejora performance (evita cargar datos innecesarios)
     * 
     * Set<UserEntity>:
     *   - Set (no List) para evitar duplicados
     *   - HashSet por defecto (orden no importa)
     * 
     * Ejemplo:
     * RoleEntity adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN);
     * Set<UserEntity> admins = adminRole.getUsers();  // ← Aquí se carga desde BD
     */
    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    private Set<UserEntity> users = new HashSet<>();

    // ============== CONSTRUCTORES ==============

    /**
     * Constructor vacío (REQUERIDO por JPA)
     * JPA usa reflexión para crear instancias
     */
    public RoleEntity() {
    }

    /**
     * Constructor con nombre de rol
     * Útil para crear roles en DataInitializer
     */
    public RoleEntity(RoleName name) {
        this.name = name;
    }

    /**
     * Constructor completo
     * Útil para crear roles con descripción
     */
    public RoleEntity(RoleName name, String description) {
        this.name = name;
        this.description = description;
    }

    public RoleName getName() {
        return name;
    }

    public void setName(RoleName name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<UserEntity> getUsers() {
        return users;
    }

    public void setUsers(Set<UserEntity> users) {
        this.users = users;
    }

    // ============== GETTERS Y SETTERS ==============
    
   
}
