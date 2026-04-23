package com.silveira.care360.di;

import android.content.Context;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.silveira.care360.data.remote.auth.FirebaseAuthDataSource;
import com.silveira.care360.data.remote.auth.GoogleSignInDataSource;
import com.silveira.care360.data.remote.firestore.FirebaseUserDataSource;
import com.silveira.care360.data.remote.firestore.FirebaseGroupDataSource;
import com.silveira.care360.data.remote.firestore.FirebasePatologiaDataSource;
import com.silveira.care360.data.remote.firestore.FirebaseSeguimientoDataSource;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class DataSourceModule {

    @Provides
    @Singleton
    public FirebaseAuthDataSource provideFirebaseAuthDataSource(FirebaseAuth firebaseAuth) {
        return new FirebaseAuthDataSource(firebaseAuth);
    }

    @Provides
    @Singleton
    public FirebaseUserDataSource provideFirebaseUserDataSource(FirebaseFirestore firestore) {
        return new FirebaseUserDataSource(firestore);
    }

    @Provides
    @Singleton
    public FirebaseGroupDataSource provideFirebaseGroupDataSource(FirebaseFirestore firestore) {
        return new FirebaseGroupDataSource(firestore);
    }

    @Provides
    @Singleton
    public FirebasePatologiaDataSource provideFirebasePatologiaDataSource(FirebaseFirestore firestore) {
        return new FirebasePatologiaDataSource(firestore);
    }

    @Provides
    @Singleton
    public FirebaseSeguimientoDataSource provideFirebaseSeguimientoDataSource(FirebaseFirestore firestore) {
        return new FirebaseSeguimientoDataSource(firestore);
    }

    @Provides
    @Singleton
    public GoogleSignInDataSource provideGoogleSignInDataSource(@ApplicationContext Context context) {
        return new GoogleSignInDataSource(context);
    }
}
