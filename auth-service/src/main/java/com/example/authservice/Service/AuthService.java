package com.example.authservice.Service;


import com.example.authservice.Entity.Dto.AuthenticationRequestDto;
import com.example.authservice.Entity.Dto.RegistirationRequestDto;
import com.example.authservice.Entity.User;
import com.example.authservice.Repository.AuthRepository;
import com.example.commonservice.Entity.APIResponse;
import com.example.commonservice.Entity.Dto.UserDetailsDto;
import com.example.commonservice.Util.AppUtil;
import com.example.commonservice.Util.CustomMapper;
import com.example.commonservice.Util.JWTUtil;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class AuthService {
    private final String BASKET_SERVICE_URL = "http://localhost:9000/basket-service/api/basket/";

    private final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private CustomMapper customMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthRepository authRepository;

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private AppUtil appUtil;


    public APIResponse register(RegistirationRequestDto requestDto) {
        logger.info("checking email: {}", requestDto.getEmail());
        if (!isUniqeUser(requestDto.getEmail())) {
            logger.info("email:{} linked to another account", requestDto.getEmail());
            return new APIResponse(
                    HttpStatus.BAD_REQUEST,
                    "this email linked to another account."
            );
        }

        //encode password
        requestDto.setPassword(passwordEncoder.encode(requestDto.getPassword()));

        //create user
        User user = customMapper.map(requestDto, User.class);
        logger.info("saving user with id: {}", user.getId());
        User createdUser = authRepository.save(user);

        UserDetailsDto userDetailsDto = customMapper.map(createdUser, UserDetailsDto.class);

        org.springframework.security.core.userdetails.User userDetails = new org.springframework.security.core.userdetails.User(
                userDetailsDto.getId().toString(),
                userDetailsDto.getPassword(),
                Set.of(new SimpleGrantedAuthority(userDetailsDto.getRole().toString()))
        );

        //create token
        logger.info("creating token");
        String token = jwtUtil.generateJwtToken(userDetails);
        if (token == null) {
            logger.error("error while creating token for user: {}", userDetails);
            return new APIResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "token generate failed"
            );
        }

        //send request to basket service to initialize user's basket
        Request request = Request.Post(BASKET_SERVICE_URL + "initializeLoggedInUserBasket");
        APIResponse basketServiceResponse = appUtil.sendRequest(request, token);
        if (basketServiceResponse.getHttpStatus() != HttpStatus.CREATED) {
            logger.info("user registered but basket not initialized");
            return new APIResponse(
                    HttpStatus.CREATED,
                    "user register success but initialize basket failed",
                    token
            );
        }

        logger.info("user register and initialize basket success");
        return new APIResponse(
                HttpStatus.CREATED,
                "success",
                token
        );
    }

    public APIResponse login(AuthenticationRequestDto requestDto) {
        if (isUniqeUser(requestDto.getEmail())) {
            logger.info("email: {} was incorrect", requestDto.getEmail());
            return new APIResponse(
                    HttpStatus.UNAUTHORIZED,
                    "email incorrect"
            );
        }

        //get user and check if password matches
        logger.info("getting user with email: {}", requestDto.getEmail());
        User user = getUserByEmail(requestDto.getEmail());

        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            logger.info("password didnt match");
            return new APIResponse(
                    HttpStatus.UNAUTHORIZED,
                    "email or password incorrect"
            );
        }

        org.springframework.security.core.userdetails.User userDetails = new org.springframework.security.core.userdetails.User(
                user.getId().toString(),
                user.getPassword(),
                user.getAuthorities()
        );

        //generate token
        logger.info("generating token");
        String token = jwtUtil.generateJwtToken(userDetails);
        if (token == null) {
            logger.error("error while creating token for user: {}", userDetails);
            return new APIResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "error creating token"
            );
        }

        logger.info("login success for user: {}", userDetails);
        return new APIResponse(
                HttpStatus.OK,
                "success",
                token
        );
    }

    private User getUserByEmail(String email) {
        return authRepository.findUserByEmail(email);
    }

    private boolean isUniqeUser(String email) {
        User user = getUserByEmail(email);
        return user == null;
    }
}
