package com.silveira.care360.ui.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.usecase.StartEmailLoginFlowUseCase;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class LoginEmailViewModel extends ViewModel {

    private final StartEmailLoginFlowUseCase startEmailLoginFlowUseCase;

    private final MutableLiveData<LoginState> _state = new MutableLiveData<>(new LoginState());
    public LiveData<LoginState> state = _state;

    private final MutableLiveData<LoginNavigation> _navigation = new MutableLiveData<>();
    public LiveData<LoginNavigation> navigation = _navigation;

    @Inject
    public LoginEmailViewModel(StartEmailLoginFlowUseCase startEmailLoginFlowUseCase) {
        this.startEmailLoginFlowUseCase = startEmailLoginFlowUseCase;
    }

    public void startLoginFlow(String email, String pass) {
        _state.setValue(new LoginState(true));

        startEmailLoginFlowUseCase.execute(email, pass, new ResultCallback<StartEmailLoginFlowUseCase.Result>() {
            @Override
            public void onSuccess(StartEmailLoginFlowUseCase.Result result) {
                handleResult(result);
            }

            @Override
            public void onError(String message) {
                _state.postValue(new LoginState("No se pudo comprobar el email"));
            }
        });
    }

    private void handleResult(StartEmailLoginFlowUseCase.Result result) {
        if (result == null) {
            _state.postValue(new LoginState("No se pudo comprobar el email"));
            return;
        }

        switch (result.getType()) {
            case GOOGLE_ACCOUNT:
                _state.postValue(new LoginState(LoginErrorType.GOOGLE_ACCOUNT));
                return;
            case NO_ACCOUNT:
                _state.postValue(new LoginState(LoginErrorType.NO_ACCOUNT, result.getExtraData()));
                return;
            case NAVIGATION:
                if (result.getDestination() == StartEmailLoginFlowUseCase.Destination.HOME) {
                    _navigation.postValue(LoginNavigation.HOME);
                } else {
                    _navigation.postValue(LoginNavigation.GROUP_ENTRY);
                }
                return;
            case ERROR:
                _state.postValue(new LoginState(result.getErrorMessage()));
                return;
            default:
                _state.postValue(new LoginState("No se pudo comprobar el email"));
        }
    }

    public static class LoginState {
        public final boolean isLoading;
        public final String errorMessage;
        public final LoginErrorType errorType;
        public final String extraData;

        public LoginState() { this(false, null, null, null); }
        public LoginState(boolean isLoading) { this(isLoading, null, null, null); }
        public LoginState(String errorMessage) { this(false, errorMessage, null, null); }
        public LoginState(LoginErrorType type) { this(false, null, type, null); }
        public LoginState(LoginErrorType type, String data) { this(false, null, type, data); }

        private LoginState(boolean isLoading, String errorMessage, LoginErrorType errorType, String extraData) {
            this.isLoading = isLoading;
            this.errorMessage = errorMessage;
            this.errorType = errorType;
            this.extraData = extraData;
        }
    }

    public enum LoginErrorType { GOOGLE_ACCOUNT, NO_ACCOUNT }
    public enum LoginNavigation { HOME, GROUP_ENTRY }
}
