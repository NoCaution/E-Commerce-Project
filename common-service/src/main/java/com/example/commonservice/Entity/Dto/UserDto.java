package com.example.commonservice.Entity.Dto;

import com.example.commonservice.Entity.Enum.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {

    private UUID id;

    private String fullName;

    private String email;

    private String phoneNumber;

    private Role role;

}
