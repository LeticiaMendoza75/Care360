package com.silveira.care360.domain.usecase;

import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.GroupMember;
import com.silveira.care360.domain.model.User;
import com.silveira.care360.domain.repository.UserRepository;

import java.util.List;

import javax.inject.Inject;

public class StartEmailLoginFlowUseCase {

    private final UserRepository userRepository;
    private final LoginWithEmailUseCase loginWithEmailUseCase;

    @Inject
    public StartEmailLoginFlowUseCase(UserRepository userRepository,
                                      LoginWithEmailUseCase loginWithEmailUseCase) {
        this.userRepository = userRepository;
        this.loginWithEmailUseCase = loginWithEmailUseCase;
    }

    public void execute(String email, String password, ResultCallback<Result> callback) {
        userRepository.searchUserByEmail(email, new ResultCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> users) {
                if (users == null || users.isEmpty()) {
                    callback.onSuccess(Result.noAccount(email));
                    return;
                }

                User user = users.get(0);
                String authProvider = user.getAuthProvider();

                if ("google".equalsIgnoreCase(authProvider)) {
                    callback.onSuccess(Result.googleAccount());
                    return;
                }

                if ("password".equalsIgnoreCase(authProvider)) {
                    performEmailLogin(email, password, callback);
                    return;
                }

                callback.onSuccess(Result.error("Metodo de acceso no soportado todavia."));
            }

            @Override
            public void onError(String message) {
                callback.onSuccess(Result.error("No se pudo comprobar el email"));
            }
        });
    }

    private void performEmailLogin(String email, String password, ResultCallback<Result> callback) {
        loginWithEmailUseCase.execute(email, password, new ResultCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user == null) {
                    callback.onSuccess(Result.error("Error de sesion"));
                    return;
                }

                routeAfterLogin(user.getId(), callback);
            }

            @Override
            public void onError(String message) {
                callback.onSuccess(Result.error("Email o contrasena incorrectos"));
            }
        });
    }

    private void routeAfterLogin(String uid, ResultCallback<Result> callback) {
        userRepository.getUserMemberships(uid, new ResultCallback<List<GroupMember>>() {
            @Override
            public void onSuccess(List<GroupMember> memberships) {
                if (memberships == null || memberships.isEmpty()) {
                    callback.onSuccess(Result.navigation(Destination.GROUP_ENTRY));
                } else {
                    callback.onSuccess(Result.navigation(Destination.HOME));
                }
            }

            @Override
            public void onError(String message) {
                callback.onSuccess(Result.navigation(Destination.GROUP_ENTRY));
            }
        });
    }

    public enum Destination {
        HOME,
        GROUP_ENTRY
    }

    public static class Result {
        public enum Type {
            GOOGLE_ACCOUNT,
            NO_ACCOUNT,
            NAVIGATION,
            ERROR
        }

        private final Type type;
        private final Destination destination;
        private final String errorMessage;
        private final String extraData;

        private Result(Type type, Destination destination, String errorMessage, String extraData) {
            this.type = type;
            this.destination = destination;
            this.errorMessage = errorMessage;
            this.extraData = extraData;
        }

        public static Result googleAccount() {
            return new Result(Type.GOOGLE_ACCOUNT, null, null, null);
        }

        public static Result noAccount(String email) {
            return new Result(Type.NO_ACCOUNT, null, null, email);
        }

        public static Result navigation(Destination destination) {
            return new Result(Type.NAVIGATION, destination, null, null);
        }

        public static Result error(String errorMessage) {
            return new Result(Type.ERROR, null, errorMessage, null);
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

        public String getExtraData() {
            return extraData;
        }
    }
}
