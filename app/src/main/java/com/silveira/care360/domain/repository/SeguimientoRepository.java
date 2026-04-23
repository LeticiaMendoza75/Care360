package com.silveira.care360.domain.repository;

import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.SeguimientoRegistro;

import java.util.List;

public interface SeguimientoRepository {
    void loadRegistros(String groupId, ResultCallback<List<SeguimientoRegistro>> callback);
    void loadRegistrosIncludingDeleted(String groupId, ResultCallback<List<SeguimientoRegistro>> callback);
    void saveRegistro(String groupId, SeguimientoRegistro registro, ResultCallback<Void> callback);
    void deleteRegistro(String groupId, String registroId, String actorUserId, ResultCallback<Void> callback);
}
