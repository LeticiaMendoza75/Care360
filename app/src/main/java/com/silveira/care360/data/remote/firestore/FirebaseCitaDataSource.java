package com.silveira.care360.data.remote.firestore;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.silveira.care360.data.mapper.CitaMapper;
import com.silveira.care360.data.remote.dto.CitaDocumentDto;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class FirebaseCitaDataSource {

    private final FirebaseFirestore firestore;

    @Inject
    public FirebaseCitaDataSource(FirebaseFirestore firestore) {
        this.firestore = firestore;
    }

    public Task<List<CitaDocumentDto>> getCitasByGroupId(String groupId) {
        TaskCompletionSource<List<CitaDocumentDto>> tcs = new TaskCompletionSource<>();

        citasCollection(groupId).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                tcs.setException(task.getException());
                return;
            }

            List<CitaDocumentDto> citas = new ArrayList<>();
            QuerySnapshot snapshot = task.getResult();
            if (snapshot != null) {
                snapshot.getDocuments().forEach(doc -> {
                    CitaDocumentDto dto = CitaMapper.fromFirestore(doc);
                    if (dto != null) {
                        citas.add(dto);
                    }
                });
            }
            tcs.setResult(citas);
        });

        return tcs.getTask();
    }

    public Task<Void> addCita(String groupId, CitaDocumentDto cita) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();
        DocumentReference citaRef = hasText(cita.getId())
                ? citasCollection(groupId).document(cita.getId())
                : citasCollection(groupId).document();

        citaRef.set(CitaMapper.toFirestoreMap(cita, citaRef.getId())).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                tcs.setException(task.getException());
                return;
            }
            tcs.setResult(null);
        });

        return tcs.getTask();
    }

    public Task<Void> updateCita(String groupId, CitaDocumentDto cita) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();
        if (cita == null || !hasText(cita.getId())) {
            tcs.setException(new IllegalArgumentException("Cita invalida"));
            return tcs.getTask();
        }

        citasCollection(groupId).document(cita.getId())
                .set(CitaMapper.toFirestoreMap(cita, cita.getId()))
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        tcs.setException(task.getException());
                        return;
                    }
                    tcs.setResult(null);
                });

        return tcs.getTask();
    }

    public Task<Void> deleteCita(String groupId, String citaId) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();
        if (!hasText(citaId)) {
            tcs.setException(new IllegalArgumentException("Cita invalida"));
            return tcs.getTask();
        }

        citasCollection(groupId).document(citaId).delete().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                tcs.setException(task.getException());
                return;
            }
            tcs.setResult(null);
        });

        return tcs.getTask();
    }

    private CollectionReference citasCollection(String groupId) {
        return firestore.collection("groups").document(groupId).collection("citas");
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
