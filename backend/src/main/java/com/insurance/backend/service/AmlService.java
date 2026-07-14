package com.insurance.backend.service;

import com.insurance.backend.entity.User;
import com.insurance.backend.repository.UserRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
public class AmlService {

    private final UserRepository userRepository;

    // Simple mock blacklist for names and tax IDs
    private static final List<String> BLACKLISTED_NAMES = Arrays.asList(
            "BLACKLISTED USER", "SANCTIONED INDIVIDUAL", "TERRORIST ORGANIZATION", "BAD ACTOR"
    );

    private static final List<String> BLACKLISTED_TAX_IDS = Arrays.asList(
            "TAX-ILLEGAL-999", "OFAC-BAD-777", "999-99-9999"
    );

    public AmlService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean performAmlCheck(String legalName, String taxId) {
        try {
            // Simulate processing latency for verification check (must be < 1.8 seconds)
            Thread.sleep(150);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (legalName == null || taxId == null) {
            return false;
        }

        String normalizedName = legalName.toUpperCase().trim();
        String normalizedTaxId = taxId.toUpperCase().trim();

        // Check if name or taxId matches mock blacklists
        boolean nameMatch = BLACKLISTED_NAMES.stream().anyMatch(normalizedName::contains);
        boolean taxMatch = BLACKLISTED_TAX_IDS.stream().anyMatch(normalizedTaxId::contains);

        return !nameMatch && !taxMatch;
    }

    @Async
    @Transactional
    public void verifyAmlCheckAsync(Long userId) {
        // Retrieve candidate user
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return;
        }

        // Simulate async latency (completes under the 1.8-second SRS limit)
        try {
            Thread.sleep(800);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        boolean passed = performAmlCheck(user.getLegalName(), user.getTaxId());

        if (!passed) {
            // blacklist match: lock account and flag for review (FR5)
            user.setActive(false);
            user.setAmlFlagged(true);
            user.setAmlStatus("FLAGGED");
        } else {
            user.setAmlStatus("CLEARED");
        }

        userRepository.save(user);
    }
}
