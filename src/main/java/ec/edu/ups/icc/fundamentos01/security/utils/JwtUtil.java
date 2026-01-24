package ec.edu.ups.icc.fundamentos01.security.utils;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import ec.edu.ups.icc.fundamentos01.security.config.JwtProperties;
import ec.edu.ups.icc.fundamentos01.security.service.UserDetailsImpl;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtUtil {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    private final JwtProperties jwtProperties;
    private final SecretKey key;


    public JwtUtil(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;

        this.key=Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
    }


    public String generateToken (Authentication authentication){
        
        
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        Date now = new Date();  // Fecha actual
        Date expiryDate = new Date(now.getTime() + jwtProperties.getExpiration());

        String roles = userPrincipal.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)  // Extrae "ROLE_USER", "ROLE_ADMIN"
            .collect(Collectors.joining(","));

        return Jwts.builder().subject(String.valueOf(userPrincipal.getId()))
        .claim("email", userPrincipal.getEmail()) 
        .claim("name", userPrincipal.getName()) 
        .claim("roles", roles )
        .issuer(jwtProperties.getIssuer())
        .issuedAt(now)
        .expiration(expiryDate)
        .signWith(key, Jwts.SIG.HS256) 
        .compact();
    }


    public String generateTokenFromUserDetails(UserDetailsImpl userDetails){
        Date now= new Date();
        Date expiryDate= new Date(now.getTime()+ jwtProperties.getExpiration());

        String roles = userDetails.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(","));

        return Jwts.builder()
        .subject(String.valueOf(userDetails.getId()))
        .claim("email", userDetails.getEmail()) 
        .claim("email", userDetails.getName()) 
        .claim(roles, roles)
        .issuer(jwtProperties.getIssuer())
        .issuedAt(now)
        .expiration(expiryDate)
        .signWith(key, Jwts.SIG.HS256)
        .compact();
    }



    public Long getUserIdFromToken(String token ){
        Claims claims=Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .getPayload();

        return Long.parseLong(claims.getSubject());

    }


    public String getEmailFromToken(String token){
         Claims claims = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload();

        // Extraer claim "email" como String
        return claims.get("email", String.class);
    }

        public boolean validateToken(String authToken) {
        try {
            // Intenta parsear el token
            // Si algo falla, lanza excepción
            Jwts.parser()
                .verifyWith(key)              // Verifica firma con nuestra clave
                .build()
                .parseSignedClaims(authToken);
            
            // Si llegamos aquí, el token es VÁLIDO
            return true;
            
        } catch (SignatureException ex) {
            // Firma inválida: Token modificado o clave incorrecta
            // Ejemplo: Alguien cambió el payload pero no puede firmar correctamente
            logger.error("Firma JWT inválida: {}", ex.getMessage());
            
        } catch (MalformedJwtException ex) {
            // Token malformado: No tiene estructura correcta (header.payload.signature)
            // Ejemplo: "abc123" en lugar de token válido
            logger.error("Token JWT malformado: {}", ex.getMessage());
            
        } catch (ExpiredJwtException ex) {
            // Token expirado: Pasaron más de 30 minutos desde su creación
            // Ejemplo: Token creado a las 10:00, ahora son las 10:35
            logger.error("Token JWT expirado: {}", ex.getMessage());
            
        } catch (UnsupportedJwtException ex) {
            // Token no soportado: Usa algoritmo que no soportamos
            // Ejemplo: Token firmado con RS256 pero esperamos HS256
            logger.error("Token JWT no soportado: {}", ex.getMessage());
            
        } catch (IllegalArgumentException ex) {
            // Claims vacío: Token sin payload
            logger.error("JWT claims string está vacío: {}", ex.getMessage());
        }
        
        // Si cayó en cualquier catch, el token es INVÁLIDO
        return false;
    }

    
}


