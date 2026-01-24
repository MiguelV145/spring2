package ec.edu.ups.icc.fundamentos01.security.service;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ec.edu.ups.icc.fundamentos01.exceptions.domain.BadRequestException;
import ec.edu.ups.icc.fundamentos01.exceptions.domain.ConflictException;
import ec.edu.ups.icc.fundamentos01.security.RoleRepository;
import ec.edu.ups.icc.fundamentos01.security.dto.AuthResponseDto;
import ec.edu.ups.icc.fundamentos01.security.dto.LoginRequestDto;
import ec.edu.ups.icc.fundamentos01.security.dto.RegisterRequestDto;
import ec.edu.ups.icc.fundamentos01.security.models.RoleEntity;
import ec.edu.ups.icc.fundamentos01.security.models.RoleName;
import ec.edu.ups.icc.fundamentos01.security.utils.JwtUtil;
import ec.edu.ups.icc.fundamentos01.users.models.UserEntity;
import ec.edu.ups.icc.fundamentos01.users.repository.UserRepository;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

     private final AuthenticationManager authenticationManager; // Valida credenciales
    private final UserRepository userRepository;               // Acceso a BD
    private final RoleRepository roleRepository;               // Gestión de roles
    private final PasswordEncoder passwordEncoder;             // Hash de passwords
    private final JwtUtil jwtUtil;                            // Generación de tokens

    public AuthService(AuthenticationManager authenticationManager,
                       UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Login: Valida credenciales y retorna JWT
     */
    @Transactional(readOnly = true) // Solo lectura, no modifica BD
    public AuthResponseDto login(LoginRequestDto loginRequest) {
        
        // 1. Validar email y password con Spring Security
        // authenticationManager usa UserDetailsService internamente
        // Si falla: lanza BadCredentialsException → 401
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getEmail(),
                loginRequest.getPassword()
            )
        );

        // 2. Establecer usuario autenticado en contexto de seguridad
        // Permite acceso a usuario actual en servicios
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. Generar JWT con datos del usuario
        String jwt = jwtUtil.generateToken(authentication);

        // 4. Extraer información del usuario autenticado
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Convertir authorities a Set<String> para la respuesta
        Set<String> roles = userDetails.getAuthorities().stream()
            .map(item -> item.getAuthority()) // "ROLE_USER", "ROLE_ADMIN"
            .collect(Collectors.toSet());

        // 5. Retornar JWT + datos del usuario
        return new AuthResponseDto(
            jwt,                      // Token para autenticación
            userDetails.getId(),      // ID del usuario
            userDetails.getName(),    // Nombre completo
            userDetails.getEmail(),   // Email
            roles                     // Roles asignados
        );
    }

    /**
     * Registro: Crea nuevo usuario y retorna JWT automáticamente
     */
    @Transactional // Requiere transacción para INSERT
    public AuthResponseDto register(RegisterRequestDto registerRequest) {
        
        // 1. Validar que email no exista
        // Si existe: lanza ConflictException → 409
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new ConflictException("El email ya está registrado");
        }

        // 2. Crear nueva entidad de usuario
        UserEntity user = new UserEntity();
        user.setName(registerRequest.getName());
        user.setEmail(registerRequest.getEmail());
        // Hash del password con BCrypt (nunca almacenar en texto plano)
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

        // 3. Asignar rol por defecto ROLE_USER
        // Si no existe: lanza BadRequestException → 400
        RoleEntity userRole = roleRepository.findByName(RoleName.ROLE_USER)
            .orElseThrow(() -> new BadRequestException("Rol por defecto no encontrado"));

        Set<RoleEntity> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);

        // 4. Guardar en BD (INSERT)
        user = userRepository.save(user);

        // 5. Generar JWT automáticamente para login directo
        // No requiere que el usuario haga login después de registrarse
        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        String jwt = jwtUtil.generateTokenFromUserDetails(userDetails);

        // Convertir roles a nombres de string
        Set<String> roleNames = user.getRoles().stream()
            .map(role -> role.getName().name()) // RoleName.ROLE_USER → "ROLE_USER"
            .collect(Collectors.toSet());

        // 6. Retornar JWT + datos del usuario registrado
        return new AuthResponseDto(
            jwt,
            user.getId(),
            user.getName(),
            user.getEmail(),
            roleNames
        );
    }
    
}
