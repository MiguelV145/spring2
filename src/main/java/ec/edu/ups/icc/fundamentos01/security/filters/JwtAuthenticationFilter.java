package ec.edu.ups.icc.fundamentos01.security.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import ec.edu.ups.icc.fundamentos01.security.config.JwtProperties;
import ec.edu.ups.icc.fundamentos01.security.service.UserDetailsServiceImpl;
import ec.edu.ups.icc.fundamentos01.security.utils.JwtUtil;

import java.io.IOException;


@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
     private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    
    private final JwtUtil jwtUtil; // Para validar y extraer datos del JWT
    private final UserDetailsServiceImpl userDetailsService; // Para cargar usuario desde BD
    private final JwtProperties jwtProperties; // Configuración JWT (header, prefix)

    public JwtAuthenticationFilter(JwtUtil jwtUtil,
            UserDetailsServiceImpl userDetailsService,
            JwtProperties jwtProperties) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.jwtProperties = jwtProperties;
    }

    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        try {
            
            String jwt = getJwtFromRequest(request);

            
            if (StringUtils.hasText(jwt) && jwtUtil.validateToken(jwt)) {

                
                String email = jwtUtil.getEmailFromToken(jwt);

               
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, // Principal (el usuario)
                        null, // Credentials (no necesarias)
                        userDetails.getAuthorities() // Authorities (roles/permisos)
                );

                
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

               
                SecurityContextHolder.getContext().setAuthentication(authentication);

                
                logger.debug("Usuario autenticado: {}", email);
            }

        } catch (Exception ex) {
            
            logger.error("No se pudo establecer la autenticación del usuario", ex);
        }


        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
       
        String bearerToken = request.getHeader(jwtProperties.getHeader());

        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(jwtProperties.getPrefix())) {
            return bearerToken.substring(jwtProperties.getPrefix().length());
        }

        return null;
    }
}