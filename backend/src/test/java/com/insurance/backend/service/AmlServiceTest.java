package com.insurance.backend.service;

import com.insurance.backend.entity.User;
import com.insurance.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AmlServiceTest {

    private AmlService amlService;

    @Mock
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        amlService = new AmlService(userRepository);
    }

    @Test
    void testAmlCheck_Success() {
        boolean result = amlService.performAmlCheck("John Meridian", "TAX-99882");
        assertTrue(result, "Standard applicant details should pass the AML checks");
    }

    @Test
    void testAmlCheck_BlacklistedName_Fails() {
        boolean result = amlService.performAmlCheck("Blacklisted User", "TAX-99882");
        assertFalse(result, "Blacklisted name must be flagged and rejected");
    }

    @Test
    void testAmlCheck_BlacklistedTaxId_Fails() {
        boolean result = amlService.performAmlCheck("Jane Doe", "TAX-ILLEGAL-999");
        assertFalse(result, "Blacklisted tax identity parameter must be flagged and rejected");
    }

    @Test
    void testVerifyAmlCheckAsync_BlacklistedMatch_LocksAccount() {
        Long userId = 1L;
        User mockUser = User.builder()
                .id(userId)
                .legalName("BLACKLISTED USER")
                .taxId("TAX-000")
                .isActive(true)
                .amlFlagged(false)
                .amlStatus("PENDING")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        amlService.verifyAmlCheckAsync(userId);

        assertFalse(mockUser.isActive(), "Blacklisted user must be locked (isActive = false)");
        assertTrue(mockUser.isAmlFlagged(), "Blacklisted user must be flagged for review");
        assertEquals("FLAGGED", mockUser.getAmlStatus());
        verify(userRepository, times(1)).save(mockUser);
    }
}
