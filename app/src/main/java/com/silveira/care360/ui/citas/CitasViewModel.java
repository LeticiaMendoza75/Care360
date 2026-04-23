package com.silveira.care360.ui.citas;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.Cita;
import com.silveira.care360.domain.model.User;
import com.silveira.care360.domain.report.CitasPdfExporter;
import com.silveira.care360.domain.usecase.DeleteCitaUseCase;
import com.silveira.care360.domain.usecase.ExportCitasPdfUseCase;
import com.silveira.care360.domain.usecase.GetCurrentUserUseCase;
import com.silveira.care360.domain.usecase.LoadCitasDataUseCase;
import com.silveira.care360.domain.usecase.SaveCitaUseCase;
import com.silveira.care360.domain.usecase.UpdateCitaUseCase;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class CitasViewModel extends ViewModel {

    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final LoadCitasDataUseCase loadCitasDataUseCase;
    private final SaveCitaUseCase saveCitaUseCase;
    private final UpdateCitaUseCase updateCitaUseCase;
    private final DeleteCitaUseCase deleteCitaUseCase;
    private final ExportCitasPdfUseCase exportCitasPdfUseCase;

    private final MutableLiveData<CitasState> _state = new MutableLiveData<>(new CitasState());
    public LiveData<CitasState> state = _state;

    private final MutableLiveData<CitasAction> _action = new MutableLiveData<>();
    public LiveData<CitasAction> action = _action;

    @Inject
    public CitasViewModel(GetCurrentUserUseCase getCurrentUserUseCase,
                          LoadCitasDataUseCase loadCitasDataUseCase,
                          SaveCitaUseCase saveCitaUseCase,
                          UpdateCitaUseCase updateCitaUseCase,
                          DeleteCitaUseCase deleteCitaUseCase,
                          ExportCitasPdfUseCase exportCitasPdfUseCase) {
        this.getCurrentUserUseCase = getCurrentUserUseCase;
        this.loadCitasDataUseCase = loadCitasDataUseCase;
        this.saveCitaUseCase = saveCitaUseCase;
        this.updateCitaUseCase = updateCitaUseCase;
        this.deleteCitaUseCase = deleteCitaUseCase;
        this.exportCitasPdfUseCase = exportCitasPdfUseCase;
    }

    public void loadCitasData() {
        User currentUser = getCurrentUserUseCase.execute();
        if (currentUser == null || isBlank(currentUser.getId())) {
            _action.setValue(new ShowMessageAction("Usuario no autenticado"));
            return;
        }

        CitasState current = _state.getValue();
        _state.setValue(new CitasState(true,
                current != null ? current.activeGroupId : null,
                current != null ? current.citas : new ArrayList<>(),
                null));

        loadCitasDataUseCase.execute(currentUser.getId(), new ResultCallback<LoadCitasDataUseCase.Result>() {
            @Override
            public void onSuccess(LoadCitasDataUseCase.Result result) {
                _state.postValue(new CitasState(false, result.getActiveGroupId(), result.getCitas(), null));
            }

            @Override
            public void onError(String message) {
                emitError(message != null ? message : "No se pudieron cargar las citas");
            }
        });
    }

    public void onAnadirCitaClicked() {
        _action.setValue(new ShowCitaEditorAction(null));
    }

    public void onVerMasClicked(Cita cita) {
        if (cita != null) _action.setValue(new ShowCitaDetailAction(cita));
    }

    public void onGestionarClicked(Cita cita) {
        if (cita != null) _action.setValue(new ShowCitaEditorAction(cita));
    }

    public void onDeleteCitaClicked(Cita cita) {
        if (cita != null) _action.setValue(new ConfirmDeleteCitaAction(cita));
    }

    public void onExportPdfClicked() {
        CitasState current = _state.getValue();
        List<Cita> citas = current != null ? current.citas : null;
        if (citas == null || citas.isEmpty()) {
            _action.setValue(new ShowMessageAction("No hay citas para exportar"));
            return;
        }

        exportCitasPdfUseCase.execute(citas, new ResultCallback<CitasPdfExporter.Result>() {
            @Override
            public void onSuccess(CitasPdfExporter.Result result) {
                if (result == null || result.getUri() == null) {
                    _action.postValue(new ShowMessageAction("No se pudo generar el PDF"));
                    return;
                }
                _action.postValue(new ShareCitasPdfAction(result.getUri(), result.getFileName()));
            }

            @Override
            public void onError(String message) {
                _action.postValue(new ShowMessageAction(message != null ? message : "No se pudo generar el PDF"));
            }
        });
    }

    public void onCitaEditorConfirmed(Cita cita) {
        CitasState current = _state.getValue();
        String activeGroupId = current != null ? current.activeGroupId : null;
        User currentUser = getCurrentUserUseCase.execute();

        if (currentUser == null || isBlank(currentUser.getId())) {
            _action.setValue(new ShowMessageAction("Usuario no autenticado"));
            return;
        }
        if (isBlank(activeGroupId)) {
            _action.setValue(new ShowMessageAction("No hay grupo activo"));
            return;
        }
        if (cita == null || isBlank(cita.getTitulo()) || isBlank(cita.getFecha()) || isBlank(cita.getHora())) {
            _action.setValue(new ShowMessageAction("Completa titulo, fecha y hora"));
            return;
        }

        _state.setValue(new CitasState(true, activeGroupId,
                current != null ? current.citas : new ArrayList<>(), null));

        if (isBlank(cita.getId())) {
            saveCitaUseCase.execute(activeGroupId, currentUser.getId(), cita.getTitulo(), cita.getFecha(),
                    cita.getHora(), cita.getLugar(), cita.getProfesional(), cita.getPersonaEncargada(), cita.getObservaciones(),
                    cita.isRecordatorioActivo(), new ResultCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            _action.postValue(new ShowMessageAction("Cita guardada"));
                            loadCitasData();
                        }

                        @Override
                        public void onError(String message) {
                            emitError("No se pudo guardar la cita");
                        }
                    });
            return;
        }

        updateCitaUseCase.execute(activeGroupId, currentUser.getId(), cita, new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                _action.postValue(new ShowMessageAction("Cita actualizada"));
                loadCitasData();
            }

            @Override
            public void onError(String message) {
                emitError("No se pudo actualizar la cita");
            }
        });
    }

    public void confirmDeleteCita(Cita cita) {
        CitasState current = _state.getValue();
        String activeGroupId = current != null ? current.activeGroupId : null;
        if (cita == null || isBlank(cita.getId()) || isBlank(activeGroupId)) {
            _action.setValue(new ShowMessageAction("No se pudo eliminar la cita"));
            return;
        }

        deleteCitaUseCase.execute(activeGroupId, cita.getId(), new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                _action.postValue(new ShowMessageAction("Cita eliminada"));
                loadCitasData();
            }

            @Override
            public void onError(String message) {
                emitError("No se pudo eliminar la cita");
            }
        });
    }

    public void onActionHandled() {
        _action.setValue(null);
    }

    private void emitError(String message) {
        CitasState current = _state.getValue();
        _state.postValue(new CitasState(false,
                current != null ? current.activeGroupId : null,
                current != null ? current.citas : new ArrayList<>(),
                message));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static class CitasState {
        public final boolean isLoading;
        public final String activeGroupId;
        public final List<Cita> citas;
        public final String errorMessage;

        public CitasState() {
            this(false, null, new ArrayList<>(), null);
        }

        public CitasState(boolean isLoading, String activeGroupId, List<Cita> citas, String errorMessage) {
            this.isLoading = isLoading;
            this.activeGroupId = activeGroupId;
            this.citas = citas != null ? citas : new ArrayList<>();
            this.errorMessage = errorMessage;
        }
    }

    public static abstract class CitasAction { }

    public static class ShowCitaEditorAction extends CitasAction {
        public final Cita cita;
        public ShowCitaEditorAction(Cita cita) { this.cita = cita; }
    }

    public static class ShowCitaDetailAction extends CitasAction {
        public final Cita cita;
        public ShowCitaDetailAction(Cita cita) { this.cita = cita; }
    }

    public static class ConfirmDeleteCitaAction extends CitasAction {
        public final Cita cita;
        public ConfirmDeleteCitaAction(Cita cita) { this.cita = cita; }
    }

    public static class ShowMessageAction extends CitasAction {
        public final String message;
        public ShowMessageAction(String message) { this.message = message; }
    }

    public static class ShareCitasPdfAction extends CitasAction {
        public final Uri uri;
        public final String fileName;

        public ShareCitasPdfAction(Uri uri, String fileName) {
            this.uri = uri;
            this.fileName = fileName;
        }
    }
}
