package com.silveira.care360.data.repository;

import com.silveira.care360.data.mapper.MedicamentoMapper;
import com.silveira.care360.data.remote.dto.MedicamentoDocumentDto;
import com.silveira.care360.data.remote.firestore.FirebaseMedicamentoDataSource;
import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.Medicamento;
import com.silveira.care360.domain.repository.MedicamentoRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class MedicamentoRepositoryImpl implements MedicamentoRepository {

    private final FirebaseMedicamentoDataSource firebaseMedicamentoDataSource;

    @Inject
    public MedicamentoRepositoryImpl(FirebaseMedicamentoDataSource firebaseMedicamentoDataSource) {
        this.firebaseMedicamentoDataSource = firebaseMedicamentoDataSource;
    }

    @Override
    public void getMedicamentosByGroupId(String groupId, ResultCallback<List<Medicamento>> callback) {
        firebaseMedicamentoDataSource.getMedicamentosByGroupId(groupId).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                callback.onError("No se pudieron cargar los medicamentos");
                return;
            }

            callback.onSuccess(mapMedicamentos(task.getResult()));
        });
    }

    @Override
    public void getMedicamentoById(String groupId, String medicamentoId, ResultCallback<Medicamento> callback) {
        firebaseMedicamentoDataSource.getMedicamentoById(groupId, medicamentoId).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                callback.onError("No se pudo cargar el medicamento");
                return;
            }

            callback.onSuccess(MedicamentoMapper.fromDto(task.getResult()));
        });
    }

    @Override
    public void addMedicamento(String groupId, Medicamento medicamento, ResultCallback<Void> callback) {
        MedicamentoDocumentDto medicamentoDto = MedicamentoMapper.toDocumentDto(medicamento);

        firebaseMedicamentoDataSource.addMedicamento(groupId, medicamentoDto).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                callback.onError("No se pudo guardar el medicamento");
                return;
            }

            callback.onSuccess(null);
        });
    }

    @Override
    public void updateMedicamento(String groupId, Medicamento medicamento, ResultCallback<Void> callback) {
        MedicamentoDocumentDto medicamentoDto = MedicamentoMapper.toDocumentDto(medicamento);

        firebaseMedicamentoDataSource.updateMedicamento(groupId, medicamentoDto).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                callback.onError("No se pudo actualizar el medicamento");
                return;
            }

            callback.onSuccess(null);
        });
    }

    @Override
    public void deleteMedicamento(String groupId, String medicamentoId, String deletedByUserId, ResultCallback<Void> callback) {
        firebaseMedicamentoDataSource.deleteMedicamento(groupId, medicamentoId, deletedByUserId).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                callback.onError("No se pudo eliminar el medicamento");
                return;
            }

            callback.onSuccess(null);
        });
    }

    private List<Medicamento> mapMedicamentos(List<MedicamentoDocumentDto> dtos) {
        List<Medicamento> medicamentos = new ArrayList<>();
        if (dtos == null) {
            return medicamentos;
        }

        for (MedicamentoDocumentDto dto : dtos) {
            Medicamento medicamento = MedicamentoMapper.fromDto(dto);
            if (medicamento != null) {
                medicamentos.add(medicamento);
            }
        }

        return medicamentos;
    }
}
