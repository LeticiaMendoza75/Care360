package com.silveira.care360.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.usecase.ResetPasswordFlowUseCase;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ResetPasswordViewModel extends ViewModel {
    private final ResetPasswordFlowUseCase resetPasswordFlowUseCase;

    private final MutableLiveData<ResetPasswordState> _state =
            new MutableLiveData<>(new ResetPasswordState());
    public LiveData<ResetPasswordState> state = _state;

    private final MutableLiveData<ResetPasswordAction> _action = new MutableLiveData<>();
    public LiveData<ResetPasswordAction> action = _action;

    @Inject
    public ResetPasswordViewModel(ResetPasswordFlowUseCase resetPasswordFlowUseCase) {
        this.resetPasswordFlowUseCase = resetPasswordFlowUseCase;
    }

    public void startResetFlow(String email) {
        _state.setValue(new ResetPasswordState(true));

        resetPasswordFlowUseCase.execute(email, new ResultCallback<ResetPasswordFlowUseCase.Result>() {
            @Override
            public void onSuccess(ResetPasswordFlowUseCase.Result result) {
                handleResult(result);
            }

            @Override
            public void onError(String message) {
                emitMessage(message != null ? message : "No se pudo comprobar el email");
            }
        });
    }

    private void handleResult(ResetPasswordFlowUseCase.Result result) {
        if (result == null) {
            emitMessage("No se pudo comprobar el email");
            return;
        }

        switch (result.getType()) {
            case NO_ACCOUNT:
                _state.postValue(new ResetPasswordState(false));
                _action.postValue(new ShowNoAccountDialogAction());
                return;
            case GOOGLE_ONLY:
                _state.postValue(new ResetPasswordState(false));
                _action.postValue(new ShowGoogleOnlyDialogAction());
                return;
            case EMAIL_SENT:
                _state.postValue(new ResetPasswordState(false));
                _action.postValue(new ShowEmailSentDialogAction());
                return;
            case ERROR:
                emitMessage(result.getMessage());
                return;
            default:
                emitMessage("No se pudo comprobar el email");
        }
    }

    public void onActionHandled() {
        _action.setValue(null);
    }

    private void emitMessage(String message) {
        _state.postValue(new ResetPasswordState(false));
        _action.postValue(new ShowMessageAction(message));
    }

    public static class ResetPasswordState {
        public final boolean isLoading;

        public ResetPasswordState() {
            this(false);
        }

        public ResetPasswordState(boolean isLoading) {
            this.isLoading = isLoading;
        }
    }

    public static abstract class ResetPasswordAction { }

    public static class ShowMessageAction extends ResetPasswordAction {
        public final String message;

        public ShowMessageAction(String message) {
            this.message = message;
        }
    }

    public static class ShowNoAccountDialogAction extends ResetPasswordAction { }

    public static class ShowGoogleOnlyDialogAction extends ResetPasswordAction { }

    public static class ShowEmailSentDialogAction extends ResetPasswordAction { }
}
