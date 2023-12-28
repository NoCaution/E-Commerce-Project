package com.example.commonservice.Service;

import com.example.commonservice.Entity.Dto.UserDetailsDto;
import com.example.commonservice.Entity.Enum.Role;
import com.example.commonservice.Util.AppUtil;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    private final Logger logger = LoggerFactory.getLogger(CommonService.class);

    @Autowired
    private AppUtil appUtil;

    public String getLoggedInUserId() {
        logger.info("getting logged in user id");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }

        logger.info("authentication failed");
        return null;
    }

    public UserDetailsDto getLoggedInUser() {
        logger.info("getting logged in user");
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

        logger.info("authentcation failed");
        return null;
    }

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
