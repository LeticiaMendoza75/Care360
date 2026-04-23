package com.silveira.care360.domain.usecase;

import android.net.Uri;

import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.storage.DocumentFileStorage;

import javax.inject.Inject;

public class UploadDocumentoFileUseCase {

    private final DocumentFileStorage documentFileStorage;

    @Inject
    public UploadDocumentoFileUseCase(DocumentFileStorage documentFileStorage) {
        this.documentFileStorage = documentFileStorage;
    }

    public void execute(String groupId, Uri localUri, ResultCallback<DocumentFileStorage.UploadResult> callback) {
        documentFileStorage.uploadDocumentFile(groupId, localUri, callback);
    }
}
