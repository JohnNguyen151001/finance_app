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
 * ViewModel tạo ví mới — chỉ lưu SQLite (Room).
 */
public class AddWalletViewModel extends AndroidViewModel {

    private final WalletRepository walletRepository;
    private final MutableLiveData<Boolean> saveSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public AddWalletViewModel(@NonNull Application application) {
        super(application);
        walletRepository = new WalletRepository(application);
    }

    public LiveData<Boolean> getSaveSuccess() { return saveSuccess; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public void saveWallet(String name, String type, double balance) {
        if (name.isEmpty()) {
            errorMessage.setValue(((FinanceApp) getApplication())
                    .getString(com.financeapp.mobile.R.string.wallet_name_required));
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "";

        WalletEntity wallet = new WalletEntity();
        wallet.userId = userId.isEmpty() ? null : userId;
        wallet.name = name;
        wallet.type = type;
        wallet.balance = balance;
        wallet.createdAt = System.currentTimeMillis();
        wallet.iconKey = defaultIconForType(type);
        wallet.currency = "VND";
        wallet.isDeleted = 0;

        ((FinanceApp) getApplication()).databaseIo().execute(() -> {
            walletRepository.insert(wallet);
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
