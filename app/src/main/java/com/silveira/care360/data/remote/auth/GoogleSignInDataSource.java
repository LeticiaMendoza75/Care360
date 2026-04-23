package com.silveira.care360.data.remote.auth;

import android.content.Context;
import android.content.Intent;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.silveira.care360.R;
import com.silveira.care360.core.auth.GoogleAuthResult;
import com.silveira.care360.domain.common.ResultCallback;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class GoogleSignInDataSource {

    private final GoogleSignInClient googleClient;

    @Inject
    public GoogleSignInDataSource(@ApplicationContext Context context) {
        // Obtenemos el Web Client ID desde strings.xml (vinculado a google-services.json)
        String webClientId = context.getString(R.string.default_web_client_id);
        
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId) // Este es el punto crítico para Firebase Auth
                .requestEmail()
                .requestProfile()
                .build();

        googleClient = GoogleSignIn.getClient(context, gso);
    }

    public Intent getSignInIntent() {
        // Forzamos el cierre de sesión previo para permitir elegir cuenta siempre (útil en testing)
        googleClient.signOut();
        return googleClient.getSignInIntent();
    }

    public void resolveSignInResult(Intent data, ResultCallback<GoogleAuthResult> callback) {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            if (account == null) {
                callback.onSuccess(null);
                return;
            }

            callback.onSuccess(new GoogleAuthResult(
                    account.getIdToken(),
                    account.getEmail(),
                    account.getDisplayName(),
                    account.getPhotoUrl() != null ? account.getPhotoUrl().toString() : null
            ));
        } catch (ApiException e) {
            callback.onError(String.valueOf(e.getStatusCode()));
        }
    }

    public void signOut(Runnable onComplete) {
        googleClient.signOut().addOnCompleteListener(task -> {
            if (onComplete != null) {
                onComplete.run();
            }
        });
    }
}
