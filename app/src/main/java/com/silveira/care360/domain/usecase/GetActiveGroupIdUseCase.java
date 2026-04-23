package com.silveira.care360.domain.usecase;

import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.repository.UserRepository;

import javax.inject.Inject;

public class GetActiveGroupIdUseCase {

    private final UserRepository userRepository;

    @Inject
    public GetActiveGroupIdUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void execute(String userId, ResultCallback<String> callback) {
        userRepository.getActiveGroupId(userId, callback);
    }
}
