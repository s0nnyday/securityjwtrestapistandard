package com.example.securityjwtrestapistandard.service;

import com.example.securityjwtrestapistandard.entity.UserEntity;
import com.example.securityjwtrestapistandard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public Optional<UserEntity> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
