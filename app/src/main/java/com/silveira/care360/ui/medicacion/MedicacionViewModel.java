package com.silveira.care360.ui.medicacion;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.DiaMedicacion;
import com.silveira.care360.domain.model.Medicamento;
import com.silveira.care360.domain.model.User;
import com.silveira.care360.domain.report.MedicacionPdfExporter;
import com.silveira.care360.domain.usecase.DeleteMedicamentoUseCase;
import com.silveira.care360.domain.usecase.ExportMedicacionPdfUseCase;
import com.silveira.care360.domain.usecase.GetCurrentUserUseCase;
import com.silveira.care360.domain.usecase.LoadMedicacionDataUseCase;
import com.silveira.care360.domain.usecase.SaveMedicamentoUseCase;
import com.silveira.care360.domain.usecase.UpdateMedicamentoUseCase;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class MedicacionViewModel extends ViewModel {

    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final LoadMedicacionDataUseCase loadMedicacionDataUseCase;
    private final SaveMedicamentoUseCase saveMedicamentoUseCase;
    private final UpdateMedicamentoUseCase updateMedicamentoUseCase;
    private final DeleteMedicamentoUseCase deleteMedicamentoUseCase;
    private final ExportMedicacionPdfUseCase exportMedicacionPdfUseCase;

    private final MutableLiveData<MedicacionState> _state =
            new MutableLiveData<>(new MedicacionState());
    public LiveData<MedicacionState> state = _state;

    private final MutableLiveData<MedicacionAction> _action = new MutableLiveData<>();
    public LiveData<MedicacionAction> action = _action;

    @Inject
    public MedicacionViewModel(
            GetCurrentUserUseCase getCurrentUserUseCase,
            LoadMedicacionDataUseCase loadMedicacionDataUseCase,
            SaveMedicamentoUseCase saveMedicamentoUseCase,
            UpdateMedicamentoUseCase updateMedicamentoUseCase,
            DeleteMedicamentoUseCase deleteMedicamentoUseCase,
            ExportMedicacionPdfUseCase exportMedicacionPdfUseCase
    ) {
        this.getCurrentUserUseCase = getCurrentUserUseCase;
        this.loadMedicacionDataUseCase = loadMedicacionDataUseCase;
        this.saveMedicamentoUseCase = saveMedicamentoUseCase;
        this.updateMedicamentoUseCase = updateMedicamentoUseCase;
        this.deleteMedicamentoUseCase = deleteMedicamentoUseCase;
        this.exportMedicacionPdfUseCase = exportMedicacionPdfUseCase;
    }

    public void loadMedicacionData() {
        User currentUser = getCurrentUserUseCase.execute();
        if (currentUser == null) {
            _action.setValue(new ShowMessageAction("Usuario no autenticado"));
            return;
        }

        MedicacionState current = _state.getValue();
        _state.setValue(new MedicacionState(
                true,
                current != null ? current.activeGroupId : null,
                current != null ? current.medicamentos : new ArrayList<>(),
                null
        ));

        loadMedicacionDataUseCase.execute(currentUser.getId(), new ResultCallback<LoadMedicacionDataUseCase.Result>() {
            @Override
            public void onSuccess(LoadMedicacionDataUseCase.Result result) {
                _state.postValue(new MedicacionState(
                        false,
                        result.getActiveGroupId(),
                        result.getMedicamentos(),
                        null
                ));
            }

            @Override
            public void onError(String message) {
                emitError(message != null ? message : "No se pudo cargar el grupo activo");
            }
        });
    }

    public void onAnadirMedicamentoClicked() {
        _action.setValue(new ShowMedicamentoEditorAction(null));
    }

    public void onExportPdfClicked() {
        MedicacionState current = _state.getValue();
        List<Medicamento> medicamentos = current != null ? current.medicamentos : null;
        if (medicamentos == null || medicamentos.isEmpty()) {
            _action.setValue(new ShowMessageAction("No hay medicacion para exportar"));
            return;
        }

        exportMedicacionPdfUseCase.execute(medicamentos, new ResultCallback<MedicacionPdfExporter.Result>() {
            @Override
            public void onSuccess(MedicacionPdfExporter.Result result) {
                if (result == null || result.getUri() == null) {
                    _action.postValue(new ShowMessageAction("No se pudo generar el PDF"));
                    return;
                }
                _action.postValue(new ShareMedicacionPdfAction(result.getUri(), result.getFileName()));
            }

            @Override
            public void onError(String message) {
                _action.postValue(new ShowMessageAction(message != null ? message : "No se pudo generar el PDF"));
            }
        });
    }

    public void onMedicamentoEditorConfirmed(Medicamento medicamento) {
        MedicacionState current = _state.getValue();
        String activeGroupId = current != null ? current.activeGroupId : null;

        if (isBlank(activeGroupId)) {
            _action.setValue(new ShowMessageAction("No hay grupo activo"));
            return;
        }

        User currentUser = getCurrentUserUseCase.execute();
        if (currentUser == null || isBlank(currentUser.getId())) {
            _action.setValue(new ShowMessageAction("Usuario no autenticado"));
            return;
        }

        if (medicamento == null) {
            _action.setValue(new ShowMessageAction("Medicamento no valido"));
            return;
        }

        if (isBlank(medicamento.getNombre())) {
            _action.setValue(new ShowMessageAction("Introduce el nombre del medicamento"));
            return;
        }

        if (isBlank(medicamento.getFechaInicio())) {
            _action.setValue(new ShowMessageAction("Introduce la fecha de inicio"));
            return;
        }

        if (medicamento.getDias() == null || medicamento.getDias().isEmpty()) {
            _action.setValue(new ShowMessageAction("Añade al menos un día"));
            return;
        }

        if (!sonDiasValidos(medicamento.getDias())) {
            _action.setValue(new ShowMessageAction("Revisa los dias del medicamento"));
            return;
        }

        _state.setValue(new MedicacionState(
                true,
                activeGroupId,
                current != null ? current.medicamentos : new ArrayList<>(),
                null
        ));

        ResultCallback<SaveMedicamentoUseCase.Result> saveCallback = new ResultCallback<SaveMedicamentoUseCase.Result>() {
            @Override
            public void onSuccess(SaveMedicamentoUseCase.Result result) {
                _action.postValue(new ShowMessageAction(
                        isBlank(medicamento.getId()) ? "Medicamento guardado" : "Medicamento actualizado"
                ));
                if (result != null && result.isExactAlarmPermissionRequired()) {
                    _action.postValue(new ShowExactAlarmPermissionAction());
                }
                loadMedicacionData();
            }

            @Override
            public void onError(String message) {
                emitError(isBlank(medicamento.getId())
                        ? "No se pudo guardar el medicamento"
                        : "No se pudo actualizar el medicamento");
            }
        };

        if (isBlank(medicamento.getId())) {
            saveMedicamentoUseCase.execute(
                    activeGroupId,
                    currentUser.getId(),
                    medicamento.getNombre(),
                    medicamento.getFechaInicio(),
                    medicamento.getFechaFin(),
                    medicamento.getObservaciones(),
                    medicamento.isAlertasActivas(),
                    medicamento.getDias(),
                    saveCallback
            );
            return;
        }

        updateMedicamentoUseCase.execute(activeGroupId, currentUser.getId(), medicamento, new ResultCallback<UpdateMedicamentoUseCase.Result>() {
            @Override
            public void onSuccess(UpdateMedicamentoUseCase.Result result) {
                _action.postValue(new ShowMessageAction("Medicamento actualizado"));
                if (result != null && result.isExactAlarmPermissionRequired()) {
                    _action.postValue(new ShowExactAlarmPermissionAction());
                }
                loadMedicacionData();
            }

            @Override
            public void onError(String message) {
                emitError("No se pudo actualizar el medicamento");
            }
        });
    }

    public void onVerMasClicked(Medicamento medicamento) {
        if (medicamento == null) {
            _action.setValue(new ShowMessageAction("Medicamento no válido"));
            return;
        }

        if (isBlank(medicamento.getId())) {
            _action.setValue(new ShowMessageAction("El medicamento no tiene id"));
            return;
        }

        _action.setValue(new NavigateToDetalleMedicamentoAction(medicamento.getId()));
    }

    public void onEditarMedicamentoClicked(Medicamento medicamento) {
        if (medicamento == null) {
            return;
        }
        _action.setValue(new ShowMedicamentoEditorAction(medicamento));
    }

    public void onEliminarMedicamentoClicked(Medicamento medicamento) {
        if (medicamento == null) {
            return;
        }
        _action.setValue(new ConfirmDeleteMedicamentoAction(medicamento));
    }

    public void onAnadirDiaClicked(Medicamento medicamento) {
        if (medicamento == null) {
            return;
        }
        _action.setValue(new ShowMedicamentoEditorAction(medicamento));
    }

    public void onAnadirHoraClicked(Medicamento medicamento, DiaMedicacion dia) {
        if (medicamento == null || dia == null) {
            return;
        }
        _action.setValue(new ShowMedicamentoEditorAction(medicamento));
    }

    public void confirmDeleteMedicamento(Medicamento medicamento) {
        MedicacionState current = _state.getValue();
        String activeGroupId = current != null ? current.activeGroupId : null;
        User currentUser = getCurrentUserUseCase.execute();

        if (medicamento == null || isBlank(activeGroupId) || isBlank(medicamento.getId())
                || currentUser == null || isBlank(currentUser.getId())) {
            _action.setValue(new ShowMessageAction("No se pudo eliminar el medicamento"));
            return;
        }

        _state.setValue(new MedicacionState(
                true,
                activeGroupId,
                current != null ? current.medicamentos : new ArrayList<>(),
                null
        ));

        deleteMedicamentoUseCase.execute(activeGroupId, medicamento.getId(), currentUser.getId(), new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                _action.postValue(new ShowMessageAction("Medicamento eliminado"));
                loadMedicacionData();
            }

            @Override
            public void onError(String message) {
                emitError("No se pudo eliminar el medicamento");
            }
        });
    }

    public void onActionHandled() {
        _action.setValue(null);
    }

    private void emitError(String message) {
        MedicacionState current = _state.getValue();
        _state.postValue(new MedicacionState(
                false,
                current != null ? current.activeGroupId : null,
                current != null ? current.medicamentos : new ArrayList<>(),
                message
        ));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean sonDiasValidos(List<DiaMedicacion> dias) {
        for (DiaMedicacion dia : dias) {
            if (dia == null) return false;
            if (isBlank(dia.getFecha())) return false;
            if (dia.getHoras() != null) {
                for (String hora : dia.getHoras()) {
                    if (isBlank(hora)) return false;
                }
            }
        }
        return true;
    }

    public static class MedicacionState {
        public final boolean isLoading;
        public final String activeGroupId;
        public final List<Medicamento> medicamentos;
        public final String errorMessage;

        public MedicacionState() {
            this(false, null, new ArrayList<>(), null);
        }

        public MedicacionState(boolean isLoading,
                               String activeGroupId,
                               List<Medicamento> medicamentos,
                               String errorMessage) {
            this.isLoading = isLoading;
            this.activeGroupId = activeGroupId;
            this.medicamentos = medicamentos != null ? medicamentos : new ArrayList<>();
            this.errorMessage = errorMessage;
        }
    }

    public static abstract class MedicacionAction { }

    public static class ShowMedicamentoEditorAction extends MedicacionAction {
        public final Medicamento medicamento;

        public ShowMedicamentoEditorAction(Medicamento medicamento) {
            this.medicamento = medicamento;
        }
    }

    public static class NavigateToDetalleMedicamentoAction extends MedicacionAction {
        public final String medicamentoId;

        public NavigateToDetalleMedicamentoAction(String medicamentoId) {
            this.medicamentoId = medicamentoId;
        }
    }

    public static class ShowMessageAction extends MedicacionAction {
        public final String message;

        public ShowMessageAction(String message) {
            this.message = message;
        }
    }

    public static class ShowExactAlarmPermissionAction extends MedicacionAction { }

    public static class ConfirmDeleteMedicamentoAction extends MedicacionAction {
        public final Medicamento medicamento;

        public ConfirmDeleteMedicamentoAction(Medicamento medicamento) {
            this.medicamento = medicamento;
        }
    }

    public static class ShareMedicacionPdfAction extends MedicacionAction {
        public final Uri uri;
        public final String fileName;

        public ShareMedicacionPdfAction(Uri uri, String fileName) {
            this.uri = uri;
            this.fileName = fileName;
        }
    }
}
