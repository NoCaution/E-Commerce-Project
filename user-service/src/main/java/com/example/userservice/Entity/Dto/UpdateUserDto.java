package com.example.userservice.Entity.Dto;

import com.example.userservice.Entity.Enum.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserDto {

    private UUID id;

    private String fullName;

    private String email;

    private String password;

    private String phoneNumber;

    private Role role;
}
