package com.silveira.care360.domain.repository;

import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.Patologia;

import java.util.List;

public interface PatologiaRepository {
    void loadPatologias(String groupId, ResultCallback<List<Patologia>> callback);
    void loadPatologiasIncludingDeleted(String groupId, ResultCallback<List<Patologia>> callback);
    void savePatologia(String groupId, Patologia patologia, ResultCallback<Void> callback);
    void deletePatologia(String groupId, String patologiaId, String actorUserId, ResultCallback<Void> callback);
}
