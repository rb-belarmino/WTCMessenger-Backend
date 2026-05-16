package br.com.fiap.crm_backend.config;

import br.com.fiap.crm_backend.user.entity.User;
import br.com.fiap.crm_backend.user.enums.UserRole;
import br.com.fiap.crm_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    @Bean
    CommandLineRunner seedUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.count() == 0) {
                User operator = User.builder()
                        .email("operator@wtcmessenger.com")
                        .password(passwordEncoder.encode("operator123"))
                        .role(UserRole.OPERATOR)
                        .fullName("Admin Operator")
                        .active(true)
                        .createdAt(LocalDateTime.now())
                        .build();

                User customer = User.builder()
                        .email("customer@wtcmessenger.com")
                        .password(passwordEncoder.encode("customer123"))
                        .role(UserRole.CUSTOMER)
                        .fullName("Test Customer")
                        .active(true)
                        .createdAt(LocalDateTime.now())
                        .build();

                userRepository.save(operator);
                userRepository.save(customer);

                log.info("=== INITIAL TEST USERS CREATED IN MONGODB ATLAS ===");
                log.info("OPERATOR -> email: operator@wtcmessenger.com | password: operator123");
                log.info("CUSTOMER -> email: customer@wtcmessenger.com | password: customer123");
                log.info("==================================================");
            } else {
                log.info("=== USERS COLLECTION IS NOT EMPTY, SKIPPING SEED ===");
            }
        };
    }
}
