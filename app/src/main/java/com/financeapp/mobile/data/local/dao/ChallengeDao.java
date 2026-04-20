package com.financeapp.mobile.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.financeapp.mobile.data.local.entity.ChallengeEntity;

import java.util.List;

@Dao
public interface ChallengeDao {

    @Query("SELECT * FROM challenges WHERE userId = :uid AND active = 1 ORDER BY createdAt DESC")
    List<ChallengeEntity> getActiveForUser(String uid);

    @Query("SELECT COUNT(*) FROM challenges WHERE userId = :uid AND active = 1")
    int countActiveForUser(String uid);

    @Insert
    long insert(ChallengeEntity entity);

    @Update
    void update(ChallengeEntity entity);

    @Query("DELETE FROM challenges WHERE id = :id")
    void deleteById(long id);
}
