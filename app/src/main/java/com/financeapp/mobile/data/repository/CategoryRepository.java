package com.financeapp.mobile.data.repository;

import android.app.Application;

import com.financeapp.mobile.data.local.AppDatabase;
import com.financeapp.mobile.data.local.dao.CategoryDao;
import com.financeapp.mobile.data.local.entity.CategoryEntity;

import java.util.List;

public class CategoryRepository {

    private final CategoryDao categoryDao;

    public CategoryRepository(Application application) {
        categoryDao = AppDatabase.getInstance(application).categoryDao();
    }

    public List<CategoryEntity> getByKind(String uid, String kind) {
        return categoryDao.getByKindForUser(uid, kind);
    }

    public List<CategoryEntity> getAll(String uid) {
        return categoryDao.getAllForUser(uid);
    }

    public CategoryEntity getById(long id) {
        return categoryDao.getById(id);
    }
}
