package com.silveira.care360.di;

import android.content.Context;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.silveira.care360.R;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class NetworkModule {

    @Provides
    @Singleton
    public FirebaseAuth provideFirebaseAuth() {
        return FirebaseAuth.getInstance();
    }

    @Provides
    @Singleton
    public FirebaseFirestore provideFirestore() {
        return FirebaseFirestore.getInstance();
    }

    @Provides
    @Singleton
    public FirebaseStorage provideFirebaseStorage(@ApplicationContext Context context) {
        String bucket = null;
        try {
            bucket = context.getString(R.string.google_storage_bucket);
        } catch (Exception ignored) {
        }

        if (bucket != null) {
            bucket = bucket.trim();
            if (!bucket.isEmpty()) {
                if (!bucket.startsWith("gs://")) {
                    bucket = "gs://" + bucket;
                }
                return FirebaseStorage.getInstance(bucket);
            }
        }

        return FirebaseStorage.getInstance();
    }
}
