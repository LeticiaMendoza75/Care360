package com.silveira.care360.domain.usecase;

import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.User;
import com.silveira.care360.domain.repository.AuthRepository;

import javax.inject.Inject;

public class LoginWithEmailUseCase {

    private final AuthRepository authRepository;

    @Inject
    public LoginWithEmailUseCase(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public void execute(String email, String password, ResultCallback<User> callback) {
        authRepository.signInWithEmailAndPassword(email, password, callback);
    }
}
