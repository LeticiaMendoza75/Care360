package com.silveira.care360.data.repository;

import android.content.Intent;

import com.silveira.care360.core.auth.GoogleAuthResult;
import com.silveira.care360.core.auth.GoogleSignInGateway;
import com.silveira.care360.data.remote.auth.GoogleSignInDataSource;
import com.silveira.care360.domain.common.ResultCallback;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class GoogleSignInGatewayImpl implements GoogleSignInGateway {
    private final GoogleSignInDataSource googleSignInDataSource;

    @Inject
    public GoogleSignInGatewayImpl(GoogleSignInDataSource googleSignInDataSource) {
        this.googleSignInDataSource = googleSignInDataSource;
    }

    @Override
    public Intent getGoogleSignInIntent() {
        return googleSignInDataSource.getSignInIntent();
    }

    @Override
    public void resolveSignInResult(Intent data, ResultCallback<GoogleAuthResult> callback) {
        googleSignInDataSource.resolveSignInResult(data, callback);
    }
}
