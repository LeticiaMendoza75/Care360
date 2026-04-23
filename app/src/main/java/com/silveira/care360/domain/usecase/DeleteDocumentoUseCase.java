package com.silveira.care360.domain.usecase;

import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.Documento;
import com.silveira.care360.domain.repository.DocumentoRepository;
import com.silveira.care360.domain.storage.DocumentFileStorage;

import javax.inject.Inject;

public class DeleteDocumentoUseCase {

    private final DocumentoRepository documentoRepository;
    private final DocumentFileStorage documentFileStorage;

    @Inject
    public DeleteDocumentoUseCase(DocumentoRepository documentoRepository,
                                  DocumentFileStorage documentFileStorage) {
        this.documentoRepository = documentoRepository;
        this.documentFileStorage = documentFileStorage;
    }

    public void execute(String groupId, Documento documento, ResultCallback<Void> callback) {
        if (documento == null || documento.getId() == null || documento.getId().trim().isEmpty()) {
            callback.onError("Documento invalido");
            return;
        }
        documentFileStorage.deleteDocumentFile(documento.getFileUrl(), new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                documentoRepository.deleteDocumento(groupId, documento.getId(), callback);
            }

            @Override
            public void onError(String message) {
                documentoRepository.deleteDocumento(groupId, documento.getId(), callback);
            }
        });
    }
}
