package com.silveira.care360.domain.usecase;

import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.User;
import com.silveira.care360.domain.repository.AuthRepository;
import com.silveira.care360.domain.repository.UserRepository;

import javax.inject.Inject;

public class RegisterUserUseCase {

    private final AuthRepository authRepository;
    private final UserRepository userRepository;

    @Inject
    public RegisterUserUseCase(AuthRepository authRepository, UserRepository userRepository) {
        this.authRepository = authRepository;
        this.userRepository = userRepository;
    }

    public void execute(String email, String password, ResultCallback<User> callback) {
        authRepository.createUserWithEmailAndPassword(email, password, callback);
    }

    /**
     * Guarda el perfil de usuario en la base de datos usando el modelo de dominio.
     */
    public void saveUserProfile(User domainUser, ResultCallback<Void> callback) {
        userRepository.createMinimalUserProfile(domainUser, "password", callback);
    }
}
