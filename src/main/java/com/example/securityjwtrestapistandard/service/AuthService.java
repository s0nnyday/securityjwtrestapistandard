package com.example.securityjwtrestapistandard.service;

import com.example.securityjwtrestapistandard.dto.*;
import com.example.securityjwtrestapistandard.entity.UserEntity;
import com.example.securityjwtrestapistandard.repository.UserRepository;
import com.example.securityjwtrestapistandard.security.CustomUserDetails;
import com.example.securityjwtrestapistandard.security.JwtIssuer;
import com.example.securityjwtrestapistandard.util.Constants;
import com.example.securityjwtrestapistandard.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtIssuer jwtIssuer;
    private final AuthenticationManager authenticationManager;

    public Object attemptLogin(String email, String password) {

        // 존재하지 않는 이메일
        if (userRepository.findByEmail(email).isEmpty()) {
            return LoginErrorResponse.builder()
                    .status(401) // Unauthorized
                    .success(false)
                    .message(Constants.NON_EXISTENT_EMAIL)
                    .error("INVALID_CREDENTIALS")
                    .build();
        }
        // 로그인 시작
        try {
            var authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
            SecurityContextHolder.getContext().setAuthentication(authentication); // SecurityContext 에 authenticaiton 저장
            var principal = (CustomUserDetails) authentication.getPrincipal();
            log.info("로그인 CustomUserDetails:" + principal.toString());
            var roles = principal.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();

            var token = jwtIssuer.issue(principal.getUserId(), principal.getEmail(), roles);
            // 로그인
            return LoginSuccessResponse.builder()
                    .status(200)
                    .success(true)
                    .message(Constants.LOGIN_SUCCESS_MESSAGE)
                    .accessToken(token)
                    .build();
        } catch (BadCredentialsException e) {
            // 잘못된 아이디 또는 비밀번호
            log.error("Login failed: ", e);
            return LoginErrorResponse.builder()
                    .status(401) // Unauthorized
                    .success(false)
                    .message(Constants.INVALID_USERNAME_OR_PASSWORD_MESSAGE)
                    .error("INVALID_CREDENTIALS")
                    .build();
        } catch (AuthenticationException e) {
            // 로그인 실패 처리
            log.error("Login failed: ", e);
            return LoginErrorResponse.builder()
                    .status(401)
                    .success(false)
                    .message(Constants.LOGIN_ERROR_MESSAGE)
                    .error("Invalid credentials")
                    .build();
        }
    }


    public Object registerUser(RegisterRequest request) {
        try {
            //예외 처리
            if (!ValidationUtil.isValidEmail(request.getEmail())) {
                return RegisterErrorResponse.builder()
                        .status(400)
                        .success(false)
                        .message(Constants.INVALID_EMAIL_FORMAT)
                        .build();
            }

            if (!ValidationUtil.isValidPassword(request.getPassword())) {
                return RegisterErrorResponse.builder()
                        .status(400)
                        .success(false)
                        .message(Constants.INVALID_PASSWORD_FORMAT)
                        .build();
            }

            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                return RegisterErrorResponse.builder()
                        .status(400)
                        .success(false)
                        .message(Constants.EMAIL_ALREADY_EXISTS)
                        .build();
            }

            //회원 등록 시작
            UserEntity newUser = UserEntity.createUser(request.getEmail(), passwordEncoder.encode(request.getPassword()), "");

            userRepository.save(newUser);

            return RegisterSuccessResponse.builder()
                    .status(200)
                    .success(true)
                    .message(Constants.SUCCESSFUL_REGISTRATION)
                    .user(RegisterSuccessResponse.UserResponse.builder()
                            .userId(newUser.getId())
                            .email(newUser.getEmail())
                            .role(String.valueOf(newUser.getUserRole()))
                            .build())
                    .build();
        } catch (DataIntegrityViolationException e) {
            // 데이터베이스 무결성과 관련된 예외 처리
            log.error("Error during user registration:", e);
            return RegisterErrorResponse.builder()
                    .status(500)
                    .success(false)
                    .message(Constants.REGISTRATION_ERROR_MESSAGE)
                    .build();
        }
    }

}
