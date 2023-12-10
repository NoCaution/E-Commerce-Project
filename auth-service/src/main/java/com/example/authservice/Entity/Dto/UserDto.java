package com.example.authservice.Entity.Dto;

import com.example.authservice.Entity.Enum.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private UUID id;

    private String fullName;

    private String email;

    private String phoneNumber;

    private Role role;
}
