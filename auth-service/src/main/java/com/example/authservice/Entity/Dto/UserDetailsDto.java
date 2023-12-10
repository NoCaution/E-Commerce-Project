package com.example.authservice.Entity.Dto;

import com.example.authservice.Entity.Enum.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailsDto {

    private UUID id;

    private String password;

    private Role role;
}
