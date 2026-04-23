package com.silveira.care360.domain.usecase;

import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.GroupUserInput;
import com.silveira.care360.domain.model.User;
import com.silveira.care360.domain.repository.GroupRepository;

import javax.inject.Inject;

public class JoinGroupByCodeUseCase {

    private final GroupRepository groupRepository;

    @Inject
    public JoinGroupByCodeUseCase(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    public void execute(String joinCode, User user, ResultCallback<Result> callback) {
        groupRepository.joinGroupByCode(joinCode, toGroupUserInput(user), new ResultCallback<GroupRepository.JoinGroupResult>() {
            @Override
            public void onSuccess(GroupRepository.JoinGroupResult result) {
                callback.onSuccess(toResult(result));
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    private GroupUserInput toGroupUserInput(User user) {
        if (user == null) {
            return null;
        }

        return new GroupUserInput(
                user.getId(),
                user.getEmail(),
                user.getDisplayName()
        );
    }

    private Result toResult(GroupRepository.JoinGroupResult result) {
        if (result == null) {
            return Result.joined("Te has unido al grupo");
        }

        if (result.isJoined()) {
            return Result.joined(result.getMessage());
        }

        return Result.joined(result.getMessage());
    }

    public static class Result {
        public enum Type {
            JOINED
        }

        private final Type type;
        private final String message;

        private Result(Type type, String message) {
            this.type = type;
            this.message = message;
        }

        public static Result joined(String message) {
            return new Result(Type.JOINED, message != null ? message : "Te has unido al grupo");
        }

        public Type getType() {
            return type;
        }

        public String getMessage() {
            return message;
        }

        public boolean isJoined() {
            return type == Type.JOINED;
        }
    }
}
