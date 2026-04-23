package com.silveira.care360.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.User;
import com.silveira.care360.domain.usecase.GetCurrentUserUseCase;
import com.silveira.care360.domain.usecase.JoinGroupByCodeUseCase;
import com.silveira.care360.domain.usecase.SignOutUseCase;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class GroupEntryViewModel extends ViewModel {
    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final SignOutUseCase signOutUseCase;
    private final JoinGroupByCodeUseCase joinGroupByCodeUseCase;

    private final MutableLiveData<GroupEntryState> _state =
            new MutableLiveData<>(new GroupEntryState());
    public LiveData<GroupEntryState> state = _state;

    private final MutableLiveData<GroupEntryAction> _action = new MutableLiveData<>();
    public LiveData<GroupEntryAction> action = _action;

    @Inject
    public GroupEntryViewModel(GetCurrentUserUseCase getCurrentUserUseCase,
                               SignOutUseCase signOutUseCase,
                               JoinGroupByCodeUseCase joinGroupByCodeUseCase) {
        this.getCurrentUserUseCase = getCurrentUserUseCase;
        this.signOutUseCase = signOutUseCase;
        this.joinGroupByCodeUseCase = joinGroupByCodeUseCase;
    }

    public String getLoggedUserEmail() {
        User currentUser = getCurrentUserUseCase.execute();
        if (currentUser != null && currentUser.getEmail() != null) {
            return currentUser.getEmail();
        }
        return "";
    }

    public void performLogout(Runnable onComplete) {
        signOutUseCase.execute(onComplete);
    }

    public void onCreateGroupClicked() {
        _action.setValue(new NavigateToCreateGroupAction());
    }

    public void joinWithCode(String code) {
        User user = getCurrentUserUseCase.execute();
        if (user == null) {
            _action.setValue(new ShowMessageAction("Usuario no logueado"));
            return;
        }

        String normalizedCode = code != null ? code.trim().toUpperCase() : "";
        if (normalizedCode.length() != 8) {
            _action.setValue(new ShowMessageAction("Introduce un codigo valido de 8 caracteres"));
            return;
        }

        _state.setValue(new GroupEntryState(true));

        joinGroupByCodeUseCase.execute(normalizedCode, user, new ResultCallback<JoinGroupByCodeUseCase.Result>() {
            @Override
            public void onSuccess(JoinGroupByCodeUseCase.Result result) {
                _state.postValue(new GroupEntryState(false));
                String message = result != null && result.getMessage() != null
                        ? result.getMessage()
                        : "Te has unido al grupo";
                _action.postValue(new NavigateToHomeAction(message));
            }

            @Override
            public void onError(String message) {
                emitError(message != null ? message : "Error buscando el codigo");
            }
        });
    }

    public void onActionHandled() {
        _action.setValue(null);
    }

    private void emitError(String message) {
        _state.postValue(new GroupEntryState(false));
        _action.postValue(new ShowMessageAction(message));
    }

    public static class GroupEntryState {
        public final boolean isLoading;

        public GroupEntryState() {
            this(false);
        }

        public GroupEntryState(boolean isLoading) {
            this.isLoading = isLoading;
        }
    }

    public static abstract class GroupEntryAction { }

    public static class ShowMessageAction extends GroupEntryAction {
        public final String message;

        public ShowMessageAction(String message) {
            this.message = message;
        }
    }

    public static class NavigateToCreateGroupAction extends GroupEntryAction { }

    public static class NavigateToHomeAction extends GroupEntryAction {
        public final String message;

        public NavigateToHomeAction(String message) {
            this.message = message;
        }
    }
}
