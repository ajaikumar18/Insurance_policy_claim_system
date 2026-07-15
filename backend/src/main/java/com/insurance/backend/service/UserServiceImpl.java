package com.insurance.backend.service;

import com.insurance.backend.dto.*;
import com.insurance.backend.entity.Role;
import com.insurance.backend.entity.User;
import com.insurance.backend.exception.AmlCheckFailedException;
import com.insurance.backend.exception.EmailAlreadyExistsException;
import com.insurance.backend.repository.UserRepository;
import com.insurance.backend.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AmlService amlService;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;
    private final AuditLogService auditLogService;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           AmlService amlService,
                           JwtTokenProvider tokenProvider,
                           AuthenticationManager authenticationManager,
                           AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.amlService = amlService;
        this.tokenProvider = tokenProvider;
        this.authenticationManager = authenticationManager;
        this.auditLogService = auditLogService;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + request.getEmail()));

        return new AuthResponse(jwt, user.getEmail(), user.getUsername(), user.getRole().name());
    }

    @Override
    @Transactional
    public OnboardResponse onboardPolicyholder(OnboardRequest request) {
        // Validate email uniqueness
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email address is already in use");
        }

        // Validate username uniqueness
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username is already taken");
        }

        // Create the user with pending AML status
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.POLICYHOLDER)
                .legalName(request.getLegalName())
                .taxId(request.getTaxId())
                .address(request.getAddress())
                .contactNumber(request.getContactNumber())
                .isActive(true)
                .amlFlagged(false)
                .amlStatus("PENDING")
                .build();

        User savedUser = userRepository.save(user);

        // Log onboarding event
        auditLogService.log("User Onboarded", savedUser.getEmail(), 
                "Role: Policyholder, legal name: " + savedUser.getLegalName());

        // Trigger AML check asynchronously in the background (FR5)
        amlService.verifyAmlCheckAsync(savedUser.getId());

        return OnboardResponse.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .role(savedUser.getRole().name())
                .amlStatus(savedUser.getAmlStatus())
                .kycStatus("VERIFIED")
                .message("Policyholder onboarding initialized; AML check running in background")
                .build();
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
    }

    @Override
    @Transactional
    public User createUser(UserCreateRequest request) {
        // Validate email uniqueness
        String finalEmail = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(finalEmail)) {
            throw new EmailAlreadyExistsException("Email address is already in use");
        }

        // Validate username uniqueness
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username is already taken");
        }

        // Map role
        Role role = Role.valueOf(request.getRole().toUpperCase());

        User user = User.builder()
                .username(request.getUsername())
                .email(finalEmail)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .officeId(request.getOfficeId() != null ? request.getOfficeId() : "Singapore")
                .certificationNumber(request.getCertificationNumber())
                .legalName(request.getLegalName() != null ? request.getLegalName() : request.getUsername())
                .isActive(true)
                .amlFlagged(false)
                .amlStatus("CLEARED")
                .build();

        User saved = userRepository.save(user);

        // Record audit log
        auditLogService.log("User Created", saved.getEmail(), "Role: " + saved.getRole() + ", office: " + saved.getOfficeId());

        return saved;
    }
}
