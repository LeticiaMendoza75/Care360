package com.silveira.care360.data.local;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.silveira.care360.data.local.dao.UserDao;
import com.silveira.care360.data.local.dao.GroupDao;
import com.silveira.care360.data.local.entity.UserEntity;
import com.silveira.care360.data.local.entity.GroupEntity;
import com.silveira.care360.data.local.entity.GroupMemberEntity;

@Database(entities = {UserEntity.class, GroupEntity.class, GroupMemberEntity.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract GroupDao groupDao();
}
