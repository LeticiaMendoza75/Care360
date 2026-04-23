package com.silveira.care360.ui.medicacion;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.DiaMedicacion;
import com.silveira.care360.domain.model.Medicamento;
import com.silveira.care360.domain.model.User;
import com.silveira.care360.domain.usecase.DeleteMedicamentoUseCase;
import com.silveira.care360.domain.usecase.GetCurrentUserUseCase;
import com.silveira.care360.domain.usecase.LoadMedicamentoDetalleUseCase;
import com.silveira.care360.domain.usecase.UpdateMedicamentoUseCase;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class DetalleMedicamentoViewModel extends ViewModel {

    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final LoadMedicamentoDetalleUseCase loadMedicamentoDetalleUseCase;
    private final UpdateMedicamentoUseCase updateMedicamentoUseCase;
    private final DeleteMedicamentoUseCase deleteMedicamentoUseCase;

    private final MutableLiveData<DetalleMedicamentoState> _state =
            new MutableLiveData<>(new DetalleMedicamentoState());
    public LiveData<DetalleMedicamentoState> state = _state;

    private final MutableLiveData<DetalleMedicamentoAction> _action = new MutableLiveData<>();
    public LiveData<DetalleMedicamentoAction> action = _action;

    @Inject
    public DetalleMedicamentoViewModel(
            GetCurrentUserUseCase getCurrentUserUseCase,
            LoadMedicamentoDetalleUseCase loadMedicamentoDetalleUseCase,
            UpdateMedicamentoUseCase updateMedicamentoUseCase,
            DeleteMedicamentoUseCase deleteMedicamentoUseCase
    ) {
        this.getCurrentUserUseCase = getCurrentUserUseCase;
        this.loadMedicamentoDetalleUseCase = loadMedicamentoDetalleUseCase;
        this.updateMedicamentoUseCase = updateMedicamentoUseCase;
        this.deleteMedicamentoUseCase = deleteMedicamentoUseCase;
    }

    public void loadMedicamentoDetalle(String medicamentoId) {
        User currentUser = getCurrentUserUseCase.execute();
        if (currentUser == null) {
            _state.setValue(new DetalleMedicamentoState(false, null, null, "Usuario no autenticado"));
            return;
        }

        _state.setValue(new DetalleMedicamentoState(true, null, null, null));

        loadMedicamentoDetalleUseCase.execute(currentUser.getId(), medicamentoId, new ResultCallback<LoadMedicamentoDetalleUseCase.Result>() {
            @Override
            public void onSuccess(LoadMedicamentoDetalleUseCase.Result result) {
                _state.postValue(new DetalleMedicamentoState(
                        false,
                        result.getActiveGroupId(),
                        result.getMedicamento(),
                        null
                ));
            }

            @Override
            public void onError(String message) {
                _state.postValue(new DetalleMedicamentoState(
                        false,
                        null,
                        null,
                        message != null ? message : "No se pudo obtener el grupo activo"
                ));
            }
        });
    }

    public void onEditarMedicamentoClicked() {
        Medicamento medicamento = getCurrentMedicamento();
        if (medicamento == null) {
            _action.setValue(new ShowMessageAction("Medicamento no disponible"));
            return;
        }
        _action.setValue(new ShowMedicamentoEditorAction(medicamento));
    }

    public void onAnadirDiaClicked() {
        Medicamento medicamento = getCurrentMedicamento();
        if (medicamento == null) {
            _action.setValue(new ShowMessageAction("Medicamento no disponible"));
            return;
        }
        _action.setValue(new ShowMedicamentoEditorAction(medicamento));
    }

    public void onAnadirHoraClicked(DiaMedicacion dia) {
        Medicamento medicamento = getCurrentMedicamento();
        if (medicamento == null || dia == null) {
            _action.setValue(new ShowMessageAction("No se pudo abrir la edicion"));
            return;
        }
        _action.setValue(new ShowMedicamentoEditorAction(medicamento));
    }

    public void onEditarDiaClicked(DiaMedicacion dia) {
        Medicamento medicamento = getCurrentMedicamento();
        if (medicamento == null || dia == null) {
            _action.setValue(new ShowMessageAction("No se pudo editar el dia"));
            return;
        }
        _action.setValue(new ShowMedicamentoEditorAction(medicamento));
    }

    public void onEliminarDiaClicked(DiaMedicacion dia) {
        Medicamento medicamento = getCurrentMedicamento();
        if (medicamento == null || dia == null) {
            _action.setValue(new ShowMessageAction("No se pudo eliminar el dia"));
            return;
        }

        List<DiaMedicacion> nuevosDias = new ArrayList<>();
        for (DiaMedicacion currentDia : medicamento.getDias()) {
            if (currentDia == null) {
                continue;
            }
            if (sameDate(currentDia.getFecha(), dia.getFecha())) {
                continue;
            }
            nuevosDias.add(copyDia(currentDia));
        }

        Medicamento actualizado = copyMedicamento(medicamento);
        actualizado.setDias(nuevosDias);
        saveMedicamentoChanges(actualizado, "Dia eliminado", "No se pudo eliminar el dia");
    }

    public void onEliminarMedicamentoClicked() {
        Medicamento medicamento = getCurrentMedicamento();
        if (medicamento == null) {
            _action.setValue(new ShowMessageAction("Medicamento no disponible"));
            return;
        }
        _action.setValue(new ConfirmDeleteMedicamentoAction(medicamento));
    }

    public void onMedicamentoEditorConfirmed(Medicamento medicamento) {
        saveMedicamentoChanges(medicamento, "Medicamento actualizado", "No se pudo actualizar el medicamento");
    }

    public void confirmDeleteMedicamento() {
        DetalleMedicamentoState current = _state.getValue();
        Medicamento medicamento = current != null ? current.medicamento : null;
        String activeGroupId = current != null ? current.activeGroupId : null;
        User currentUser = getCurrentUserUseCase.execute();

        if (medicamento == null || isBlank(activeGroupId) || isBlank(medicamento.getId())
                || currentUser == null || isBlank(currentUser.getId())) {
            _action.setValue(new ShowMessageAction("No se pudo eliminar el medicamento"));
            return;
        }

        _state.setValue(new DetalleMedicamentoState(true, activeGroupId, medicamento, null));

        deleteMedicamentoUseCase.execute(activeGroupId, medicamento.getId(), currentUser.getId(), new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                _action.postValue(new ShowMessageAction("Medicamento eliminado"));
                _action.postValue(new FinishAction());
            }

            @Override
            public void onError(String message) {
                _state.postValue(new DetalleMedicamentoState(
                        false,
                        activeGroupId,
                        medicamento,
                        "No se pudo eliminar el medicamento"
                ));
            }
        });
    }

    public void onActionHandled() {
        _action.setValue(null);
    }

    private void saveMedicamentoChanges(Medicamento medicamento, String successMessage, String errorMessage) {
        DetalleMedicamentoState current = _state.getValue();
        String activeGroupId = current != null ? current.activeGroupId : null;
        User currentUser = getCurrentUserUseCase.execute();

        if (medicamento == null || isBlank(activeGroupId) || currentUser == null || isBlank(currentUser.getId())) {
            _action.setValue(new ShowMessageAction(errorMessage));
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

        _state.setValue(new DetalleMedicamentoState(true, activeGroupId, current != null ? current.medicamento : null, null));

        updateMedicamentoUseCase.execute(activeGroupId, currentUser.getId(), medicamento, new ResultCallback<UpdateMedicamentoUseCase.Result>() {
            @Override
            public void onSuccess(UpdateMedicamentoUseCase.Result result) {
                _action.postValue(new ShowMessageAction(successMessage));
                if (result != null && result.isExactAlarmPermissionRequired()) {
                    _action.postValue(new ShowExactAlarmPermissionAction());
                }
                loadMedicamentoDetalle(medicamento.getId());
            }

            @Override
            public void onError(String message) {
                _state.postValue(new DetalleMedicamentoState(
                        false,
                        activeGroupId,
                        current != null ? current.medicamento : null,
                        errorMessage
                ));
            }
        });
    }

    private Medicamento getCurrentMedicamento() {
        DetalleMedicamentoState current = _state.getValue();
        return current != null ? current.medicamento : null;
    }

    private Medicamento copyMedicamento(Medicamento medicamento) {
        Medicamento copy = new Medicamento();
        copy.setId(medicamento.getId());
        copy.setNombre(medicamento.getNombre());
        copy.setFechaInicio(medicamento.getFechaInicio());
        copy.setFechaFin(medicamento.getFechaFin());
        copy.setObservaciones(medicamento.getObservaciones());
        copy.setAlertasActivas(medicamento.isAlertasActivas());
        copy.setDias(copyDias(medicamento.getDias()));
        copy.setCreatedBy(medicamento.getCreatedBy());
        copy.setCreatedAt(medicamento.getCreatedAt());
        copy.setUpdatedBy(medicamento.getUpdatedBy());
        copy.setUpdatedAt(medicamento.getUpdatedAt());
        return copy;
    }

    private List<DiaMedicacion> copyDias(List<DiaMedicacion> dias) {
        List<DiaMedicacion> result = new ArrayList<>();
        if (dias == null) {
            return result;
        }

        for (DiaMedicacion dia : dias) {
            if (dia != null) {
                result.add(copyDia(dia));
            }
        }
        return result;
    }

    private DiaMedicacion copyDia(DiaMedicacion dia) {
        return new DiaMedicacion(
                dia.getFecha(),
                dia.getHoras() != null ? new ArrayList<>(dia.getHoras()) : new ArrayList<>()
        );
    }

    private boolean sameDate(String left, String right) {
        String safeLeft = left != null ? left.trim() : "";
        String safeRight = right != null ? right.trim() : "";
        return safeLeft.equalsIgnoreCase(safeRight);
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

    public static class DetalleMedicamentoState {
        public final boolean isLoading;
        public final String activeGroupId;
        public final Medicamento medicamento;
        public final String errorMessage;

        public DetalleMedicamentoState() {
            this(false, null, null, null);
        }

        public DetalleMedicamentoState(boolean isLoading,
                                       String activeGroupId,
                                       Medicamento medicamento,
                                       String errorMessage) {
            this.isLoading = isLoading;
            this.activeGroupId = activeGroupId;
            this.medicamento = medicamento;
            this.errorMessage = errorMessage;
        }
    }

    public static abstract class DetalleMedicamentoAction { }

    public static class ShowMedicamentoEditorAction extends DetalleMedicamentoAction {
        public final Medicamento medicamento;

        public ShowMedicamentoEditorAction(Medicamento medicamento) {
            this.medicamento = medicamento;
        }
    }

    public static class ConfirmDeleteMedicamentoAction extends DetalleMedicamentoAction {
        public final Medicamento medicamento;

        public ConfirmDeleteMedicamentoAction(Medicamento medicamento) {
            this.medicamento = medicamento;
        }
    }

    public static class ShowMessageAction extends DetalleMedicamentoAction {
        public final String message;

        public ShowMessageAction(String message) {
            this.message = message;
        }
    }

    public static class ShowExactAlarmPermissionAction extends DetalleMedicamentoAction { }

    public static class FinishAction extends DetalleMedicamentoAction { }
}
