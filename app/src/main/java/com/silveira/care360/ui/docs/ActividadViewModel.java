package com.silveira.care360.ui.docs;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.ActividadItem;
import com.silveira.care360.domain.model.User;
import com.silveira.care360.domain.usecase.GetCurrentUserUseCase;
import com.silveira.care360.domain.usecase.LoadActividadDataUseCase;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ActividadViewModel extends ViewModel {

    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final LoadActividadDataUseCase loadActividadDataUseCase;

    private final MutableLiveData<ActividadState> _state = new MutableLiveData<>(new ActividadState());
    public LiveData<ActividadState> state = _state;

    private final MutableLiveData<ActividadAction> _action = new MutableLiveData<>();
    public LiveData<ActividadAction> action = _action;

    @Inject
    public ActividadViewModel(GetCurrentUserUseCase getCurrentUserUseCase,
                              LoadActividadDataUseCase loadActividadDataUseCase) {
        this.getCurrentUserUseCase = getCurrentUserUseCase;
        this.loadActividadDataUseCase = loadActividadDataUseCase;
    }

    public void loadActividad() {
        User currentUser = getCurrentUserUseCase.execute();
        if (currentUser == null || isBlank(currentUser.getId())) {
            _action.setValue(new ShowMessageAction("Usuario no autenticado"));
            return;
        }
        ActividadState current = _state.getValue();
        _state.setValue(new ActividadState(
                true,
                current != null ? current.activeGroupId : null,
                current != null ? current.activities : new ArrayList<>(),
                current != null ? current.todayCount : 0,
                null
        ));

        loadActividadDataUseCase.execute(currentUser.getId(), new ResultCallback<LoadActividadDataUseCase.Result>() {
            @Override
            public void onSuccess(LoadActividadDataUseCase.Result result) {
                _state.postValue(new ActividadState(
                        false,
                        result != null ? result.getActiveGroupId() : null,
                        result != null ? result.getActivities() : new ArrayList<>(),
                        result != null ? result.getTodayCount() : 0,
                        null
                ));
            }

            @Override
            public void onError(String message) {
                ActividadState currentState = _state.getValue();
                _state.postValue(new ActividadState(
                        false,
                        currentState != null ? currentState.activeGroupId : null,
                        currentState != null ? currentState.activities : new ArrayList<>(),
                        currentState != null ? currentState.todayCount : 0,
                        message != null ? message : "No se pudo cargar la actividad"
                ));
            }
        });
    }

    public void onActionHandled() {
        _action.setValue(null);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static class ActividadState {
        public final boolean isLoading;
        public final String activeGroupId;
        public final List<ActividadItem> activities;
        public final int todayCount;
        public final String errorMessage;

        public ActividadState() {
            this(false, null, new ArrayList<>(), 0, null);
        }

        public ActividadState(boolean isLoading,
                              String activeGroupId,
                              List<ActividadItem> activities,
                              int todayCount,
                              String errorMessage) {
            this.isLoading = isLoading;
            this.activeGroupId = activeGroupId;
            this.activities = activities != null ? activities : new ArrayList<>();
            this.todayCount = todayCount;
            this.errorMessage = errorMessage;
        }
    }

    public static abstract class ActividadAction { }

    public static class ShowMessageAction extends ActividadAction {
        public final String message;

        public ShowMessageAction(String message) {
            this.message = message;
        }
    }
}
