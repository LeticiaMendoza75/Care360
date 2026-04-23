package com.silveira.care360.data.repository;

import com.silveira.care360.data.mapper.DocumentoMapper;
import com.silveira.care360.data.remote.dto.DocumentoDocumentDto;
import com.silveira.care360.data.remote.firestore.FirebaseDocumentoDataSource;
import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.Documento;
import com.silveira.care360.domain.repository.DocumentoRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class DocumentoRepositoryImpl implements DocumentoRepository {

    private final FirebaseDocumentoDataSource firebaseDocumentoDataSource;

    @Inject
    public DocumentoRepositoryImpl(FirebaseDocumentoDataSource firebaseDocumentoDataSource) {
        this.firebaseDocumentoDataSource = firebaseDocumentoDataSource;
    }

    @Override
    public void getDocumentosByGroupId(String groupId, ResultCallback<List<Documento>> callback) {
        firebaseDocumentoDataSource.getDocumentosByGroupId(groupId).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                callback.onError("No se pudieron cargar los documentos");
                return;
            }
            List<Documento> documentos = new ArrayList<>();
            List<DocumentoDocumentDto> dtos = task.getResult();
            if (dtos != null) {
                for (DocumentoDocumentDto dto : dtos) {
                    Documento documento = DocumentoMapper.fromDto(dto);
                    if (documento != null) {
                        documentos.add(documento);
                    }
                }
            }
            callback.onSuccess(documentos);
        });
    }

    @Override
    public void addDocumento(String groupId, Documento documento, ResultCallback<Void> callback) {
        firebaseDocumentoDataSource.addDocumento(groupId, DocumentoMapper.toDto(documento)).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                callback.onError("No se pudo guardar el documento");
                return;
            }
            callback.onSuccess(null);
        });
    }

    @Override
    public void updateDocumento(String groupId, Documento documento, ResultCallback<Void> callback) {
        firebaseDocumentoDataSource.updateDocumento(groupId, DocumentoMapper.toDto(documento)).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                callback.onError("No se pudo actualizar el documento");
                return;
            }
            callback.onSuccess(null);
        });
    }

    @Override
    public void deleteDocumento(String groupId, String documentoId, ResultCallback<Void> callback) {
        firebaseDocumentoDataSource.deleteDocumento(groupId, documentoId).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                callback.onError("No se pudo eliminar el documento");
                return;
            }
            callback.onSuccess(null);
        });
    }
}
