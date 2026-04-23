package com.silveira.care360.domain.usecase;

import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.Documento;
import com.silveira.care360.domain.repository.DocumentoRepository;

import javax.inject.Inject;

public class SaveDocumentoUseCase {

    private final DocumentoRepository documentoRepository;

    @Inject
    public SaveDocumentoUseCase(DocumentoRepository documentoRepository) {
        this.documentoRepository = documentoRepository;
    }

    public void execute(String groupId,
                        String currentUserId,
                        String titulo,
                        String tipo,
                        String fechaDocumento,
                        String notas,
                        String fileUrl,
                        String fileName,
                        String mimeType,
                        ResultCallback<Void> callback) {
        long now = System.currentTimeMillis();
        Documento documento = new Documento(
                null,
                titulo,
                tipo,
                fechaDocumento,
                notas,
                fileUrl,
                fileName,
                mimeType,
                currentUserId,
                now,
                currentUserId,
                now
        );
        documentoRepository.addDocumento(groupId, documento, callback);
    }
}
