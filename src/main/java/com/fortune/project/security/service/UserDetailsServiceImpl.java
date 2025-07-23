package com.fortune.project.security.service;

import com.fortune.project.entity.UserEntity;
import com.fortune.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByName(username).orElseThrow(() ->
                new UsernameNotFoundException("User with name: " + username + " not found!"));
        return UserDetailsImpl.build(user);
    }
}
