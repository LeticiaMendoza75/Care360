package com.silveira.care360.data.repository;

import com.silveira.care360.data.mapper.PatologiaMapper;
import com.silveira.care360.data.remote.firestore.FirebasePatologiaDataSource;
import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.Patologia;
import com.silveira.care360.domain.repository.PatologiaRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class PatologiaRepositoryImpl implements PatologiaRepository {

    private final FirebasePatologiaDataSource dataSource;

    @Inject
    public PatologiaRepositoryImpl(FirebasePatologiaDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void loadPatologias(String groupId, ResultCallback<List<Patologia>> callback) {
        loadPatologiasInternal(groupId, false, callback);
    }

    @Override
    public void loadPatologiasIncludingDeleted(String groupId, ResultCallback<List<Patologia>> callback) {
        loadPatologiasInternal(groupId, true, callback);
    }

    private void loadPatologiasInternal(String groupId, boolean includeDeleted, ResultCallback<List<Patologia>> callback) {
        dataSource.loadPatologias(groupId).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                callback.onError("No se pudieron cargar las patologias");
                return;
            }
            List<Patologia> items = new ArrayList<>();
            if (task.getResult() != null) {
                for (com.google.firebase.firestore.QueryDocumentSnapshot doc : task.getResult()) {
                    Patologia item = PatologiaMapper.fromDto(PatologiaMapper.fromFirestore(doc));
                    if (item != null && (includeDeleted || !item.isDeleted())) {
                        items.add(item);
                    }
                }
            }
            callback.onSuccess(items);
        });
    }

    @Override
    public void savePatologia(String groupId, Patologia patologia, ResultCallback<Void> callback) {
        dataSource.savePatologia(groupId, patologia).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onSuccess(null);
            } else {
                callback.onError("No se pudo guardar la patologia");
            }
        });
    }

    @Override
    public void deletePatologia(String groupId, String patologiaId, String actorUserId, ResultCallback<Void> callback) {
        dataSource.deletePatologia(groupId, patologiaId, actorUserId).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onSuccess(null);
            } else {
                callback.onError("No se pudo eliminar la patologia");
            }
        });
    }
}
