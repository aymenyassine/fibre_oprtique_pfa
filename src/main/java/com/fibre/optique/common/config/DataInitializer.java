package com.fibre.optique.common.config;

import com.fibre.optique.users.entity.Role;
import com.fibre.optique.users.entity.User;
import com.fibre.optique.users.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Bootstraps the first ADMIN account at application startup.
 *
 * <p>If no ADMIN user exists in the database, one is created automatically
 * using credentials from environment variables (or application.yml defaults).</p>
 *
 * <p>Configure via:</p>
 * <pre>
 *   app.init.admin.email=admin@fibre.local
 *   app.init.admin.password=AdminSecure1234!
 *   app.init.admin.nom=Admin
 *   app.init.admin.prenom=Système
 * </pre>
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.init.admin.email:admin@fibre-optique.local}")
    private String adminEmail;

    @Value("${app.init.admin.password:AdminSecure1234!}")
    private String adminPassword;

    @Value("${app.init.admin.nom:Admin}")
    private String adminNom;

    @Value("${app.init.admin.prenom:Système}")
    private String adminPrenom;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        boolean adminExists = userRepository.findAll()
                .stream()
                .anyMatch(u -> u.getRole() == Role.ADMIN);

        if (adminExists) {
            log.info("DataInitializer: admin account already exists — skipping.");
            return;
        }

        User admin = User.builder()
                .nom(adminNom)
                .prenom(adminPrenom)
                .email(adminEmail)
                .passwordHash(passwordEncoder.encode(adminPassword))
                .role(Role.ADMIN)
                .enabled(true)
                .build();

        userRepository.save(admin);
        log.warn("DataInitializer: created default ADMIN account [{}]. " +
                 "Change the password immediately in production!", adminEmail);
    }
}
