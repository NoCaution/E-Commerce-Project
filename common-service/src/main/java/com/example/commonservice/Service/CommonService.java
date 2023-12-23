package com.example.commonservice.Service;

import com.example.commonservice.Entity.Dto.UserDetailsDto;
import com.example.commonservice.Entity.Enum.Role;
import com.example.commonservice.Util.AppUtil;
import org.apache.http.client.fluent.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;


@Service
public class CommonService implements UserDetailsService {
    private final String USER_SERVICE_URL = "http://localhost:9000/api/user/";

    @Autowired
    private AppUtil appUtil;

    public String getLoggedInUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }

        return null;
    }

    public UserDetailsDto getLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            //org.springframework.security.core.userdetails.User
            User loggedInUser = (User) authentication.getPrincipal();
            Role role = Role.valueOf(Objects.requireNonNull(loggedInUser.getAuthorities().stream().findFirst().orElse(null)).getAuthority());

            return new UserDetailsDto(
                    UUID.fromString(loggedInUser.getUsername()),
                    loggedInUser.getPassword(),
                    role
            );
        }
        return null;
    }

    @Cacheable(value="user-cache",key="#result.username")
    @Override
    public org.springframework.security.core.userdetails.User loadUserByUsername(String token) throws UsernameNotFoundException {
        UserDetailsDto userDetailsDto = appUtil.sendRequest(Request.Get(USER_SERVICE_URL + "getLoggedInUser"), token, UserDetailsDto.class);

        return new org.springframework.security.core.userdetails.User(
                userDetailsDto.getId().toString(),
                userDetailsDto.getPassword(),
                Set.of(new SimpleGrantedAuthority(userDetailsDto.getRole().toString()))
        );
    }

}
