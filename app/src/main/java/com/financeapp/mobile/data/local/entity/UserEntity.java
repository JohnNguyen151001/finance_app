package com.financeapp.mobile.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class UserEntity {

    @PrimaryKey
    @NonNull
    public String uuid; // mapped to user_id in ERD (Firebase UID)

    public String email;
    public String password_hash;
    public String display_name;
    public long created_at;
}
