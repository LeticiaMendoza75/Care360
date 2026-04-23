package com.silveira.care360.data.remote.firestore;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.silveira.care360.data.mapper.DocumentoMapper;
import com.silveira.care360.data.remote.dto.DocumentoDocumentDto;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class FirebaseDocumentoDataSource {

    private final FirebaseFirestore firestore;

    @Inject
    public FirebaseDocumentoDataSource(FirebaseFirestore firestore) {
        this.firestore = firestore;
    }

    public Task<List<DocumentoDocumentDto>> getDocumentosByGroupId(String groupId) {
        TaskCompletionSource<List<DocumentoDocumentDto>> tcs = new TaskCompletionSource<>();
        documentosCollection(groupId).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                tcs.setException(task.getException());
                return;
            }
            List<DocumentoDocumentDto> documentos = new ArrayList<>();
            QuerySnapshot snapshot = task.getResult();
            if (snapshot != null) {
                snapshot.getDocuments().forEach(doc -> {
                    DocumentoDocumentDto dto = DocumentoMapper.fromFirestore(doc);
                    if (dto != null) {
                        documentos.add(dto);
                    }
                });
            }
            tcs.setResult(documentos);
        });
        return tcs.getTask();
    }

    public Task<Void> addDocumento(String groupId, DocumentoDocumentDto documento) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();
        DocumentReference ref = hasText(documento.getId())
                ? documentosCollection(groupId).document(documento.getId())
                : documentosCollection(groupId).document();
        ref.set(DocumentoMapper.toFirestoreMap(documento, ref.getId())).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                tcs.setException(task.getException());
                return;
            }
            tcs.setResult(null);
        });
        return tcs.getTask();
    }

    public Task<Void> updateDocumento(String groupId, DocumentoDocumentDto documento) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();
        if (documento == null || !hasText(documento.getId())) {
            tcs.setException(new IllegalArgumentException("Documento invalido"));
            return tcs.getTask();
        }
        documentosCollection(groupId).document(documento.getId())
                .set(DocumentoMapper.toFirestoreMap(documento, documento.getId()))
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        tcs.setException(task.getException());
                        return;
                    }
                    tcs.setResult(null);
                });
        return tcs.getTask();
    }

    public Task<Void> deleteDocumento(String groupId, String documentoId) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();
        if (!hasText(documentoId)) {
            tcs.setException(new IllegalArgumentException("Documento invalido"));
            return tcs.getTask();
        }
        documentosCollection(groupId).document(documentoId).delete().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                tcs.setException(task.getException());
                return;
            }
            tcs.setResult(null);
        });
        return tcs.getTask();
    }

    private CollectionReference documentosCollection(String groupId) {
        return firestore.collection("groups").document(groupId).collection("documentos");
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
