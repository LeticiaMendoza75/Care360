package com.silveira.care360.domain.usecase;

import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.User;
import com.silveira.care360.domain.repository.AuthRepository;
import com.silveira.care360.domain.repository.UserRepository;

import javax.inject.Inject;

public class RegisterWithEmailFlowUseCase {

    private final RegisterUserUseCase registerUserUseCase;
    private final AuthRepository authRepository;
    private final UserRepository userRepository;

    @Inject
    public RegisterWithEmailFlowUseCase(RegisterUserUseCase registerUserUseCase,
                                        AuthRepository authRepository,
                                        UserRepository userRepository) {
        this.registerUserUseCase = registerUserUseCase;
        this.authRepository = authRepository;
        this.userRepository = userRepository;
    }

    public void execute(String name, String email, String password, ResultCallback<Result> callback) {
        registerUserUseCase.execute(email, password, new ResultCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user == null) {
                    callback.onSuccess(Result.error("Error creando sesion"));
                    return;
                }

                updateProfileAndSave(user, name, callback);
            }

            @Override
            public void onError(String message) {
                callback.onSuccess(Result.error(message != null ? message : "Error desconocido"));
            }
        });
    }

    private void updateProfileAndSave(User domainUser, String name, ResultCallback<Result> callback) {
        authRepository.updateCurrentUserDisplayName(name, new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                domainUser.setDisplayName(name);

                userRepository.createMinimalUserProfile(domainUser, "password", new ResultCallback<Void>() {
                    @Override
                    public void onSuccess(Void ignored) {
                        routeAfterRegister(domainUser.getId(), callback);
                    }

                    @Override
                    public void onError(String message) {
                        callback.onSuccess(Result.error("Error guardando perfil"));
                    }
                });
            }

            @Override
            public void onError(String message) {
                callback.onSuccess(Result.error("Error actualizando el perfil"));
            }
        });
    }

    private void routeAfterRegister(String uid, ResultCallback<Result> callback) {
        userRepository.getUserProfile(uid, new ResultCallback<User>() {
            @Override
            public void onSuccess(User result) {
                callback.onSuccess(Result.navigation(Destination.GROUP_ENTRY));
            }

            @Override
            public void onError(String message) {
                callback.onSuccess(Result.navigation(Destination.GROUP_ENTRY));
            }
        });
    }

    public enum Destination {
        GROUP_ENTRY
    }

    public static class Result {
        public enum Type {
            NAVIGATION,
            ERROR
        }

        private final Type type;
        private final Destination destination;
        private final String errorMessage;

        private Result(Type type, Destination destination, String errorMessage) {
            this.type = type;
            this.destination = destination;
            this.errorMessage = errorMessage;
        }

        public static Result navigation(Destination destination) {
            return new Result(Type.NAVIGATION, destination, null);
        }

        public static Result error(String errorMessage) {
            return new Result(Type.ERROR, null, errorMessage);
        }

        public Type getType() {
            return type;
        }

        public Destination getDestination() {
            return destination;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
