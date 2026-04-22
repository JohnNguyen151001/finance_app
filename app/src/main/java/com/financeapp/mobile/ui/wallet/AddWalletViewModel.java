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

/**
 * ViewModel thêm/sửa ví.
 */
public class AddWalletViewModel extends AndroidViewModel {

    private final WalletRepository walletRepository;
    private final MutableLiveData<Boolean> saveSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<WalletEntity> existingWallet = new MutableLiveData<>();

    public AddWalletViewModel(@NonNull Application application) {
        super(application);
        walletRepository = new WalletRepository(application);
    }

    public LiveData<Boolean> getSaveSuccess() { return saveSuccess; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<WalletEntity> getExistingWallet() { return existingWallet; }

    public void loadWallet(long id) {
        if (id <= 0) return;
        ((FinanceApp) getApplication()).databaseIo().execute(() -> {
            existingWallet.postValue(walletRepository.getById(id));
        });
    }

    public void saveWallet(long id, String name, String type, double balance) {
        if (name.isEmpty()) {
            errorMessage.setValue("Vui lòng nhập tên ví");
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "";

        ((FinanceApp) getApplication()).databaseIo().execute(() -> {
            WalletEntity wallet;
            if (id > 0) {
                wallet = walletRepository.getById(id);
                if (wallet == null) {
                    saveSuccess.postValue(false);
                    return;
                }
            } else {
                wallet = new WalletEntity();
                wallet.userId = userId.isEmpty() ? null : userId;
                wallet.createdAt = System.currentTimeMillis();
                wallet.currency = "VND";
                wallet.isDeleted = 0;
            }

            wallet.name = name;
            wallet.type = type;
            wallet.balance = balance;
            wallet.iconUrl = defaultIconForType(type);

            if (id > 0) {
                walletRepository.update(wallet);
            } else {
                walletRepository.insert(wallet);
            }
            saveSuccess.postValue(true);
        });
    }

    private String defaultIconForType(String type) {
        if (type == null) return "👛";
        switch (type) {
            case "BANK":   return "🏦";
            case "CREDIT": return "💳";
            case "GOAL":   return "🎯";
            default:       return "👛";
        }
    }
}
