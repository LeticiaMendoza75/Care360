package com.silveira.care360.domain.usecase;

import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.Documento;
import com.silveira.care360.domain.repository.DocumentoRepository;

import javax.inject.Inject;

public class UpdateDocumentoUseCase {

    private final DocumentoRepository documentoRepository;

    @Inject
    public UpdateDocumentoUseCase(DocumentoRepository documentoRepository) {
        this.documentoRepository = documentoRepository;
    }

    public void execute(String groupId, String currentUserId, Documento documento, ResultCallback<Void> callback) {
        if (documento == null) {
            callback.onError("Documento invalido");
            return;
        }
        documento.setUpdatedBy(currentUserId);
        documento.setUpdatedAt(System.currentTimeMillis());
        documentoRepository.updateDocumento(groupId, documento, callback);
    }
}
