package com.example.basketservice.Security;

import com.example.commonservice.Service.CommonService;
import com.example.commonservice.Util.JWTUtil;
import com.example.commonservice.Util.JwtToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    @Value("${application.security.jwt.secret-key}")
    private String SECRET_KEY;

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private CommonService commonService;

    @Autowired
    private JwtToken jwtToken;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        String token;
        String authHeader = request.getHeader("Authorization");
        String id;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        token = authHeader.substring(7).trim();
        id = jwtUtil.extractUserId(token);

        if (id != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            User user = commonService.loadUserByUsername(token);
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

                //store token safely so hat token can be passed through inter-service communication
                jwtToken.setToken(SECRET_KEY, token);
            }
        }
        filterChain.doFilter(request, response);
    }
}
