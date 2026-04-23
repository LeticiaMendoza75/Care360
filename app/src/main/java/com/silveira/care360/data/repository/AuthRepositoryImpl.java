package com.silveira.care360.data.repository;

import com.google.firebase.auth.FirebaseUser;
import com.silveira.care360.data.mapper.UserMapper;
import com.silveira.care360.data.remote.auth.FirebaseAuthDataSource;
import com.silveira.care360.data.remote.auth.GoogleSignInDataSource;
import com.silveira.care360.domain.auth.EmailAccessMethod;
import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.User;
import com.silveira.care360.domain.repository.AuthRepository;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AuthRepositoryImpl implements AuthRepository {

    private final FirebaseAuthDataSource authDataSource;
    private final GoogleSignInDataSource googleSignInDataSource;

    @Inject
    public AuthRepositoryImpl(FirebaseAuthDataSource authDataSource,
                              GoogleSignInDataSource googleSignInDataSource) {
        this.authDataSource = authDataSource;
        this.googleSignInDataSource = googleSignInDataSource;
    }

    @Override
    public User getCurrentUser() {
        return mapCurrentUser();
    }

    @Override
    public boolean isUserLoggedIn() {
        return authDataSource.isUserLoggedIn();
    }

    @Override
    public void checkEmailAccessMethod(String email, ResultCallback<EmailAccessMethod> callback) {
        authDataSource.fetchSignInMethodsForEmail(email).addOnCompleteListener(task -> {
            if (!task.isSuccessful() || task.getResult() == null) {
                callback.onSuccess(EmailAccessMethod.ERROR);
                return;
            }

            java.util.List<String> methods = task.getResult().getSignInMethods();
            if (methods == null || methods.isEmpty()) {
                callback.onSuccess(EmailAccessMethod.NO_ACCOUNT);
                return;
            }

            boolean hasPassword = methods.contains("password");
            boolean hasGoogle = methods.contains("google.com");

            if (hasGoogle && !hasPassword) {
                callback.onSuccess(EmailAccessMethod.GOOGLE_ONLY);
            } else if (hasPassword) {
                callback.onSuccess(EmailAccessMethod.PASSWORD);
            } else {
                callback.onSuccess(EmailAccessMethod.UNSUPPORTED);
            }
        });
    }

    @Override
    public void signInWithGoogleToken(String idToken, ResultCallback<User> callback) {
        authDataSource.signInWithGoogleToken(idToken).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                String message = task.getException() != null ? task.getException().getMessage() : "Google sign-in failed";
                callback.onError(message);
                return;
            }

            User user = mapCurrentUser();
            if (user == null) {
                callback.onError("User session not available");
                return;
            }

            callback.onSuccess(user);
        });
    }

    @Override
    public void signInWithEmailAndPassword(String email, String password, ResultCallback<User> callback) {
        authDataSource.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                String message = task.getException() != null ? task.getException().getMessage() : "Email sign-in failed";
                callback.onError(message);
                return;
            }

            User user = mapCurrentUser();
            if (user == null) {
                callback.onError("User session not available");
                return;
            }

            callback.onSuccess(user);
        });
    }

    @Override
    public void createUserWithEmailAndPassword(String email, String password, ResultCallback<User> callback) {
        authDataSource.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                String message = task.getException() != null ? task.getException().getMessage() : "Create user failed";
                callback.onError(message);
                return;
            }

            User user = mapCurrentUser();
            if (user == null) {
                callback.onError("User session not available");
                return;
            }

            callback.onSuccess(user);
        });
    }

    @Override
    public void sendPasswordResetEmail(String email, ResultCallback<Void> callback) {
        authDataSource.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                String message = task.getException() != null ? task.getException().getMessage() : "Reset password failed";
                callback.onError(message);
                return;
            }

            callback.onSuccess(null);
        });
    }

    @Override
    public void updateCurrentUserDisplayName(String displayName, ResultCallback<Void> callback) {
        authDataSource.updateCurrentUserDisplayName(displayName).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                String message = task.getException() != null ? task.getException().getMessage() : "Update display name failed";
                callback.onError(message);
                return;
            }

            callback.onSuccess(null);
        });
    }

    @Override
    public void signOutFromAllProviders(Runnable onComplete) {
        authDataSource.signOut();
        googleSignInDataSource.signOut(onComplete);
    }

    private User mapCurrentUser() {
        FirebaseUser firebaseUser = authDataSource.getFirebaseCurrentUser();
        return UserMapper.fromFirebaseUser(firebaseUser);
    }
}
