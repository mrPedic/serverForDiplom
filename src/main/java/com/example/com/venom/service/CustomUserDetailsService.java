package com.example.com.venom.service;

import com.example.com.venom.entity.UserEntity;
import com.example.com.venom.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            Long userId = Long.parseLong(username);
            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));

            return User.builder()
                    .username(user.getId().toString())
                    .password("") // Не используется для JWT
                    .roles(user.getRole().name())
                    .build();
        } catch (NumberFormatException e) {
            throw new UsernameNotFoundException("Invalid user id format: " + username);
        }
    }
}
