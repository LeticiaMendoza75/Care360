package com.silveira.care360.ui.auth;

import android.content.Intent;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.silveira.care360.R;
import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.usecase.GetGoogleSignInIntentUseCase;
import com.silveira.care360.domain.usecase.HandleGoogleSignInUseCase;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class WelcomeViewModel extends ViewModel {

    private static final String TAG = "WelcomeViewModel";

    private final GetGoogleSignInIntentUseCase getGoogleSignInIntentUseCase;
    private final HandleGoogleSignInUseCase handleGoogleSignInUseCase;

    private final MutableLiveData<WelcomeState> _state = new MutableLiveData<>(new WelcomeState());
    public LiveData<WelcomeState> state = _state;

    private final MutableLiveData<WelcomeNavigation> _navigation = new MutableLiveData<>();
    public LiveData<WelcomeNavigation> navigation = _navigation;

    @Inject
    public WelcomeViewModel(GetGoogleSignInIntentUseCase getGoogleSignInIntentUseCase,
                            HandleGoogleSignInUseCase handleGoogleSignInUseCase) {
        this.getGoogleSignInIntentUseCase = getGoogleSignInIntentUseCase;
        this.handleGoogleSignInUseCase = handleGoogleSignInUseCase;
    }

    public Intent getGoogleSignInIntent() {
        return getGoogleSignInIntentUseCase.execute();
    }

    public void onGoogleSignInResult(Intent data) {
        handleGoogleSignInUseCase.execute(data, new ResultCallback<HandleGoogleSignInUseCase.Result>() {
            @Override
            public void onSuccess(HandleGoogleSignInUseCase.Result result) {
                handleGoogleSignInResult(result);
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Unexpected Google sign-in flow error: " + message);
                _state.postValue(new WelcomeState(R.string.login_google_error));
            }
        });
    }

    private void handleGoogleSignInResult(HandleGoogleSignInUseCase.Result result) {
        if (result == null) {
            _state.postValue(new WelcomeState(R.string.login_google_error));
            return;
        }

        switch (result.getType()) {
            case LOADING:
                _state.postValue(new WelcomeState(true));
                return;
            case CANCELLED:
                _state.postValue(new WelcomeState(R.string.login_cancelled));
                return;
            case GOOGLE_ACCOUNT_ERROR:
                _state.postValue(new WelcomeState(R.string.login_google_account_error));
                return;
            case GOOGLE_SIGN_IN_ERROR:
                Log.e(TAG, "Google sign-in error. Status Code: " + result.getDebugMessage());
                _state.postValue(new WelcomeState(R.string.login_google_error));
                return;
            case MISSING_ID_TOKEN:
                _state.postValue(new WelcomeState(R.string.login_missing_web_client_id));
                return;
            case USER_NULL:
                _state.postValue(new WelcomeState(R.string.login_user_null));
                return;
            case FIREBASE_AUTH_ERROR:
                Log.e(TAG, "FirebaseAuth error: " + result.getDebugMessage());
                _state.postValue(new WelcomeState(R.string.login_firebase_auth_error));
                return;
            case NAVIGATION:
                _state.postValue(new WelcomeState(false));
                if (result.getDestination() == HandleGoogleSignInUseCase.Destination.HOME) {
                    _navigation.postValue(WelcomeNavigation.HOME);
                } else {
                    _navigation.postValue(WelcomeNavigation.GROUP_ENTRY);
                }
                return;
            default:
                _state.postValue(new WelcomeState(R.string.login_google_error));
        }
    }

    public void onNavigationDone() {
        _navigation.setValue(null);
    }

    public static class WelcomeState {
        public final boolean isLoading;
        public final Integer errorResId;

        public WelcomeState() { this(false, null); }
        public WelcomeState(boolean isLoading) { this(isLoading, null); }
        public WelcomeState(Integer errorResId) { this(false, errorResId); }
        private WelcomeState(boolean isLoading, Integer errorResId) {
            this.isLoading = isLoading;
            this.errorResId = errorResId;
        }
    }

    public enum WelcomeNavigation { HOME, GROUP_ENTRY }
}
