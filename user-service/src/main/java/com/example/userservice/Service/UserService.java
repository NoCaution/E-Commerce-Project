package com.example.userservice.Service;

import com.example.commonservice.Entity.APIResponse;
import com.example.commonservice.Entity.Dto.UserDetailsDto;
import com.example.commonservice.Entity.Dto.UserDto;
import com.example.commonservice.Service.CommonService;
import com.example.commonservice.Util.CustomMapper;
import com.example.userservice.Entity.Dto.UpdateUserDto;
import com.example.userservice.Entity.User;
import com.example.userservice.Repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {
    private final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CommonService commonService;

    @Autowired
    private CustomMapper customMapper;


    public APIResponse getLoggedInUser() {
        logger.info("getting authenticated user");
        String authenticatedUserId = commonService.getLoggedInUserId();

        logger.info("getting user by authenticated user id: {}", authenticatedUserId);
        User user = getById(UUID.fromString(authenticatedUserId));

        logger.info("getting loggedInUser success");
        UserDetailsDto userDetailsDto = customMapper.map(user, UserDetailsDto.class);
        return new APIResponse(
                HttpStatus.OK,
                "success",
                userDetailsDto
        );
    }


    public APIResponse getUserById(UUID id) {
        if (id == null) {
            logger.info("user id was null");
            return new APIResponse(
                    HttpStatus.BAD_REQUEST,
                    "user id can not be null"
            );
        }
        logger.info("getting user with id: {}", id);
        User user = getById(id);
        if (user == null) {
            logger.info("user was null with id: {}", id);
            return new APIResponse(
                    HttpStatus.NOT_FOUND,
                    "user not found"
            );
        }

        logger.info("get user success with id: {}", id);
        UserDto userDto = customMapper.map(user, UserDto.class);
        return new APIResponse(
                HttpStatus.OK,
                "success",
                userDto
        );
    }


    public APIResponse getUsers() {
        logger.info("getting users");
        List<User> userList = userRepository.findAll();
        if (userList.isEmpty()) {
            logger.info("no user found");
            return new APIResponse(
                    HttpStatus.OK,
                    "success",
                    userList
            );
        }

        logger.info("get users success");
        List<UserDto> userDtoList = customMapper.convertList(userList, UserDto.class);
        return new APIResponse(
                HttpStatus.OK,
                "success",
                userDtoList
        );
    }


    public APIResponse getUserByEmail(String email) {
        if (email == null) {
            logger.info("email was null");
            return new APIResponse(
                    HttpStatus.BAD_REQUEST,
                    "email can not be null"
            );
        }

        logger.info("getting user with email: {}", email);
        User user = userRepository.findUserByEmail(email);
        if (user == null) {
            logger.info("no user found for email: {}", email);
            return new APIResponse(
                    HttpStatus.NOT_FOUND,
                    "user not found"
            );
        }

        logger.info("get user success with email: {}", email);
        UserDto userDto = customMapper.map(user, UserDto.class);
        return new APIResponse(
                HttpStatus.OK,
                "success",
                userDto
        );
    }


    public APIResponse updateUser(UpdateUserDto updateUserDto) {
        if (updateUserDto.getId() == null) {
            logger.info("user id was null");
            return new APIResponse(
                    HttpStatus.BAD_REQUEST,
                    "user id can not be null"
            );
        }

        logger.info("getting user with id: {}", updateUserDto.getId());
        User user = getById(updateUserDto.getId());
        if (user == null) {
            logger.info("no user found for id: {}", updateUserDto.getId());
            return new APIResponse(
                    HttpStatus.NOT_FOUND,
                    "no user found"
            );
        }

        logger.info("updating user for id: {}", updateUserDto.getId());
        User updatedUser = updateUserFields(user, updateUserDto);

        logger.info("update user success with id: {}", updateUserDto.getId());
        userRepository.save(updatedUser);
        return new APIResponse(
                HttpStatus.OK,
                "success"
        );
    }


    @Cacheable(value = "user-cache", key = "#id")
    @Override
    public org.springframework.security.core.userdetails.User loadUserByUsername(String id) throws UsernameNotFoundException {
        UUID userId = UUID.fromString(id);
        User user = userRepository.findById(userId).orElse(new User());
        return new org.springframework.security.core.userdetails.User(
                id,
                user.getPassword(),
                user.getAuthorities()
        );
    }


    private User updateUserFields(User user, UpdateUserDto toBeUpdatedFields) {
        user.setEmail(toBeUpdatedFields.getEmail() == null ? user.getEmail() : toBeUpdatedFields.getEmail());
        user.setPassword(toBeUpdatedFields.getPassword() == null ? user.getPassword() : passwordEncoder.encode(toBeUpdatedFields.getPassword()));
        user.setFullName(toBeUpdatedFields.getFullName() == null ? user.getFullName() : toBeUpdatedFields.getFullName());
        user.setPhoneNumber(toBeUpdatedFields.getPhoneNumber() == null ? user.getPhoneNumber() : toBeUpdatedFields.getPhoneNumber());
        user.setRole(toBeUpdatedFields.getRole() == null ? user.getRole() : toBeUpdatedFields.getRole());
        return user;
    }


    private User getById(UUID id) {
        return userRepository.findById(id).orElse(null);
    }
}
