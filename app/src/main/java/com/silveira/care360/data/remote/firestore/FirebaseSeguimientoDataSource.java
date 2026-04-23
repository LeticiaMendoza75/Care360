package com.silveira.care360.data.remote.firestore;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.silveira.care360.data.mapper.SeguimientoRegistroMapper;
import com.silveira.care360.domain.model.SeguimientoRegistro;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FirebaseSeguimientoDataSource {

    private final FirebaseFirestore firestore;

    @Inject
    public FirebaseSeguimientoDataSource(FirebaseFirestore firestore) {
        this.firestore = firestore;
    }

    public Task<com.google.firebase.firestore.QuerySnapshot> loadRegistros(String groupId) {
        return seguimiento(groupId).orderBy("recordedAt", Query.Direction.DESCENDING).get();
    }

    public Task<Void> saveRegistro(String groupId, SeguimientoRegistro registro) {
        if (registro == null) {
            return com.google.android.gms.tasks.Tasks.forException(new IllegalArgumentException("Registro nulo"));
        }
        if (isBlank(registro.getId())) {
            Map<String, Object> createData = SeguimientoRegistroMapper.toCreateMap(registro);
            return seguimiento(groupId).document().set(createData);
        }
        return seguimiento(groupId)
                .document(registro.getId())
                .set(SeguimientoRegistroMapper.toUpdateMap(registro), SetOptions.merge());
    }

    public Task<Void> deleteRegistro(String groupId, String registroId) {
        return deleteRegistro(groupId, registroId, null);
    }

    public Task<Void> deleteRegistro(String groupId, String registroId, String actorUserId) {
        return seguimiento(groupId)
                .document(registroId)
                .set(SeguimientoRegistroMapper.toSoftDeleteMap(actorUserId), SetOptions.merge());
    }

    private CollectionReference seguimiento(String groupId) {
        return firestore.collection("groups").document(groupId).collection("seguimiento");
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
