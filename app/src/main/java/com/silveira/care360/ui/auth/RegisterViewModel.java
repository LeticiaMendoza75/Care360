package com.silveira.care360.ui.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.usecase.RegisterWithEmailFlowUseCase;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class RegisterViewModel extends ViewModel {

    private final RegisterWithEmailFlowUseCase registerWithEmailFlowUseCase;

    private final MutableLiveData<RegisterState> _state = new MutableLiveData<>(new RegisterState());
    public LiveData<RegisterState> state = _state;

    private final MutableLiveData<RegisterNavigation> _navigation = new MutableLiveData<>();
    public LiveData<RegisterNavigation> navigation = _navigation;

    @Inject
    public RegisterViewModel(RegisterWithEmailFlowUseCase registerWithEmailFlowUseCase) {
        this.registerWithEmailFlowUseCase = registerWithEmailFlowUseCase;
    }

    public void register(String name, String email, String password) {
        _state.setValue(new RegisterState(true));

        registerWithEmailFlowUseCase.execute(name, email, password, new ResultCallback<RegisterWithEmailFlowUseCase.Result>() {
            @Override
            public void onSuccess(RegisterWithEmailFlowUseCase.Result result) {
                handleResult(result);
            }

            @Override
            public void onError(String message) {
                _state.postValue(new RegisterState(message != null ? message : "Error desconocido"));
            }
        });
    }

    private void handleResult(RegisterWithEmailFlowUseCase.Result result) {
        if (result == null) {
            _state.postValue(new RegisterState("Error desconocido"));
            return;
        }

        switch (result.getType()) {
            case NAVIGATION:
                _navigation.postValue(RegisterNavigation.GROUP_ENTRY);
                return;
            case ERROR:
                _state.postValue(new RegisterState(result.getErrorMessage()));
                return;
            default:
                _state.postValue(new RegisterState("Error desconocido"));
        }
    }

    public static class RegisterState {
        public final boolean isLoading;
        public final String errorMessage;
        public final boolean isSuccess;

        public RegisterState() { this(false, null, false); }
        public RegisterState(boolean isLoading) { this(isLoading, null, false); }
        public RegisterState(String error) { this(false, error, false); }
        public RegisterState(boolean isLoading, String error, boolean success) {
            this.isLoading = isLoading;
            this.errorMessage = error;
            this.isSuccess = success;
        }
    }

    public enum RegisterNavigation { GROUP_ENTRY, HOME_OR_GROUP }
}
