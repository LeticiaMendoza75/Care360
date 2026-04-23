package com.silveira.care360.data.storage;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.provider.OpenableColumns;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.storage.DocumentFileStorage;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class FirebaseDocumentFileStorage implements DocumentFileStorage {

    private final FirebaseStorage firebaseStorage;
    private final Context context;

    @Inject
    public FirebaseDocumentFileStorage(FirebaseStorage firebaseStorage,
                                       @ApplicationContext Context context) {
        this.firebaseStorage = firebaseStorage;
        this.context = context.getApplicationContext();
    }

    @Override
    public void uploadDocumentFile(String groupId, Uri localUri, ResultCallback<UploadResult> callback) {
        if (groupId == null || groupId.trim().isEmpty() || localUri == null) {
            callback.onError("Documento invalido");
            return;
        }

        InputStream inputStream;
        try {
            inputStream = context.getContentResolver().openInputStream(localUri);
            if (inputStream == null) {
                callback.onError("No se pudo acceder al archivo seleccionado");
                return;
            }
        } catch (Exception e) {
            callback.onError("No se pudo acceder al archivo seleccionado");
            return;
        }

        String fileName = resolveFileName(localUri);
        StorageReference fileRef = buildReference(groupId, fileName);

        fileRef.putStream(inputStream)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException() != null ? task.getException() : new IllegalStateException("upload_error");
                    }
                    return fileRef.getDownloadUrl();
                })
                .addOnSuccessListener(uri -> {
                    closeQuietly(inputStream);
                    callback.onSuccess(new UploadResult(
                            uri != null ? uri.toString() : "",
                            fileName,
                            safeMimeType(context.getContentResolver().getType(localUri), inferMimeTypeFromName(fileName))
                    ));
                })
                .addOnFailureListener(e -> {
                    closeQuietly(inputStream);
                    callback.onError("No se pudo subir el documento");
                });
    }

    @Override
    public void uploadDocumentPhoto(String groupId, byte[] jpegBytes, ResultCallback<UploadResult> callback) {
        if (groupId == null || groupId.trim().isEmpty() || jpegBytes == null || jpegBytes.length == 0) {
            callback.onError("Foto invalida");
            return;
        }

        String fileName = "foto_documento_" + timestamp() + ".jpg";
        String mimeType = "image/jpeg";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(jpegBytes);
        StorageReference fileRef = buildReference(groupId, fileName);
        fileRef.putStream(inputStream)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException() != null ? task.getException() : new IllegalStateException("upload_error");
                    }
                    return fileRef.getDownloadUrl();
                })
                .addOnSuccessListener(uri -> {
                    closeQuietly(inputStream);
                    callback.onSuccess(new UploadResult(
                            uri != null ? uri.toString() : "",
                            fileName,
                            mimeType
                    ));
                })
                .addOnFailureListener(e -> {
                    closeQuietly(inputStream);
                    callback.onError("No se pudo subir la foto");
                });
    }

    @Override
    public void deleteDocumentFile(String fileUrl, ResultCallback<Void> callback) {
        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            callback.onSuccess(null);
            return;
        }

        try {
            firebaseStorage.getReferenceFromUrl(fileUrl.trim())
                    .delete()
                    .addOnSuccessListener(unused -> callback.onSuccess(null))
                    .addOnFailureListener(e -> callback.onSuccess(null));
        } catch (Exception ignored) {
            callback.onSuccess(null);
        }
    }

    private StorageReference buildReference(String groupId, String fileName) {
        return firebaseStorage.getReference()
                .child("groups")
                .child(groupId)
                .child("documentos")
                .child(timestamp() + "_" + sanitize(fileName));
    }

    private String resolveFileName(Uri uri) {
        String fallback = "documento_" + timestamp();
        if (uri == null) return fallback;
        ContentResolver resolver = context.getContentResolver();
        try (android.database.Cursor cursor = resolver.query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (index >= 0) {
                    String name = cursor.getString(index);
                    if (name != null && !name.trim().isEmpty()) {
                        return name.trim();
                    }
                }
            }
        } catch (Exception ignored) {
        }
        String lastSegment = uri.getLastPathSegment();
        if (lastSegment != null && !lastSegment.trim().isEmpty()) {
            return lastSegment.trim();
        }
        return fallback;
    }

    private String inferMimeTypeFromName(String fileName) {
        if (fileName == null) return "application/octet-stream";
        String lower = fileName.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".pdf")) return "application/pdf";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".png")) return "image/png";
        return "application/octet-stream";
    }

    private String safeMimeType(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private String sanitize(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return "documento";
        }
        return fileName.trim().replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private void closeQuietly(Closeable closeable) {
        if (closeable == null) return;
        try {
            closeable.close();
        } catch (Exception ignored) {
        }
    }

    private String timestamp() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
    }
}
