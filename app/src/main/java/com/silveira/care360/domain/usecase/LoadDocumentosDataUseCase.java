package com.silveira.care360.domain.usecase;

import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.Documento;
import com.silveira.care360.domain.repository.DocumentoRepository;
import com.silveira.care360.domain.repository.UserRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

public class LoadDocumentosDataUseCase {

    private final UserRepository userRepository;
    private final DocumentoRepository documentoRepository;

    @Inject
    public LoadDocumentosDataUseCase(UserRepository userRepository,
                                     DocumentoRepository documentoRepository) {
        this.userRepository = userRepository;
        this.documentoRepository = documentoRepository;
    }

    public void execute(String userId, ResultCallback<Result> callback) {
        userRepository.getActiveGroupId(userId, new ResultCallback<String>() {
            @Override
            public void onSuccess(String groupId) {
                if (groupId == null || groupId.trim().isEmpty()) {
                    callback.onError("No hay grupo activo");
                    return;
                }
                documentoRepository.getDocumentosByGroupId(groupId, new ResultCallback<List<Documento>>() {
                    @Override
                    public void onSuccess(List<Documento> documentos) {
                        List<Documento> sorted = new ArrayList<>(documentos != null ? documentos : new ArrayList<>());
                        Collections.sort(sorted, Comparator.comparingLong(Documento::getUpdatedAt).reversed());
                        callback.onSuccess(new Result(groupId, sorted));
                    }

                    @Override
                    public void onError(String message) {
                        callback.onError("No se pudieron cargar los documentos");
                    }
                });
            }

            @Override
            public void onError(String message) {
                callback.onError("No se pudo cargar el grupo activo");
            }
        });
    }

    public static class Result {
        private final String activeGroupId;
        private final List<Documento> documentos;

        public Result(String activeGroupId, List<Documento> documentos) {
            this.activeGroupId = activeGroupId;
            this.documentos = documentos != null ? documentos : new ArrayList<>();
        }

        public String getActiveGroupId() { return activeGroupId; }
        public List<Documento> getDocumentos() { return documentos; }
    }
}
