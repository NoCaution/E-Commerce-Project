package com.example.userservice.Controller;

import com.example.commonservice.Entity.APIResponse;
import com.example.commonservice.Service.CommonService;
import com.example.userservice.Entity.Dto.UpdateUserDto;
import com.example.userservice.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private CommonService commonService;


    @GetMapping("/getLoggedInUser")
    public APIResponse getLoggedInUser() {
        return userService.getLoggedInUser();
    }


    @PutMapping("/updateLoggedInUser")
    public APIResponse updateLoggedInUser(@RequestBody UpdateUserDto updateUserDto) {
        UUID authenticatedUserId = UUID.fromString(commonService.getLoggedInUserId());
        updateUserDto.setId(authenticatedUserId);

        return userService.updateUser(updateUserDto);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getUsers")
    public APIResponse getUsers() {
        return userService.getUsers();
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getUserById/{id}")
    public APIResponse getUserById(@PathVariable UUID id) {
        return userService.getUserById(id);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getUserByEmail/{email}")
    public APIResponse getUserByEmail(@PathVariable String email) {
        return userService.getUserByEmail(email);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/updateUser")
    public APIResponse updateUser(@RequestBody UpdateUserDto updateUserDto) {
        return userService.updateUser(updateUserDto);
    }
}
