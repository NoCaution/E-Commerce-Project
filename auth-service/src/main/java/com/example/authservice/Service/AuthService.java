package com.example.authservice.Service;


import com.example.authservice.Entity.Dto.AuthenticationRequestDto;
import com.example.authservice.Entity.Dto.AuthenticationResponseDto;
import com.example.authservice.Entity.Dto.RegistirationRequestDto;
import com.example.authservice.Entity.User;
import com.example.authservice.Repository.AuthRepository;
import com.example.commonservice.Entity.Dto.UserDetailsDto;
import com.example.commonservice.Util.CustomMapper;
import com.example.commonservice.Util.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Set;

@Service
public class AuthService{
    @Autowired
    private CustomMapper customMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthRepository authRepository;

    @Autowired
    private JWTUtil jwtUtil;


    private boolean isUniqeUser(String email) {
        User user = getUserByEmail(email);
        return user == null;
    }

    public AuthenticationResponseDto register(RegistirationRequestDto requestDto) {
        if (!isUniqeUser(requestDto.getEmail())) {
            return new AuthenticationResponseDto(
                    true
            );
        }

        requestDto.setPassword(passwordEncoder.encode(requestDto.getPassword()));

        User user = customMapper.map(requestDto, User.class);
        user.setCreatedAt(new Date());
        authRepository.save(user);

        UserDetailsDto userDetailsDto = customMapper.map(getUserByEmail(requestDto.getEmail()), UserDetailsDto.class);

        org.springframework.security.core.userdetails.User userDetails = new org.springframework.security.core.userdetails.User(
                userDetailsDto.getId().toString(),
                userDetailsDto.getPassword(),
                Set.of(new SimpleGrantedAuthority(userDetailsDto.getRole().toString()))
        );

        String token = jwtUtil.generateJwtToken(userDetails);
        return new AuthenticationResponseDto(
                token,
                false
        );
    }

    public AuthenticationResponseDto login(AuthenticationRequestDto requestDto) {
        if (isUniqeUser(requestDto.getEmail())) {
            return new AuthenticationResponseDto(
                    false
            );
        }

        User user = getUserByEmail(requestDto.getEmail());
        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            // user esixts but token will be null
            return new AuthenticationResponseDto();
        }

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

    private User getUserByEmail(String email) {
        return authRepository.findUserByEmail(email);
    }


}
