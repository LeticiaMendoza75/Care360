package com.silveira.care360.data.remote.auth;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.auth.UserProfileChangeRequest;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FirebaseAuthDataSource {

    private final FirebaseAuth firebaseAuth;

    @Inject
    public FirebaseAuthDataSource(FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;
    }

    public FirebaseUser getFirebaseCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    public boolean isUserLoggedIn() {
        return firebaseAuth.getCurrentUser() != null;
    }

    public void signOut() {
        firebaseAuth.signOut();
    }

    public Task<AuthResult> signInWithGoogleToken(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        return firebaseAuth.signInWithCredential(credential);
    }

    public Task<SignInMethodQueryResult> fetchSignInMethodsForEmail(String email) {
        return firebaseAuth.fetchSignInMethodsForEmail(email);
    }

    public Task<AuthResult> signInWithEmailAndPassword(String email, String password) {
        return firebaseAuth.signInWithEmailAndPassword(email, password);
    }

    public Task<AuthResult> createUserWithEmailAndPassword(String email, String password) {
        return firebaseAuth.createUserWithEmailAndPassword(email, password);
    }

    public Task<Void> sendPasswordResetEmail(String email) {
        return firebaseAuth.sendPasswordResetEmail(email);
    }

    public Task<Void> updateCurrentUserDisplayName(String displayName) {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null) {
            return Tasks.forException(new IllegalStateException("No user logged in"));
        }

        UserProfileChangeRequest req = new UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build();

        return firebaseUser.updateProfile(req);
    }
}
