package com.example.authservice.Service;

import com.example.authservice.Entity.Dto.*;
import com.example.authservice.Entity.User;
import com.example.authservice.Repository.AuthRepository;
import com.example.commonservice.Util.AppUtil;
import com.example.commonservice.Util.CustomMapper;
import com.example.commonservice.Util.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.Set;

@Service
public class AuthService{
    private final String USER_SERVICE_URL = "http://localhost:9000/api/user/";

    @Autowired
    private CustomMapper customMapper;

    @Autowired
    private AppUtil appUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthRepository authRepository;

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private AuthenticationManager authManager;


    private boolean isUniqeUser(String email){
            UserDto userDto = appUtil.sendRequest(USER_SERVICE_URL + "getUserByEmail/" + email, UserDto.class);
            return userDto == null;
    }

    public AuthenticationResponseDto register(RegistirationRequestDto requestDto){
        if(!isUniqeUser(requestDto.getEmail())){
            return new AuthenticationResponseDto(
                    true
            );
        }

        requestDto.setPassword(passwordEncoder.encode(requestDto.getPassword()));

        User user = customMapper.map(requestDto,User.class);
        user.setCreatedAt(new Date());
        authRepository.save(user);

        // this method gets the user-service url and returns the desired object;
        UserDetailsDto commonUserDto = appUtil.sendRequest(USER_SERVICE_URL + "getUserByEmail/" + requestDto.getEmail(), UserDetailsDto.class);

        org.springframework.security.core.userdetails.User userDetails = new org.springframework.security.core.userdetails.User(
                commonUserDto.getId().toString(),
                commonUserDto.getPassword(),
                Set.of(new SimpleGrantedAuthority(commonUserDto.getRole().toString()))
        );

        String token = jwtUtil.generateJwtToken(userDetails);
        return new AuthenticationResponseDto(
                token,
                false
        );
    }

    public AuthenticationResponseDto login(AuthenticationRequestDto requestDto){
        if(isUniqeUser(requestDto.getEmail())){
            return new AuthenticationResponseDto(
                    false
            );
        }

        User user = appUtil.sendRequest(USER_SERVICE_URL + "getUserByEmail/" + requestDto.getEmail(), User.class);
        if(!passwordEncoder.matches(requestDto.getPassword(),user.getPassword())){
            // user esixts but token will be null
            return new AuthenticationResponseDto();
        }
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getId(),
                        requestDto.getPassword()
                )
        );

        org.springframework.security.core.userdetails.User userDetails = new org.springframework.security.core.userdetails.User(
                user.getId().toString(),
                user.getPassword(),
                user.getAuthorities()
        );

        String token = jwtUtil.generateJwtToken(userDetails);
        return new AuthenticationResponseDto(
                token
        );
    }


}
