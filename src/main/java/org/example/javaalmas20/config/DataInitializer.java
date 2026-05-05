package org.example.javaalmas20.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.javaalmas20.domain.entity.Role;
import org.example.javaalmas20.domain.entity.RoleName;
import org.example.javaalmas20.domain.entity.User;
import org.example.javaalmas20.repository.RoleRepository;
import org.example.javaalmas20.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Initializes required roles and a default admin user on startup.
 * Only runs in the 'dev' or 'default' profiles.
 */
@Slf4j
@Component
@Profile({"dev", "default"})
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Create roles if they don't exist
        for (RoleName roleName : RoleName.values()) {
            if (!roleRepository.existsByName(roleName)) {
                roleRepository.save(Role.builder()
                        .name(roleName)
                        .description(roleName.name().replace("ROLE_", "") + " role")
                        .build());
                log.info("Created role: {}", roleName);
            }
        }

        // Create default admin if none exists
        if (!userRepository.existsByUsername("admin")) {
            Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN).orElseThrow();
            Role userRole = roleRepository.findByName(RoleName.ROLE_USER).orElseThrow();

            User admin = User.builder()
                    .username("admin")
                    .email("admin@javaalmas20.local")
                    .password(passwordEncoder.encode("Admin@123456"))
                    .firstName("System")
                    .lastName("Administrator")
                    .roles(Set.of(adminRole, userRole))
                    .build();
            userRepository.save(admin);
            log.info("Created default admin user: admin / Admin@123456");
        }
    }
}
