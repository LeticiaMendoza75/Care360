package com.silveira.care360.data.remote.firestore;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.silveira.care360.data.mapper.MedicamentoMapper;
import com.silveira.care360.data.remote.dto.DiaMedicacionDocumentDto;
import com.silveira.care360.data.remote.dto.MedicamentoDocumentDto;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class FirebaseMedicamentoDataSource {

    private final FirebaseFirestore firestore;

    @Inject
    public FirebaseMedicamentoDataSource(FirebaseFirestore firestore) {
        this.firestore = firestore;
    }

    public Task<List<MedicamentoDocumentDto>> getMedicamentosByGroupId(String groupId) {
        TaskCompletionSource<List<MedicamentoDocumentDto>> tcs = new TaskCompletionSource<>();

        medicamentosCollection(groupId)
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        tcs.setException(task.getException());
                        return;
                    }

                    QuerySnapshot snapshot = task.getResult();
                    List<DocumentSnapshot> docs = snapshot != null
                            ? new ArrayList<>(snapshot.getDocuments())
                            : new ArrayList<>();

                    loadMedicamentosSequentially(docs, 0, new ArrayList<>(), tcs);
                });

        return tcs.getTask();
    }

    public Task<MedicamentoDocumentDto> getMedicamentoById(String groupId, String medicamentoId) {
        TaskCompletionSource<MedicamentoDocumentDto> tcs = new TaskCompletionSource<>();

        DocumentReference medRef = medicamentosCollection(groupId).document(medicamentoId);

        medRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                tcs.setException(task.getException());
                return;
            }

            DocumentSnapshot doc = task.getResult();
            if (doc == null || !doc.exists()) {
                tcs.setResult(null);
                return;
            }

            loadDiasForMedicamento(medRef).addOnCompleteListener(daysTask -> {
                if (!daysTask.isSuccessful()) {
                    tcs.setException(daysTask.getException());
                    return;
                }

                List<DiaMedicacionDocumentDto> dias = daysTask.getResult();
                tcs.setResult(MedicamentoMapper.fromFirestore(doc, dias));
            });
        });

        return tcs.getTask();
    }

    public Task<Void> addMedicamento(String groupId, MedicamentoDocumentDto medicamento) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();

        DocumentReference medRef;
        if (medicamento.getId() != null && !medicamento.getId().trim().isEmpty()) {
            medRef = medicamentosCollection(groupId).document(medicamento.getId());
        } else {
            medRef = medicamentosCollection(groupId).document();
        }

        String medicamentoId = medRef.getId();

        WriteBatch batch = firestore.batch();

        batch.set(medRef, MedicamentoMapper.toFirestoreMedicamentoMap(medicamento, medicamentoId));

        List<DiaMedicacionDocumentDto> dias = medicamento.getDias();
        if (dias != null) {
            for (DiaMedicacionDocumentDto dia : dias) {
                if (dia == null || dia.getFecha() == null || dia.getFecha().trim().isEmpty()) {
                    continue;
                }

                String fechaKey = dia.getFechaKey() != null && !dia.getFechaKey().trim().isEmpty()
                        ? dia.getFechaKey()
                        : buildFechaKey(dia.getFecha());
                DocumentReference diaRef = medRef.collection("dias").document(fechaKey);

                batch.set(diaRef, MedicamentoMapper.toFirestoreDiaMap(dia, fechaKey));
            }
        }

        batch.commit().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                tcs.setException(task.getException());
                return;
            }
            tcs.setResult(null);
        });

        return tcs.getTask();
    }

    private void loadMedicamentosSequentially(List<DocumentSnapshot> docs,
                                              int index,
                                              List<MedicamentoDocumentDto> result,
                                              TaskCompletionSource<List<MedicamentoDocumentDto>> tcs) {
        if (index >= docs.size()) {
            tcs.setResult(result);
            return;
        }

        DocumentSnapshot doc = docs.get(index);
        DocumentReference medRef = doc.getReference();

        loadDiasForMedicamento(medRef).addOnCompleteListener(daysTask -> {
            if (!daysTask.isSuccessful()) {
                tcs.setException(daysTask.getException());
                return;
            }

            List<DiaMedicacionDocumentDto> dias = daysTask.getResult();
            MedicamentoDocumentDto medicamento = MedicamentoMapper.fromFirestore(doc, dias);
            if (medicamento != null) {
                result.add(medicamento);
            }

            loadMedicamentosSequentially(docs, index + 1, result, tcs);
        });
    }

    private Task<List<DiaMedicacionDocumentDto>> loadDiasForMedicamento(DocumentReference medRef) {
        TaskCompletionSource<List<DiaMedicacionDocumentDto>> tcs = new TaskCompletionSource<>();

        medRef.collection("dias")
                .orderBy("fechaKey")
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        tcs.setException(task.getException());
                        return;
                    }

                    QuerySnapshot snapshot = task.getResult();
                    List<DiaMedicacionDocumentDto> dias = new ArrayList<>();

                    if (snapshot != null) {
                        for (QueryDocumentSnapshot doc : snapshot) {
                            DiaMedicacionDocumentDto dia = MedicamentoMapper.fromFirestoreDia(doc);
                            if (dia != null) {
                                dias.add(dia);
                            }
                        }
                    }

                    tcs.setResult(dias);
                });

        return tcs.getTask();
    }

    public Task<Void> updateMedicamento(String groupId, MedicamentoDocumentDto medicamento) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();

        if (medicamento == null || medicamento.getId() == null || medicamento.getId().trim().isEmpty()) {
            tcs.setException(new IllegalArgumentException("Medicamento invalido"));
            return tcs.getTask();
        }

        DocumentReference medRef = medicamentosCollection(groupId).document(medicamento.getId());

        medRef.collection("dias").get().addOnCompleteListener(daysTask -> {
            if (!daysTask.isSuccessful()) {
                tcs.setException(daysTask.getException());
                return;
            }

            WriteBatch batch = firestore.batch();
            batch.set(medRef, MedicamentoMapper.toFirestoreMedicamentoMap(medicamento, medicamento.getId()));

            QuerySnapshot existingDays = daysTask.getResult();
            if (existingDays != null) {
                for (QueryDocumentSnapshot doc : existingDays) {
                    batch.delete(doc.getReference());
                }
            }

            List<DiaMedicacionDocumentDto> dias = medicamento.getDias();
            if (dias != null) {
                for (DiaMedicacionDocumentDto dia : dias) {
                    if (dia == null || dia.getFecha() == null || dia.getFecha().trim().isEmpty()) {
                        continue;
                    }

                    String fechaKey = dia.getFechaKey() != null && !dia.getFechaKey().trim().isEmpty()
                            ? dia.getFechaKey()
                            : buildFechaKey(dia.getFecha());
                    DocumentReference diaRef = medRef.collection("dias").document(fechaKey);
                    batch.set(diaRef, MedicamentoMapper.toFirestoreDiaMap(dia, fechaKey));
                }
            }

            batch.commit().addOnCompleteListener(commitTask -> {
                if (!commitTask.isSuccessful()) {
                    tcs.setException(commitTask.getException());
                    return;
                }
                tcs.setResult(null);
            });
        });

        return tcs.getTask();
    }

    public Task<Void> deleteMedicamento(String groupId, String medicamentoId, String deletedByUserId) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();

        if (medicamentoId == null || medicamentoId.trim().isEmpty()) {
            tcs.setException(new IllegalArgumentException("Medicamento invalido"));
            return tcs.getTask();
        }

        DocumentReference medRef = medicamentosCollection(groupId).document(medicamentoId);
        medRef.collection("dias").get().addOnCompleteListener(daysTask -> {
            if (!daysTask.isSuccessful()) {
                tcs.setException(daysTask.getException());
                return;
            }

            WriteBatch batch = firestore.batch();
            java.util.Map<String, Object> updates = new java.util.HashMap<>();
            updates.put("deleted", true);
            updates.put("updatedBy", deletedByUserId);
            updates.put("updatedAt", com.google.firebase.Timestamp.now());
            batch.set(medRef, updates, com.google.firebase.firestore.SetOptions.merge());

            batch.commit().addOnCompleteListener(commitTask -> {
                if (!commitTask.isSuccessful()) {
                    tcs.setException(commitTask.getException());
                    return;
                }
                tcs.setResult(null);
            });
        });

        return tcs.getTask();
    }

    private CollectionReference medicamentosCollection(String groupId) {
        return firestore.collection("groups")
                .document(groupId)
                .collection("medicamentos");
    }

    private String buildFechaKey(String fecha) {
        String[] parts = fecha.split("/");
        if (parts.length == 3) {
            String dd = parts[0];
            String mm = parts[1];
            String yyyy = parts[2];
            return yyyy + "-" + mm + "-" + dd;
        }
        return fecha.replace("/", "-").trim();
    }
}
