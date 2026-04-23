package com.silveira.care360.data.repository;

import com.silveira.care360.data.mapper.SeguimientoRegistroMapper;
import com.silveira.care360.data.remote.firestore.FirebaseSeguimientoDataSource;
import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.SeguimientoRegistro;
import com.silveira.care360.domain.repository.SeguimientoRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SeguimientoRepositoryImpl implements SeguimientoRepository {

    private final FirebaseSeguimientoDataSource dataSource;

    @Inject
    public SeguimientoRepositoryImpl(FirebaseSeguimientoDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void loadRegistros(String groupId, ResultCallback<List<SeguimientoRegistro>> callback) {
        loadRegistrosInternal(groupId, false, callback);
    }

    @Override
    public void loadRegistrosIncludingDeleted(String groupId, ResultCallback<List<SeguimientoRegistro>> callback) {
        loadRegistrosInternal(groupId, true, callback);
    }

    private void loadRegistrosInternal(String groupId, boolean includeDeleted, ResultCallback<List<SeguimientoRegistro>> callback) {
        dataSource.loadRegistros(groupId).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                callback.onError("No se pudo cargar el seguimiento");
                return;
            }
            List<SeguimientoRegistro> items = new ArrayList<>();
            if (task.getResult() != null) {
                for (com.google.firebase.firestore.QueryDocumentSnapshot doc : task.getResult()) {
                    SeguimientoRegistro item = SeguimientoRegistroMapper.fromDto(SeguimientoRegistroMapper.fromFirestore(doc));
                    if (item != null && (includeDeleted || !item.isDeleted())) {
                        items.add(item);
                    }
                }
            }
            callback.onSuccess(items);
        });
    }

    @Override
    public void saveRegistro(String groupId, SeguimientoRegistro registro, ResultCallback<Void> callback) {
        dataSource.saveRegistro(groupId, registro).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onSuccess(null);
            } else {
                callback.onError("No se pudo guardar el registro");
            }
        });
    }

    @Override
    public void deleteRegistro(String groupId, String registroId, String actorUserId, ResultCallback<Void> callback) {
        dataSource.deleteRegistro(groupId, registroId, actorUserId).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onSuccess(null);
            } else {
                callback.onError("No se pudo eliminar el registro");
            }
        });
    }
}
