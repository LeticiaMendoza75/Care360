package com.silveira.care360.di;

import com.silveira.care360.core.auth.GoogleSignInGateway;
import com.silveira.care360.data.report.AndroidCitasPdfExporter;
import com.silveira.care360.data.report.AndroidIncidenciasPdfExporter;
import com.silveira.care360.core.reminder.MedicationReminderScheduler;
import com.silveira.care360.data.report.AndroidMedicacionPdfExporter;
import com.silveira.care360.data.reminder.MedicationReminderSchedulerImpl;
import com.silveira.care360.data.repository.AuthRepositoryImpl;
import com.silveira.care360.data.repository.DocumentoRepositoryImpl;
import com.silveira.care360.data.repository.GoogleSignInGatewayImpl;
import com.silveira.care360.data.repository.PatologiaRepositoryImpl;
import com.silveira.care360.data.repository.SeguimientoRepositoryImpl;
import com.silveira.care360.data.repository.UserRepositoryImpl;
import com.silveira.care360.data.storage.FirebaseCarePhotoStorage;
import com.silveira.care360.data.storage.FirebaseDocumentFileStorage;
import com.silveira.care360.domain.report.CitasPdfExporter;
import com.silveira.care360.domain.report.IncidenciasPdfExporter;
import com.silveira.care360.domain.report.MedicacionPdfExporter;
import com.silveira.care360.domain.repository.AuthRepository;
import com.silveira.care360.domain.repository.DocumentoRepository;
import com.silveira.care360.domain.repository.PatologiaRepository;
import com.silveira.care360.domain.repository.SeguimientoRepository;
import com.silveira.care360.domain.repository.UserRepository;
import com.silveira.care360.data.repository.GroupRepositoryImpl;
import com.silveira.care360.domain.repository.GroupRepository;
import com.silveira.care360.domain.storage.CarePhotoStorage;
import com.silveira.care360.domain.storage.DocumentFileStorage;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public abstract class RepositoryModule {

    @Binds
    @Singleton
    public abstract AuthRepository bindAuthRepository(AuthRepositoryImpl impl);

    @Binds
    @Singleton
    public abstract GoogleSignInGateway bindGoogleSignInGateway(GoogleSignInGatewayImpl impl);

    @Binds
    @Singleton
    public abstract UserRepository bindUserRepository(UserRepositoryImpl impl);
    @Binds
    @Singleton
    public abstract GroupRepository bindGroupRepository(GroupRepositoryImpl impl);

    @Binds
    @Singleton
    public abstract DocumentoRepository bindDocumentoRepository(DocumentoRepositoryImpl impl);

    @Binds
    @Singleton
    public abstract PatologiaRepository bindPatologiaRepository(PatologiaRepositoryImpl impl);

    @Binds
    @Singleton
    public abstract SeguimientoRepository bindSeguimientoRepository(SeguimientoRepositoryImpl impl);

    @Binds
    @Singleton
    public abstract MedicationReminderScheduler bindMedicationReminderScheduler(
            MedicationReminderSchedulerImpl impl
    );

    @Binds
    @Singleton
    public abstract MedicacionPdfExporter bindMedicacionPdfExporter(
            AndroidMedicacionPdfExporter impl
    );

    @Binds
    @Singleton
    public abstract CitasPdfExporter bindCitasPdfExporter(
            AndroidCitasPdfExporter impl
    );

    @Binds
    @Singleton
    public abstract IncidenciasPdfExporter bindIncidenciasPdfExporter(
            AndroidIncidenciasPdfExporter impl
    );

    @Binds
    @Singleton
    public abstract CarePhotoStorage bindCarePhotoStorage(
            FirebaseCarePhotoStorage impl
    );

    @Binds
    @Singleton
    public abstract DocumentFileStorage bindDocumentFileStorage(
            FirebaseDocumentFileStorage impl
    );
}
