package com.example.userservice.Controller;

import com.example.commonservice.Entity.APIResponse;
import com.example.commonservice.Entity.Dto.UserDto;
import com.example.commonservice.Service.CommonService;
import com.example.commonservice.Util.CustomMapper;
import com.example.userservice.Entity.Dto.UpdateUserDto;
import com.example.userservice.Service.UserService;
import com.example.userservice.Entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private CustomMapper customMapper;
    @Autowired
    private CommonService commonService;

    @Procedure("this is to get the authenticated user, id will be taken automatically")
    @GetMapping("/getLoggedInUser")
    public APIResponse getLoggedInUser() {
        String authenticatedUserId = commonService.getLoggedInUserId();
        if (authenticatedUserId == null) {
            return new APIResponse(
                    HttpStatus.NETWORK_AUTHENTICATION_REQUIRED,
                    "authentication required"
            );
        }

        User user = userService.getUserById(UUID.fromString(authenticatedUserId));

        UserDto userDto = customMapper.map(user, UserDto.class);
        return new APIResponse(
                HttpStatus.OK,
                "success",
                userDto
        );
    }

    @PutMapping("/updateLoggedInUser")
    public APIResponse updateLoggedInUser(@RequestBody UpdateUserDto updateUserDto) {
        String authenticatedUserId = commonService.getLoggedInUserId();
        if (authenticatedUserId == null) {
            return new APIResponse(
                    HttpStatus.NETWORK_AUTHENTICATION_REQUIRED,
                    "authentication required"
            );
        }

        User user = userService.getUserById(UUID.fromString(authenticatedUserId));
        User updatedUser = userService.updateUserFields(user, updateUserDto);

        userService.updateUser(updatedUser);
        return new APIResponse(
                HttpStatus.OK,
                "success"
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getUsers")
    public APIResponse getUsers() {
        List<User> userList = userService.getUsers();
        if (userList.isEmpty()) {
            return new APIResponse(
                    HttpStatus.NOT_FOUND,
                    "no user found"
            );
        }

        List<UserDto> userDtoList = customMapper.convertList(userList, UserDto.class);
        return new APIResponse(
                HttpStatus.OK,
                "success",
                userDtoList
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getUserById/{id}")
    public APIResponse getUserById(@PathVariable UUID id) {

        if (id == null) {
            return new APIResponse(
                    HttpStatus.BAD_REQUEST,
                    "given id is not legit"
            );
        }

        User user = userService.getUserById(id);
        if (user == null) {
            return new APIResponse(
                    HttpStatus.NOT_FOUND,
                    "user not found"
            );
        }

        UserDto userDto = customMapper.map(user, UserDto.class);
        return new APIResponse(
                HttpStatus.OK,
                "success",
                userDto
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getUserByEmail/{email}")
    public APIResponse getUserByEmail(@PathVariable String email) {
        if (email == null) {
            return new APIResponse(
                    HttpStatus.BAD_REQUEST,
                    "given email is not legit"
            );
        }

        User user = userService.getUserByEmail(email);
        if (user == null) {
            return new APIResponse(
                    HttpStatus.NOT_FOUND,
                    "user not found"
            );
        }

        UserDto userDto = customMapper.map(user, UserDto.class);
        return new APIResponse(
                HttpStatus.OK,
                "success",
                userDto
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/updateUser")
    public APIResponse updateUser(@RequestBody UpdateUserDto updateUserDto) {
        if (updateUserDto.getId() == null) {
            return new APIResponse(
                    HttpStatus.BAD_REQUEST,
                    "given user is not legit"
            );
        }

        User user = userService.getUserById(updateUserDto.getId());
        if (user == null) {
            return new APIResponse(
                    HttpStatus.NOT_FOUND,
                    "user not found"
            );
        }

        //updates the fields of the given user with null safety and returns this user.
        User updatedUser = userService.updateUserFields(user, updateUserDto);
        userService.updateUser(updatedUser);
        return new APIResponse(
                HttpStatus.OK,
                "success"
        );
    }
}
