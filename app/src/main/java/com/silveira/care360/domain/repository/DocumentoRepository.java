package com.silveira.care360.domain.repository;

import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.Documento;

import java.util.List;

public interface DocumentoRepository {

    void getDocumentosByGroupId(String groupId, ResultCallback<List<Documento>> callback);

    void addDocumento(String groupId, Documento documento, ResultCallback<Void> callback);

    void updateDocumento(String groupId, Documento documento, ResultCallback<Void> callback);

    void deleteDocumento(String groupId, String documentoId, ResultCallback<Void> callback);
}
