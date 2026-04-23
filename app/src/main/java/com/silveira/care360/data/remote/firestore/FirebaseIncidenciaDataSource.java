package com.silveira.care360.data.remote.firestore;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.silveira.care360.data.mapper.IncidenciaMapper;
import com.silveira.care360.data.remote.dto.IncidenciaDocumentDto;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class FirebaseIncidenciaDataSource {

    private final FirebaseFirestore firestore;

    @Inject
    public FirebaseIncidenciaDataSource(FirebaseFirestore firestore) {
        this.firestore = firestore;
    }

    public Task<List<IncidenciaDocumentDto>> getIncidenciasByGroupId(String groupId) {
        TaskCompletionSource<List<IncidenciaDocumentDto>> tcs = new TaskCompletionSource<>();
        incidenciasCollection(groupId)
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        tcs.setException(task.getException());
                        return;
                    }
                    List<IncidenciaDocumentDto> incidencias = new ArrayList<>();
                    QuerySnapshot snapshot = task.getResult();
                    if (snapshot != null) {
                        snapshot.getDocuments().forEach(doc -> {
                            IncidenciaDocumentDto dto = IncidenciaMapper.fromFirestore(doc);
                            if (dto != null) incidencias.add(dto);
                        });
                    }
                    tcs.setResult(incidencias);
                });
        return tcs.getTask();
    }

    public Task<Void> addIncidencia(String groupId, IncidenciaDocumentDto incidencia) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();
        DocumentReference ref = hasText(incidencia.getId())
                ? incidenciasCollection(groupId).document(incidencia.getId())
                : incidenciasCollection(groupId).document();
        ref.set(IncidenciaMapper.toFirestoreMap(incidencia, ref.getId())).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                tcs.setException(task.getException());
                return;
            }
            tcs.setResult(null);
        });
        return tcs.getTask();
    }

    public Task<Void> deleteIncidencia(String groupId, String incidenciaId) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();
        if (!hasText(incidenciaId)) {
            tcs.setException(new IllegalArgumentException("Incidencia invalida"));
            return tcs.getTask();
        }
        incidenciasCollection(groupId).document(incidenciaId).delete().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                tcs.setException(task.getException());
                return;
            }
            tcs.setResult(null);
        });
        return tcs.getTask();
    }

    private CollectionReference incidenciasCollection(String groupId) {
        return firestore.collection("groups").document(groupId).collection("incidencias");
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
