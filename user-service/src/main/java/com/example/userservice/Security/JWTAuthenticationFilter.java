package com.example.userservice.Security;

import com.example.commonservice.Util.JWTUtil;
import com.example.userservice.Service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private UserService userService;


    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        String token;
        String authHeader = request.getHeader("CustomAuthorization");
        String id;

        if (authHeader == null) {
            filterChain.doFilter(request, response);
            return;
        }
        List<String> headerValues = Arrays.stream(authHeader.split(" ")).toList();
        token = headerValues.get(0);
        String headerId = null;
        String headerPassword = null;
        String headerRole = null;

        if(headerValues.size() > 1){
            headerId = headerValues.get(1);
            headerPassword = headerValues.get(2);
            headerRole = headerValues.get(3);
        }

        id = jwtUtil.extractUserId(token);

        if (id != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            User user;
            if(headerId == null || headerPassword == null || headerRole == null){
                user = userService.loadUserByUsername(id);
            } else {
                user = new User(
                        headerId,
                        headerPassword,
                        Set.of(new SimpleGrantedAuthority(headerRole))
                );
            }

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
            }
        }
        filterChain.doFilter(request, response);
    }
}
