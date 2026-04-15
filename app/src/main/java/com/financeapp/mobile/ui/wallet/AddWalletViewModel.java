package com.financeapp.mobile.ui.wallet;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.financeapp.mobile.FinanceApp;
import com.financeapp.mobile.data.local.entity.WalletEntity;
import com.financeapp.mobile.data.repository.WalletRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.UUID;

public class AddWalletViewModel extends AndroidViewModel {

    private final WalletRepository walletRepository;
    private final MutableLiveData<Boolean> saveSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public AddWalletViewModel(@NonNull Application application) {
        super(application);
        walletRepository = new WalletRepository(application);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public LiveData<Boolean> getSaveSuccess() {
        return saveSuccess;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void saveWallet(String name, String type, double balance) {
        if (name.isEmpty()) {
            errorMessage.setValue("Name cannot be empty");
            return;
        }

        if (auth.getCurrentUser() == null) {
            errorMessage.setValue("User not logged in");
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        WalletEntity wallet = new WalletEntity();
        wallet.id = UUID.randomUUID().toString();
        wallet.userId = userId;
        wallet.name = name;
        wallet.type = type;
        wallet.balance = balance;
        wallet.createdAt = System.currentTimeMillis();
        wallet.iconKey = "👛"; // Default icon

        // 1. Save to Firestore
        db.collection("users").document(userId).collection("wallets").document(wallet.id)
                .set(wallet)
                .addOnSuccessListener(aVoid -> {
                    // 2. Save to local Room for offline access
                    ((FinanceApp) getApplication()).databaseIo().execute(() -> {
                        walletRepository.insert(wallet);
                        saveSuccess.postValue(true);
                    });
                })
                .addOnFailureListener(e -> {
                    errorMessage.setValue("Failed to save to cloud: " + e.getMessage());
                });
    }
}
