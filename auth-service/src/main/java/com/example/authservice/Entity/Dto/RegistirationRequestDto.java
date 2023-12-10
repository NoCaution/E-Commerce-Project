package com.example.authservice.Entity.Dto;

import com.example.authservice.Entity.Enum.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegistirationRequestDto {

    private String fullName;

    private String email;

    private String password;

    private Role role = Role.CUSTOMER;
}
