package com.silveira.care360.domain.usecase;

import android.content.Intent;

import com.silveira.care360.core.auth.GoogleAuthResult;
import com.silveira.care360.core.auth.GoogleSignInGateway;
import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.GroupMember;
import com.silveira.care360.domain.model.User;
import com.silveira.care360.domain.repository.AuthRepository;
import com.silveira.care360.domain.repository.UserRepository;

import java.util.List;

import javax.inject.Inject;

public class HandleGoogleSignInUseCase {

    private final GoogleSignInGateway googleSignInGateway;
    private final AuthRepository authRepository;
    private final UserRepository userRepository;

    @Inject
    public HandleGoogleSignInUseCase(GoogleSignInGateway googleSignInGateway,
                                     AuthRepository authRepository,
                                     UserRepository userRepository) {
        this.googleSignInGateway = googleSignInGateway;
        this.authRepository = authRepository;
        this.userRepository = userRepository;
    }

    public void execute(Intent data, ResultCallback<Result> callback) {
        if (data == null) {
            callback.onSuccess(Result.cancelled());
            return;
        }

        googleSignInGateway.resolveSignInResult(data, new ResultCallback<GoogleAuthResult>() {
            @Override
            public void onSuccess(GoogleAuthResult result) {
                if (result == null) {
                    callback.onSuccess(Result.googleAccountError());
                    return;
                }

                signInWithGoogle(result.getIdToken(), callback);
            }

            @Override
            public void onError(String message) {
                callback.onSuccess(Result.googleSignInError(message));
            }
        });
    }

    private void signInWithGoogle(String idToken, ResultCallback<Result> callback) {
        if (idToken == null) {
            callback.onSuccess(Result.missingIdToken());
            return;
        }

        callback.onSuccess(Result.loading());

        authRepository.signInWithGoogleToken(idToken, new ResultCallback<User>() {
            @Override
            public void onSuccess(User result) {
                if (result == null) {
                    callback.onSuccess(Result.userNull());
                    return;
                }

                routeAfterLogin(result, callback);
            }

            @Override
            public void onError(String message) {
                callback.onSuccess(Result.firebaseAuthError(message));
            }
        });
    }

    private void routeAfterLogin(User authenticatedUser, ResultCallback<Result> callback) {
        userRepository.getUserProfile(authenticatedUser.getId(), new ResultCallback<User>() {
            @Override
            public void onSuccess(User profile) {
                if (profile != null) {
                    userRepository.getUserMemberships(authenticatedUser.getId(), new ResultCallback<List<GroupMember>>() {
                        @Override
                        public void onSuccess(List<GroupMember> memberships) {
                            if (memberships == null || memberships.isEmpty()) {
                                callback.onSuccess(Result.navigateTo(Destination.GROUP_ENTRY));
                            } else {
                                callback.onSuccess(Result.navigateTo(Destination.HOME));
                            }
                        }

                        @Override
                        public void onError(String message) {
                            callback.onSuccess(Result.navigateTo(Destination.GROUP_ENTRY));
                        }
                    });
                    return;
                }

                createGoogleUserProfile(authenticatedUser, callback);
            }

            @Override
            public void onError(String message) {
                createGoogleUserProfile(authenticatedUser, callback);
            }
        });
    }

    private void createGoogleUserProfile(User authenticatedUser, ResultCallback<Result> callback) {
        User newUser = new User();
        newUser.setId(authenticatedUser.getId());
        newUser.setEmail(authenticatedUser.getEmail());
        newUser.setDisplayName(authenticatedUser.getDisplayName());
        newUser.setPhotoUrl(authenticatedUser.getPhotoUrl());
        newUser.setActive(true);
        newUser.setAuthProvider("google");
        newUser.setCreatedAt(System.currentTimeMillis());
        newUser.setUpdatedAt(System.currentTimeMillis());

        userRepository.createMinimalUserProfile(newUser, "google", new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                callback.onSuccess(Result.navigateTo(Destination.GROUP_ENTRY));
            }

            @Override
            public void onError(String message) {
                callback.onSuccess(Result.navigateTo(Destination.GROUP_ENTRY));
            }
        });
    }

    public enum Destination {
        HOME,
        GROUP_ENTRY
    }

    public static class Result {
        public enum Type {
            LOADING,
            NAVIGATION,
            CANCELLED,
            GOOGLE_ACCOUNT_ERROR,
            GOOGLE_SIGN_IN_ERROR,
            MISSING_ID_TOKEN,
            USER_NULL,
            FIREBASE_AUTH_ERROR
        }

        private final Type type;
        private final Destination destination;
        private final String debugMessage;

        private Result(Type type, Destination destination, String debugMessage) {
            this.type = type;
            this.destination = destination;
            this.debugMessage = debugMessage;
        }

        public static Result navigateTo(Destination destination) {
            return new Result(Type.NAVIGATION, destination, null);
        }

        public static Result loading() {
            return new Result(Type.LOADING, null, null);
        }

        public static Result cancelled() {
            return new Result(Type.CANCELLED, null, null);
        }

        public static Result googleAccountError() {
            return new Result(Type.GOOGLE_ACCOUNT_ERROR, null, null);
        }

        public static Result googleSignInError(String debugMessage) {
            return new Result(Type.GOOGLE_SIGN_IN_ERROR, null, debugMessage);
        }

        public static Result missingIdToken() {
            return new Result(Type.MISSING_ID_TOKEN, null, null);
        }

        public static Result userNull() {
            return new Result(Type.USER_NULL, null, null);
        }

        public static Result firebaseAuthError(String debugMessage) {
            return new Result(Type.FIREBASE_AUTH_ERROR, null, debugMessage);
        }

        public Type getType() {
            return type;
        }

        public Destination getDestination() {
            return destination;
        }

        public String getDebugMessage() {
            return debugMessage;
        }
    }
}
