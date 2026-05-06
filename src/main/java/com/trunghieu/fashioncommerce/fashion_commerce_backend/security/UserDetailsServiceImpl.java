package com.trunghieu.fashioncommerce.fashion_commerce_backend.security;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.User userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // Chuyển đổi User entity của bạn thành CustomUserDetails
        return new CustomUserDetails(
                userEntity.getId(), // Truyền ID vào CustomUserDetails
                userEntity.getUsername(),
                userEntity.getPasswordHash(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + userEntity.getRole().getName().name()))
        );
    }
}
