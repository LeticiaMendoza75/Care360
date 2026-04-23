package com.silveira.care360.di;

import com.silveira.care360.data.repository.CitaRepositoryImpl;
import com.silveira.care360.domain.repository.CitaRepository;

import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public abstract class CitaRepositoryModule {

    @Binds
    public abstract CitaRepository bindCitaRepository(CitaRepositoryImpl impl);
}
