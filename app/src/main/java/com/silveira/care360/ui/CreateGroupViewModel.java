package com.silveira.care360.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.User;
import com.silveira.care360.domain.usecase.CreateGroupUseCase;
import com.silveira.care360.domain.usecase.GetCurrentUserUseCase;
import com.silveira.care360.domain.usecase.SignOutUseCase;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class CreateGroupViewModel extends ViewModel {
    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final SignOutUseCase signOutUseCase;
    private final CreateGroupUseCase createGroupUseCase;

    private final MutableLiveData<CreateGroupState> _state =
            new MutableLiveData<>(new CreateGroupState());
    public LiveData<CreateGroupState> state = _state;

    private final MutableLiveData<CreateGroupAction> _action = new MutableLiveData<>();
    public LiveData<CreateGroupAction> action = _action;

    @Inject
    public CreateGroupViewModel(GetCurrentUserUseCase getCurrentUserUseCase,
                                SignOutUseCase signOutUseCase,
                                CreateGroupUseCase createGroupUseCase) {
        this.getCurrentUserUseCase = getCurrentUserUseCase;
        this.signOutUseCase = signOutUseCase;
        this.createGroupUseCase = createGroupUseCase;
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

    public void createGroup(String nombreGrupo, String personaCuidada) {
        User user = getCurrentUserUseCase.execute();
        if (user == null) {
            _action.setValue(new ShowMessageAction("Usuario no autenticado"));
            return;
        }

        _state.setValue(new CreateGroupState(true));

        createGroupUseCase.execute(nombreGrupo, personaCuidada, user, new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                _state.postValue(new CreateGroupState(false));
                _action.postValue(new NavigateToHomeAction());
            }

            @Override
            public void onError(String message) {
                emitError(message != null ? message : "Error creando el grupo");
            }
        });
    }

    public void onActionHandled() {
        _action.setValue(null);
    }

    private void emitError(String message) {
        _state.postValue(new CreateGroupState(false));
        _action.postValue(new ShowMessageAction(message));
    }

    public static class CreateGroupState {
        public final boolean isLoading;

        public CreateGroupState() {
            this(false);
        }

        public CreateGroupState(boolean isLoading) {
            this.isLoading = isLoading;
        }
    }

    public static abstract class CreateGroupAction { }

    public static class ShowMessageAction extends CreateGroupAction {
        public final String message;

        public ShowMessageAction(String message) {
            this.message = message;
        }
    }

    public static class NavigateToHomeAction extends CreateGroupAction { }
}
