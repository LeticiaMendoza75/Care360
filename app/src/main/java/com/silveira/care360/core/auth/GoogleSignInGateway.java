package com.silveira.care360.core.auth;

import android.content.Intent;

import com.silveira.care360.domain.common.ResultCallback;

public interface GoogleSignInGateway {
    Intent getGoogleSignInIntent();
    void resolveSignInResult(Intent data, ResultCallback<GoogleAuthResult> callback);
}
