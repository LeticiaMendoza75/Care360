package com.silveira.care360.domain.repository;

import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.Medicamento;

import java.util.List;

public interface MedicamentoRepository {

    void getMedicamentosByGroupId(String groupId, ResultCallback<List<Medicamento>> callback);

    void getMedicamentoById(String groupId, String medicamentoId, ResultCallback<Medicamento> callback);

    void addMedicamento(String groupId, Medicamento medicamento, ResultCallback<Void> callback);

    void updateMedicamento(String groupId, Medicamento medicamento, ResultCallback<Void> callback);

    void deleteMedicamento(String groupId, String medicamentoId, String deletedByUserId, ResultCallback<Void> callback);
}
