package com.example.commonservice.Service;

import com.example.commonservice.Entity.Dto.UserDetailsDto;
import com.example.commonservice.Entity.Dto.UserDto;
import com.example.commonservice.Util.AppUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Set;


@Service
public class CommonService implements UserDetailsService {
    private final String USER_SERVICE_URL = "http://localhost:9000/api/user/";

    @Autowired
    private AppUtil appUtil;

    public UserDto getLoggedInUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null && authentication.isAuthenticated()){
            return appUtil.sendRequest(USER_SERVICE_URL + "getUserByEmail/" + authentication.getName(), UserDto.class);
        }
        return null;
    }

    @Override
    public org.springframework.security.core.userdetails.User loadUserByUsername(String id) throws UsernameNotFoundException {
        UserDetailsDto userDetailsDto = appUtil.sendRequest(USER_SERVICE_URL + "getUserById/" + id,UserDetailsDto.class);

        return new org.springframework.security.core.userdetails.User(
                userDetailsDto.getId().toString(),
                userDetailsDto.getPassword(),
                Set.of(new SimpleGrantedAuthority(userDetailsDto.getRole().toString()))
        );
    }

}
