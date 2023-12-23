package com.example.commonservice.Config;

import com.example.commonservice.Entity.Dto.UserDetailsDto;
import com.example.commonservice.Service.CommonService;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

@Component
public class AuditorAwareImpl implements AuditorAware<User> {
    @Autowired
    private CommonService commonService;

    @NonNull
    @Override
    public Optional<User> getCurrentAuditor() {
        UserDetailsDto userDetailsDto = commonService.getLoggedInUser();
        return Optional.of(new User(
                userDetailsDto.getId().toString(),
                userDetailsDto.getPassword(),
                Set.of(new SimpleGrantedAuthority(userDetailsDto.getRole().toString()))
        ));
    }
}
