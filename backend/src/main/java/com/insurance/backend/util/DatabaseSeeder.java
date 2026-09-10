package com.insurance.backend.util;

import com.insurance.backend.entity.Policy;
import com.insurance.backend.entity.Role;
import com.insurance.backend.entity.User;
import com.insurance.backend.repository.PolicyRepository;
import com.insurance.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PolicyRepository policyRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(UserRepository userRepository, 
                          PolicyRepository policyRepository, 
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.policyRepository = policyRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            System.out.println("Seeding default users...");
            
            String hashedPw = passwordEncoder.encode("password123");

            User admin = User.builder()
                    .username("admin")
                    .email("admin@ipcms.local")
                    .passwordHash(hashedPw)
                    .role(Role.ADMIN)
                    .officeId("Lagos")
                    .certificationNumber("SYS-2024-001")
                    .isActive(true)
                    .legalName("System Admin")
                    .taxId("TAX-001")
                    .address("123 Admin St")
                    .contactNumber("1234567890")
                    .amlFlagged(false)
                    .amlStatus("CLEARED")
                    .build();
            userRepository.save(admin);

            User underwriter = User.builder()
                    .username("underwriter")
                    .email("underwriter@ipcms.local")
                    .passwordHash(hashedPw)
                    .role(Role.UNDERWRITER)
                    .officeId("London")
                    .certificationNumber("UW-2018-003")
                    .isActive(true)
                    .legalName("Peter Hawthorne")
                    .taxId("TAX-002")
                    .address("456 Underwriter Rd")
                    .contactNumber("1234567890")
                    .amlFlagged(false)
                    .amlStatus("CLEARED")
                    .build();
            userRepository.save(underwriter);

            User claimsHandler = User.builder()
                    .username("claims_handler")
                    .email("claims_handler@ipcms.local")
                    .passwordHash(hashedPw)
                    .role(Role.CLAIMS_HANDLER)
                    .officeId("Stockholm")
                    .certificationNumber("CH-2020-018")
                    .isActive(true)
                    .legalName("Stefan Lindqvist")
                    .taxId("TAX-003")
                    .address("789 Handler Ave")
                    .contactNumber("1234567890")
                    .amlFlagged(false)
                    .amlStatus("CLEARED")
                    .build();
            userRepository.save(claimsHandler);

            User agent = User.builder()
                    .username("agent")
                    .email("agent@ipcms.local")
                    .passwordHash(hashedPw)
                    .role(Role.AGENT)
                    .officeId("Dakar")
                    .certificationNumber("AG-2023-029")
                    .isActive(true)
                    .legalName("Amara Diallo")
                    .taxId("TAX-004")
                    .address("101 Agent Blvd")
                    .contactNumber("1234567890")
                    .amlFlagged(false)
                    .amlStatus("CLEARED")
                    .build();
            userRepository.save(agent);

            User policyholder = User.builder()
                    .username("policyholder")
                    .email("policyholder@ipcms.local")
                    .passwordHash(hashedPw)
                    .role(Role.POLICYHOLDER)
                    .officeId("Singapore")
                    .certificationNumber("PH-2024-055")
                    .isActive(true)
                    .legalName("John Meridian")
                    .taxId("TAX-005")
                    .address("202 Policyholder Lane")
                    .contactNumber("1234567890")
                    .amlFlagged(false)
                    .amlStatus("CLEARED")
                    .build();
            userRepository.save(policyholder);

            User holder = User.builder()
                    .username("holder")
                    .email("holder@ipcms.local")
                    .passwordHash(hashedPw)
                    .role(Role.POLICYHOLDER)
                    .officeId("Singapore")
                    .certificationNumber("PH-2024-056")
                    .isActive(true)
                    .legalName("John Meridian")
                    .taxId("TAX-005")
                    .address("202 Policyholder Lane")
                    .contactNumber("1234567890")
                    .amlFlagged(false)
                    .amlStatus("CLEARED")
                    .build();
            userRepository.save(holder);
            
            System.out.println("Default users seeded successfully.");
        }

        if (policyRepository.count() == 0) {
            System.out.println("Seeding default policies...");
            
            Optional<User> holderOpt = userRepository.findByEmail("holder@ipcms.local");
            if (holderOpt.isEmpty()) {
                holderOpt = userRepository.findByEmail("policyholder@ipcms.local");
            }
            if (holderOpt.isPresent()) {
                User holder = holderOpt.get();

                Policy policy1 = Policy.builder()
                        .policyNumber("POL-2024-001")
                        .policyholder(holder)
                        .productType("Commercial Cargo")
                        .basePremium(BigDecimal.valueOf(14500.00))
                        .activeReserve(BigDecimal.ZERO)
                        .expiryDate(LocalDate.now().plusDays(45))
                        .status("Active")
                        .build();
                policyRepository.save(policy1);

                Policy policy2 = Policy.builder()
                        .policyNumber("POL-2024-002")
                        .policyholder(holder)
                        .productType("General Liability")
                        .basePremium(BigDecimal.valueOf(8200.00))
                        .activeReserve(BigDecimal.ZERO)
                        .expiryDate(LocalDate.now().plusDays(90))
                        .status("Active")
                        .build();
                policyRepository.save(policy2);
                
                System.out.println("Default policies seeded successfully.");
            }
        }
    }
}
