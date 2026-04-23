package com.silveira.care360.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.Patologia;
import com.silveira.care360.domain.model.User;
import com.silveira.care360.domain.usecase.DeletePatologiaUseCase;
import com.silveira.care360.domain.usecase.GetCurrentUserUseCase;
import com.silveira.care360.domain.usecase.LoadPatologiasUseCase;
import com.silveira.care360.domain.usecase.SavePatologiaUseCase;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class PatologiasViewModel extends ViewModel {

    private final LoadPatologiasUseCase loadPatologiasUseCase;
    private final SavePatologiaUseCase savePatologiaUseCase;
    private final DeletePatologiaUseCase deletePatologiaUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;

    private final MutableLiveData<State> _state = new MutableLiveData<>(new State());
    public final LiveData<State> state = _state;

    private final MutableLiveData<Action> _action = new MutableLiveData<>();
    public final LiveData<Action> action = _action;

    @Inject
    public PatologiasViewModel(LoadPatologiasUseCase loadPatologiasUseCase,
                               SavePatologiaUseCase savePatologiaUseCase,
                               DeletePatologiaUseCase deletePatologiaUseCase,
                               GetCurrentUserUseCase getCurrentUserUseCase) {
        this.loadPatologiasUseCase = loadPatologiasUseCase;
        this.savePatologiaUseCase = savePatologiaUseCase;
        this.deletePatologiaUseCase = deletePatologiaUseCase;
        this.getCurrentUserUseCase = getCurrentUserUseCase;
    }

    public void load(String groupId) {
        if (isBlank(groupId)) {
            _state.setValue(new State(new ArrayList<>(), false, "No se encontró el grupo"));
            return;
        }
        State current = _state.getValue();
        _state.setValue(new State(current != null ? current.items : new ArrayList<>(), true, null));
        loadPatologiasUseCase.execute(groupId, new ResultCallback<List<Patologia>>() {
            @Override
            public void onSuccess(List<Patologia> result) {
                _state.postValue(new State(result, false, null));
            }

            @Override
            public void onError(String message) {
                _state.postValue(new State(new ArrayList<>(), false, message));
            }
        });
    }

    public void save(String groupId, String id, String nombre, String descripcion) {
        if (isBlank(groupId)) {
            _action.setValue(new ShowMessageAction("No se encontró el grupo"));
            return;
        }
        if (isBlank(nombre)) {
            _action.setValue(new ShowMessageAction("Indica el nombre de la patología"));
            return;
        }
        User user = getCurrentUserUseCase.execute();
        String userId = user != null ? safe(user.getId()) : "";
        Patologia patologia = new Patologia(
                safe(id),
                nombre.trim(),
                safe(descripcion).trim(),
                userId,
                0L,
                userId,
                0L,
                false
        );
        savePatologiaUseCase.execute(groupId, patologia, new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                _action.postValue(new ShowMessageAction("Patología guardada"));
                load(groupId);
            }

            @Override
            public void onError(String message) {
                _action.postValue(new ShowMessageAction(message != null ? message : "No se pudo guardar la patología"));
            }
        });
    }

    public void delete(String groupId, String patologiaId) {
        if (isBlank(groupId) || isBlank(patologiaId)) {
            return;
        }
        User currentUser = getCurrentUserUseCase.execute();
        String actorUserId = currentUser != null ? currentUser.getId() : "";
        deletePatologiaUseCase.execute(groupId, patologiaId, actorUserId, new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                _action.postValue(new ShowMessageAction("Patología eliminada"));
                load(groupId);
            }

            @Override
            public void onError(String message) {
                _action.postValue(new ShowMessageAction(message != null ? message : "No se pudo eliminar la patología"));
            }
        });
    }

    public void onActionHandled() {
        _action.setValue(null);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    public static class State {
        public final List<Patologia> items;
        public final boolean isLoading;
        public final String errorMessage;

        public State() {
            this(new ArrayList<>(), false, null);
        }

        public State(List<Patologia> items, boolean isLoading, String errorMessage) {
            this.items = items != null ? items : new ArrayList<>();
            this.isLoading = isLoading;
            this.errorMessage = errorMessage;
        }
    }

    public abstract static class Action { }

    public static class ShowMessageAction extends Action {
        public final String message;

        public ShowMessageAction(String message) {
            this.message = message;
        }
    }
}
