package com.silveira.care360.domain.storage;

import android.net.Uri;

import com.silveira.care360.domain.common.ResultCallback;

public interface DocumentFileStorage {

    void uploadDocumentFile(String groupId, Uri localUri, ResultCallback<UploadResult> callback);

    void uploadDocumentPhoto(String groupId, byte[] jpegBytes, ResultCallback<UploadResult> callback);

    void deleteDocumentFile(String fileUrl, ResultCallback<Void> callback);

    class UploadResult {
        private final String fileUrl;
        private final String fileName;
        private final String mimeType;

        public UploadResult(String fileUrl, String fileName, String mimeType) {
            this.fileUrl = fileUrl;
            this.fileName = fileName;
            this.mimeType = mimeType;
        }

        public String getFileUrl() { return fileUrl; }
        public String getFileName() { return fileName; }
        public String getMimeType() { return mimeType; }
    }
}
