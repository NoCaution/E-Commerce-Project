package com.example.api.gateway.Entity;

import com.example.api.gateway.Entity.Enum.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailsDto implements Serializable {

    private UUID id;

    private String password;

    private Role role;
}
