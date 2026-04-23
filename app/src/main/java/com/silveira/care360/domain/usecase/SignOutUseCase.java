package com.silveira.care360.domain.usecase;

import com.silveira.care360.domain.repository.AuthRepository;

import javax.inject.Inject;

public class SignOutUseCase {

    private final AuthRepository authRepository;

    @Inject
    public SignOutUseCase(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public void execute(Runnable onComplete) {
        authRepository.signOutFromAllProviders(onComplete);
    }
}
