package com.silveira.care360.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.silveira.care360.data.local.entity.UserEntity;

@Dao
public interface UserDao {
    @Query("SELECT * FROM users WHERE id = :userId")
    UserEntity getUserById(String userId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUser(UserEntity user);

    @Query("DELETE FROM users")
    void deleteAll();
}
