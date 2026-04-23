package com.silveira.care360.data.remote.firestore;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.silveira.care360.data.mapper.PatologiaMapper;
import com.silveira.care360.domain.model.Patologia;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FirebasePatologiaDataSource {

    private final FirebaseFirestore firestore;

    @Inject
    public FirebasePatologiaDataSource(FirebaseFirestore firestore) {
        this.firestore = firestore;
    }

    public Task<com.google.firebase.firestore.QuerySnapshot> loadPatologias(String groupId) {
        return patologias(groupId).orderBy("updatedAt", Query.Direction.DESCENDING).get();
    }

    public Task<Void> savePatologia(String groupId, Patologia patologia) {
        if (patologia == null) {
            return com.google.android.gms.tasks.Tasks.forException(new IllegalArgumentException("Patologia nula"));
        }
        if (isBlank(patologia.getId())) {
            Map<String, Object> createData = PatologiaMapper.toCreateMap(patologia);
            return patologias(groupId).document().set(createData);
        }
        return patologias(groupId)
                .document(patologia.getId())
                .set(PatologiaMapper.toUpdateMap(patologia), SetOptions.merge());
    }

    public Task<Void> deletePatologia(String groupId, String patologiaId) {
        return deletePatologia(groupId, patologiaId, null);
    }

    public Task<Void> deletePatologia(String groupId, String patologiaId, String actorUserId) {
        return patologias(groupId)
                .document(patologiaId)
                .set(PatologiaMapper.toSoftDeleteMap(actorUserId), SetOptions.merge());
    }

    private CollectionReference patologias(String groupId) {
        return firestore.collection("groups").document(groupId).collection("patologias");
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
