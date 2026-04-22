package com.financeapp.mobile.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "challenges",
        indices = {
                @Index("active"),
                @Index("userId")
        })
public class ChallengeEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public String userId;

    /** {@link com.financeapp.mobile.domain.ChallengeEngine.Type#name()} */
    @NonNull
    public String type = "";

    /** WEEKLY_LIMIT: max chi 7 ngày (VND). Các loại khác: 0. */
    public double targetValue;

    /** NO_SPEND_DAY: kỷ lục streak đã lưu. */
    public int bestStreak;

    public boolean active;

    public long createdAt;
}
