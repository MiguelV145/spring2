package ec.edu.ups.icc.fundamentos01.security.models;

public enum RoleName {
    ROLE_USER("Usuario estándar con permisos básicos"),
    ROLE_ADMIN("Administrador con permisos completos"),
    ROLE_MODERATOR("Moderador con permisos intermedios");

    private final String description;

    RoleName(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
