package com.silveira.care360.domain.usecase;

import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.storage.DocumentFileStorage;

import javax.inject.Inject;

public class UploadDocumentoPhotoUseCase {

    private final DocumentFileStorage documentFileStorage;

    @Inject
    public UploadDocumentoPhotoUseCase(DocumentFileStorage documentFileStorage) {
        this.documentFileStorage = documentFileStorage;
    }

    public void execute(String groupId, byte[] jpegBytes, ResultCallback<DocumentFileStorage.UploadResult> callback) {
        documentFileStorage.uploadDocumentPhoto(groupId, jpegBytes, callback);
    }
}
