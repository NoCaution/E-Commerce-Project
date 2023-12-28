package com.example.basketservice.Security;

import com.example.commonservice.Util.JWTUtil;
import com.example.commonservice.Util.JwtToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Component
public class JWTAuthenticationFilter extends OncePerRequestFilter {
    private final Logger logger = LoggerFactory.getLogger(JWTAuthenticationFilter.class);

    @Value("${application.security.jwt.secret-key}")
    private String SECRET_KEY;

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private JwtToken jwtToken;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        logger.info("getting auth header");
        String authHeader = request.getHeader("CustomAuthorization");
        String token;
        String headerId;
        String headerPassword;
        String headerRole;

        if (authHeader == null) {
            logger.info("auth header was null");
            filterChain.doFilter(request, response);
            return;
        }

        logger.info("getting auth header values");
        List<String> headerValues = Arrays.stream(authHeader.split(" ")).toList();

        try {
            token = headerValues.get(0);
            headerId = headerValues.get(1);
            headerPassword = headerValues.get(2);
            headerRole = headerValues.get(3);

        } catch (Exception e) {
            logger.error("error while getting header values: {}", e.getMessage(), e);
            filterChain.doFilter(request, response);
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            User user = new User(
                    headerId,
                    headerPassword,
                    Set.of(new SimpleGrantedAuthority(headerRole))
            );

            if (jwtUtil.isTokenValid(token, user)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        user.getAuthorities()
                );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);

                logger.info("authentication success");
                //store token safely so hat token can be passed through inter-service communication
                jwtToken.setToken(SECRET_KEY, token);
            }
        }
        filterChain.doFilter(request, response);
    }
}
