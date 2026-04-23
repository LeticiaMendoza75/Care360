package com.silveira.care360.domain.usecase;

import android.content.Intent;

import com.silveira.care360.core.auth.GoogleSignInGateway;

import javax.inject.Inject;

public class GetGoogleSignInIntentUseCase {

    private final GoogleSignInGateway googleSignInGateway;

    @Inject
    public GetGoogleSignInIntentUseCase(GoogleSignInGateway googleSignInGateway) {
        this.googleSignInGateway = googleSignInGateway;
    }

    public Intent execute() {
        return googleSignInGateway.getGoogleSignInIntent();
    }
}
