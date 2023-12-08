package com.example.commonservice.Service;

import com.example.commonservice.Entity.Dto.UserDetailsDto;
import com.example.commonservice.Entity.Dto.UserDto;
import com.example.commonservice.Util.AppUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Set;


@Service
public class CommonService{
    private final String USER_SERVICE_URL = "http://localhost:9000/user-service/api/user/getUserById";

    @Autowired
    private AppUtil appUtil;

    public UserDto getLoggedInUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null && authentication.isAuthenticated()){
            return appUtil.sendRequest(USER_SERVICE_URL + authentication.getName(), UserDto.class);
        }
        return null;
    }

    public org.springframework.security.core.userdetails.User loadUserByUsername(String id) throws UsernameNotFoundException {
        UserDetailsDto userDetailsDto = appUtil.sendRequest(USER_SERVICE_URL + "getUserById/" + id,UserDetailsDto.class);

        return new org.springframework.security.core.userdetails.User(
                userDetailsDto.getId().toString(),
                userDetailsDto.getPassword(),
                Set.of(new SimpleGrantedAuthority(userDetailsDto.getRole().toString()))
        );
    }

}
