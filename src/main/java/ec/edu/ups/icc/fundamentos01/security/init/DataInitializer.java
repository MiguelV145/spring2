package ec.edu.ups.icc.fundamentos01.security.init;

import ec.edu.ups.icc.fundamentos01.security.RoleRepository;
import ec.edu.ups.icc.fundamentos01.security.models.RoleEntity;
import ec.edu.ups.icc.fundamentos01.security.models.RoleName;
import ec.edu.ups.icc.fundamentos01.users.models.UserEntity;
import ec.edu.ups.icc.fundamentos01.users.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // 1. Inicializar roles
        initializeRoles();

        // 2. Crear usuario admin por defecto
        createDefaultAdminUser();
    }

    private void initializeRoles() {
        logger.info("Inicializando roles...");

        for (RoleName roleName : RoleName.values()) {
            if (!roleRepository.existsByName(roleName)) {
                RoleEntity role = new RoleEntity(roleName, roleName.getDescription());
                roleRepository.save(role);
                logger.info("Rol creado: {}", roleName);
            }
        }

        logger.info("Roles inicializados correctamente");
    }

    private void createDefaultAdminUser() {
        logger.info("Verificando usuario administrador...");

        String adminEmail = "admin@ups.edu.ec";

        if (!userRepository.existsByEmail(adminEmail)) {
            UserEntity admin = new UserEntity();
            admin.setName("Administrador");
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode("admin123"));

            // Asignar rol ADMIN
            RoleEntity adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                    .orElseThrow(() -> new RuntimeException("Rol ADMIN no encontrado"));

            Set<RoleEntity> roles = new HashSet<>();
            roles.add(adminRole);
            admin.setRoles(roles);

            userRepository.save(admin);
            logger.info("Usuario administrador creado: {}", adminEmail);
        } else {
            logger.info("Usuario administrador ya existe");
        }
    }
}