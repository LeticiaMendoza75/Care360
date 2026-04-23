package com.silveira.care360.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.silveira.care360.data.local.entity.GroupEntity;

import java.util.List;

@Dao
public interface GroupDao {
    @Query("SELECT * FROM groups WHERE id = :groupId")
    GroupEntity getGroupById(String groupId);

    @Query("SELECT * FROM groups")
    List<GroupEntity> getAllGroups();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertGroup(GroupEntity group);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertGroups(List<GroupEntity> groups);

    @Query("DELETE FROM groups")
    void deleteAll();
}
