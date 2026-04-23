package com.silveira.care360.domain.repository;

import com.silveira.care360.domain.auth.EmailAccessMethod;
import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.User;

public interface AuthRepository {
    User getCurrentUser();
    boolean isUserLoggedIn();
    void signOutFromAllProviders(Runnable onComplete);
    void checkEmailAccessMethod(String email, ResultCallback<EmailAccessMethod> callback);
    void signInWithGoogleToken(String idToken, ResultCallback<User> callback);
    void signInWithEmailAndPassword(String email, String password, ResultCallback<User> callback);
    void createUserWithEmailAndPassword(String email, String password, ResultCallback<User> callback);
    void sendPasswordResetEmail(String email, ResultCallback<Void> callback);

    /**
     * Actualiza el nombre visible del usuario autenticado actual.
     */
    void updateCurrentUserDisplayName(String displayName, ResultCallback<Void> callback);
}
