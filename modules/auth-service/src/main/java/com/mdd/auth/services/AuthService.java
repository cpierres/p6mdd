package com.mdd.auth.services;

import com.mdd.auth.entities.User;
import com.mdd.auth.models.FieldInfoDetails;
import com.mdd.auth.models.RegisterRequest;
import com.mdd.auth.models.Severity;
import com.mdd.auth.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Mono<User> register(RegisterRequest req) {
        Mono<Boolean> emailExists = userRepository.existsByEmail(req.getEmail());
        Mono<Boolean> usernameExists = userRepository.existsByUsername(req.getUsername());

        return Mono.zip(emailExists, usernameExists)
                .flatMap(tuple -> {
                    boolean e = tuple.getT1();
                    boolean u = tuple.getT2();
                    if (e || u) {
                        List<FieldInfoDetails> fields = new ArrayList<>();
                        if (e) {
                            fields.add(new FieldInfoDetails("email", "Un utilisateur avec cet email existe déjà.", Severity.WARNING));
                        }
                        if (u) {
                            fields.add(new FieldInfoDetails("username", "Un utilisateur avec ce nom existe déjà.", Severity.WARNING));
                        }
                        return Mono.error(new MultipleConflictException(fields));
                    }

                    Instant now = Instant.now();
                    // IMPORTANT (Spring Data R2DBC): si l'Id est non-null, `save()` tente un UPDATE.
                    // Pour une création, on laisse l'Id à null afin de forcer un INSERT (id généré par la DB).
                    User user = User.newUser(
                            req.getUsername(),
                            req.getEmail(),
                            passwordEncoder.encode(req.getPassword()),
                            now,
                            now
                    );
                    return userRepository.save(user);
                });
    }

    public Mono<User> authenticate(String identifier, String rawPassword) {
        Mono<User> byEmail = userRepository.findByEmail(identifier);
        Mono<User> byUsername = userRepository.findByUsername(identifier);

        return byEmail.switchIfEmpty(byUsername)
                .filter(user -> passwordEncoder.matches(rawPassword, user.getPasswordHash()));
    }

    public static class MultipleConflictException extends RuntimeException {
        private final List<FieldInfoDetails> fields;

        public MultipleConflictException(List<FieldInfoDetails> fields) {
            super("Conflits multiples");
            this.fields = fields;
        }

        public List<FieldInfoDetails> getFields() {
            return fields;
        }
    }
}
