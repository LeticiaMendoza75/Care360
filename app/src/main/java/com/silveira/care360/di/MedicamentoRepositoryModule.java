package com.silveira.care360.di;

import com.silveira.care360.data.repository.MedicamentoRepositoryImpl;
import com.silveira.care360.domain.repository.MedicamentoRepository;

import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public abstract class MedicamentoRepositoryModule {

    @Binds
    public abstract MedicamentoRepository bindMedicamentoRepository(
            MedicamentoRepositoryImpl impl
    );
}
