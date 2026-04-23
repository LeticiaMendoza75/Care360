package com.silveira.care360.domain.usecase;

import com.silveira.care360.domain.model.User;
import com.silveira.care360.domain.repository.AuthRepository;

import javax.inject.Inject;

public class GetCurrentUserUseCase {

    private final AuthRepository authRepository;

    @Inject
    public GetCurrentUserUseCase(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public User execute() {
        return authRepository.getCurrentUser();
    }
}
