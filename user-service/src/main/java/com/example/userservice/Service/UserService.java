package com.example.userservice.Service;

import com.example.userservice.Entity.Dto.UpdateUserDto;
import com.example.userservice.Entity.User;
import com.example.userservice.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public User getUserById(UUID id){
        return userRepository.findById(id).orElse(null);
    }

    public List<User> getUsers(){
        return userRepository.findAll();
    }

    public User getUserByEmail(String email){
        return userRepository.findUserByEmail(email);
    }

    public void updateUser(User user){
        userRepository.save(user);
    }

    @Override
    public org.springframework.security.core.userdetails.User loadUserByUsername(String id) throws UsernameNotFoundException {
        UUID userId = UUID.fromString(id);
        User user = getUserById(userId);
        return new org.springframework.security.core.userdetails.User(
                id,
                user.getPassword(),
                user.getAuthorities()
        );
    }

    public User updateUserFields(User user, UpdateUserDto toBeUpdatedFields) {
        user.setEmail(toBeUpdatedFields.getEmail() == null ? user.getEmail() : toBeUpdatedFields.getEmail());
        user.setPassword(toBeUpdatedFields.getPassword() == null ? user.getPassword() : passwordEncoder.encode(toBeUpdatedFields.getPassword()));
        user.setFullName(toBeUpdatedFields.getFullName() == null ? user.getFullName() : toBeUpdatedFields.getFullName());
        user.setPhoneNumber(toBeUpdatedFields.getPhoneNumber() == null ? user.getPhoneNumber() : toBeUpdatedFields.getPhoneNumber());
        user.setRole(toBeUpdatedFields.getRole() == null ? user.getRole() : toBeUpdatedFields.getRole());
        return user;
    }
}
