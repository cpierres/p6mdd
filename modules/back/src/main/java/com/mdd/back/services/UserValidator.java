package com.mdd.back.services;

import com.mdd.back.exception.MultipleResourceAlreadyExistException;
import com.mdd.back.models.FieldErrorDetail;
import com.mdd.back.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Classe UserValidator permettant de valider des propriétés des utilisateurs, comme l'unicité
 * des emails et noms d'utilisateur, lors de leur création ou mise à jour.
 * Cela permet de ne pas alourdir AuthService et permettra de partager la logique de validation avec
 * un autre futur service tel qu'un UserService par exemple.
 */
@Component
public class UserValidator {
    private final UserRepository userRepository;

    @Autowired
    public UserValidator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Valide l'unicité de l'email et du nom d'utilisateur lors de la création ou la mise à jour d'un utilisateur.
     * En cas de création, l'id doit être null.
     * En cas de mise à jour, l'id doit être indiqué et cette méthode vérifie si un autre utilisateur
     * que celui avec l'ID spécifié possède le même email ou nom d'utilisateur.
     * Une exception MultipleResourceAlreadyExistException est générée  en cas de conflit.
     *
     * @param id       null si création ou en cas de mise à jour l'identifiant l'utilisateur à exclure de la validation
     *                 pour le contrôle d'unicité
     * @param email    l'email à valider
     * @param username le nom d'utilisateur à valider
     * @return un Mono<Void> qui complète si aucune erreur de validation n'est trouvée,
     * ou émet une exception MultipleResourceAlreadyExistException en cas de conflit.
     */
    public Mono<Void> validateUniqueEmailAndUsername(UUID id, String email, String username) {
        List<FieldErrorDetail> errors = new ArrayList<>();

        // Vérifie si un autre utilisateur existe avec le même email
        Mono<Boolean> emailExists = userRepository.findByEmail(email)
                .filter(user -> !user.getId().equals(id)) // Exclure l'utilisateur courant si l'ID n'est pas null
                .hasElement();

        // Vérifie si un autre utilisateur existe avec le même username
        Mono<Boolean> usernameExists = userRepository.findByUsername(username)
                .filter(user -> !user.getId().equals(id)) // Exclure l'utilisateur courant si l'ID n'est pas null
                .hasElement();

        // Combine les résultats et gère les doublons
        return Mono.zip(emailExists, usernameExists)
                .flatMap(results -> {
                    boolean emailConflict = results.getT1();
                    boolean usernameConflict = results.getT2();

                    if (emailConflict) {
                        errors.add(new FieldErrorDetail("email", "Un utilisateur avec cet email existe déjà."));
                    }
                    if (usernameConflict) {
                        errors.add(new FieldErrorDetail("username", "Un utilisateur avec ce nom existe déjà."));
                    }

                    if (!errors.isEmpty()) {
                        return Mono.error(new MultipleResourceAlreadyExistException(errors));
                    }
                    return Mono.empty();
                });
    }
}