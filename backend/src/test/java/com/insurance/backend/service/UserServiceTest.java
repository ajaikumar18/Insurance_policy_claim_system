package com.insurance.backend.service;

import com.insurance.backend.dto.OnboardRequest;
import com.insurance.backend.dto.OnboardResponse;
import com.insurance.backend.entity.Role;
import com.insurance.backend.entity.User;
import com.insurance.backend.exception.AmlCheckFailedException;
import com.insurance.backend.exception.EmailAlreadyExistsException;
import com.insurance.backend.repository.UserRepository;
import com.insurance.backend.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AmlService amlService;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private AuditLogService auditLogService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserServiceImpl(userRepository, passwordEncoder, amlService, tokenProvider, authenticationManager, auditLogService);
    }

    @Test
    void testOnboardPolicyholder_Success() {
        OnboardRequest request = new OnboardRequest();
        request.setUsername("testpolicyholder");
        request.setEmail("testholder@ipcms.local");
        request.setPassword("password123");
        request.setLegalName("Test Holder");
        request.setTaxId("TAX-99812");
        request.setAddress("456 Main St");
        request.setContactNumber("1234567890");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        doNothing().when(amlService).verifyAmlCheckAsync(anyLong());
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");

        User savedUser = User.builder()
                .id(1L)
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash("encodedPassword")
                .role(Role.POLICYHOLDER)
                .amlStatus("PENDING")
                .build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        OnboardResponse response = userService.onboardPolicyholder(request);

        assertNotNull(response);
        assertEquals("testholder@ipcms.local", response.getEmail());
        assertEquals("PENDING", response.getAmlStatus());
        assertEquals("VERIFIED", response.getKycStatus());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testOnboardPolicyholder_EmailExists_ThrowsException() {
        OnboardRequest request = new OnboardRequest();
        request.setEmail("duplicate@ipcms.local");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> userService.onboardPolicyholder(request));
        verify(userRepository, never()).save(any(User.class));
    }
}
