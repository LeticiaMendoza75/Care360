package com.silveira.care360.di;

import android.content.Context;

import androidx.room.Room;

import com.silveira.care360.data.local.AppDatabase;
import com.silveira.care360.data.local.dao.GroupDao;
import com.silveira.care360.data.local.dao.UserDao;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class DatabaseModule {

    @Provides
    @Singleton
    public AppDatabase provideDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(context, AppDatabase.class, "care360_db")
                .fallbackToDestructiveMigration()
                .build();
    }

    @Provides
    public UserDao provideUserDao(AppDatabase database) {
        return database.userDao();
    }

    @Provides
    public GroupDao provideGroupDao(AppDatabase database) {
        return database.groupDao();
    }
}
