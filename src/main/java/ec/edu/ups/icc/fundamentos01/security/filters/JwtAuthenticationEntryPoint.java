package ec.edu.ups.icc.fundamentos01.security.filters;


import com.fasterxml.jackson.databind.ObjectMapper;

import ec.edu.ups.icc.fundamentos01.exceptions.response.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

 
@Component 
public class JwtAuthenticationEntryPoint implements  AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);


    private final ObjectMapper objectMapper;

    /**
     * Constructor: Inyección de dependencias
     * 
     * Spring inyecta su ObjectMapper configurado
     */
    public JwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

   
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

     
        logger.error("Error de autenticación: {}", authException.getMessage());

        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.UNAUTHORIZED,  // Status 401
            "Token de autenticación inválido o no proporcionado. " +
                "Debe incluir un token válido en el header Authorization: Bearer <token>",
            request.getRequestURI()   // Path del endpoint (ej: /api/products)
        );

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);


        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
