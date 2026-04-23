package com.silveira.care360.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.SeguimientoRegistro;
import com.silveira.care360.domain.model.User;
import com.silveira.care360.domain.usecase.DeleteSeguimientoUseCase;
import com.silveira.care360.domain.usecase.GetCurrentUserUseCase;
import com.silveira.care360.domain.usecase.LoadSeguimientoUseCase;
import com.silveira.care360.domain.usecase.SaveSeguimientoUseCase;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class SeguimientoViewModel extends ViewModel {

    private final LoadSeguimientoUseCase loadSeguimientoUseCase;
    private final SaveSeguimientoUseCase saveSeguimientoUseCase;
    private final DeleteSeguimientoUseCase deleteSeguimientoUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;

    private final MutableLiveData<State> _state = new MutableLiveData<>(new State());
    public final LiveData<State> state = _state;

    private final MutableLiveData<Action> _action = new MutableLiveData<>();
    public final LiveData<Action> action = _action;

    @Inject
    public SeguimientoViewModel(LoadSeguimientoUseCase loadSeguimientoUseCase,
                                SaveSeguimientoUseCase saveSeguimientoUseCase,
                                DeleteSeguimientoUseCase deleteSeguimientoUseCase,
                                GetCurrentUserUseCase getCurrentUserUseCase) {
        this.loadSeguimientoUseCase = loadSeguimientoUseCase;
        this.saveSeguimientoUseCase = saveSeguimientoUseCase;
        this.deleteSeguimientoUseCase = deleteSeguimientoUseCase;
        this.getCurrentUserUseCase = getCurrentUserUseCase;
    }

    public void load(String groupId) {
        if (isBlank(groupId)) {
            _state.setValue(new State(new ArrayList<>(), false, "No se encontró el grupo"));
            return;
        }
        State current = _state.getValue();
        _state.setValue(new State(current != null ? current.items : new ArrayList<>(), true, null));
        loadSeguimientoUseCase.execute(groupId, new ResultCallback<List<SeguimientoRegistro>>() {
            @Override
            public void onSuccess(List<SeguimientoRegistro> result) {
                _state.postValue(new State(result, false, null));
            }

            @Override
            public void onError(String message) {
                _state.postValue(new State(new ArrayList<>(), false, message));
            }
        });
    }

    public void save(String groupId,
                     String id,
                     String tipo,
                     String valorPrincipal,
                     String valorSecundario,
                     String notas) {
        if (isBlank(groupId)) {
            _action.setValue(new ShowMessageAction("No se encontró el grupo"));
            return;
        }
        if (isBlank(tipo) || isBlank(valorPrincipal)) {
            _action.setValue(new ShowMessageAction("Completa los datos del registro"));
            return;
        }
        if (SeguimientoRegistro.TIPO_TENSION.equals(tipo) && isBlank(valorSecundario)) {
            _action.setValue(new ShowMessageAction("La tensión necesita dos valores"));
            return;
        }
        User user = getCurrentUserUseCase.execute();
        String userId = user != null ? safe(user.getId()) : "";
        SeguimientoRegistro registro = new SeguimientoRegistro(
                safe(id),
                tipo,
                valorPrincipal.trim(),
                safe(valorSecundario).trim(),
                safe(notas).trim(),
                System.currentTimeMillis(),
                userId,
                0L,
                userId,
                0L,
                false
        );
        saveSeguimientoUseCase.execute(groupId, registro, new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                _action.postValue(new ShowMessageAction("Registro guardado"));
                load(groupId);
            }

            @Override
            public void onError(String message) {
                _action.postValue(new ShowMessageAction(message != null ? message : "No se pudo guardar el registro"));
            }
        });
    }

    public void delete(String groupId, String registroId) {
        if (isBlank(groupId) || isBlank(registroId)) {
            return;
        }
        User currentUser = getCurrentUserUseCase.execute();
        String actorUserId = currentUser != null ? currentUser.getId() : "";
        deleteSeguimientoUseCase.execute(groupId, registroId, actorUserId, new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                _action.postValue(new ShowMessageAction("Registro eliminado"));
                load(groupId);
            }

            @Override
            public void onError(String message) {
                _action.postValue(new ShowMessageAction(message != null ? message : "No se pudo eliminar el registro"));
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
        public final List<SeguimientoRegistro> items;
        public final boolean isLoading;
        public final String errorMessage;

        public State() {
            this(new ArrayList<>(), false, null);
        }

        public State(List<SeguimientoRegistro> items, boolean isLoading, String errorMessage) {
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
