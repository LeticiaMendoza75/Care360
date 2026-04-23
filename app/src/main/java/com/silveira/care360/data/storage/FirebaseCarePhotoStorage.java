package com.silveira.care360.data.storage;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.storage.CarePhotoStorage;

import java.io.InputStream;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class FirebaseCarePhotoStorage implements CarePhotoStorage {

    private final FirebaseStorage firebaseStorage;
    private final Context context;

    @Inject
    public FirebaseCarePhotoStorage(FirebaseStorage firebaseStorage,
                                    @ApplicationContext Context context) {
        this.firebaseStorage = firebaseStorage;
        this.context = context;
    }

    @Override
    public void uploadCarePhoto(String groupId, String localPhotoUri, ResultCallback<String> callback) {
        if (isBlank(groupId) || isBlank(localPhotoUri)) {
            callback.onError("No se pudo preparar la foto");
            return;
        }

        Uri uri;
        try {
            uri = Uri.parse(localPhotoUri.trim());
        } catch (Exception e) {
            callback.onError("No se pudo leer la foto seleccionada");
            return;
        }

        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                callback.onError("No se pudo acceder a la foto seleccionada");
                return;
            }
            inputStream.close();
        } catch (Exception e) {
            callback.onError("No se pudo acceder a la foto seleccionada");
            return;
        }

        String extension = resolveExtension(uri);
        StorageReference photoRef = firebaseStorage.getReference()
                .child(String.format(Locale.ROOT,
                        "groups/%s/care-profile/%d.%s",
                        groupId.trim(),
                        System.currentTimeMillis(),
                        extension));

        photoRef.putFile(uri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException() != null ? task.getException() : new IllegalStateException("Upload failed");
                    }
                    return photoRef.getDownloadUrl();
                })
                .addOnSuccessListener(downloadUri -> callback.onSuccess(downloadUri != null ? downloadUri.toString() : null))
                .addOnFailureListener(e -> callback.onError("No se pudo subir la foto"));
    }

    @NonNull
    private String resolveExtension(Uri uri) {
        String type = context.getContentResolver().getType(uri);
        if (type != null) {
            if (type.contains("png")) return "png";
            if (type.contains("webp")) return "webp";
        }
        return "jpg";
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
