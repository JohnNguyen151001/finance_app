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
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class WalletViewModel extends AndroidViewModel {

    private final WalletRepository walletRepository;
    private final MutableLiveData<List<WalletEntity>> wallets = new MutableLiveData<>();

    public WalletViewModel(@NonNull Application application) {
        super(application);
        walletRepository = new WalletRepository(application);
        refresh();
    }

    public LiveData<List<WalletEntity>> getWallets() {
        return wallets;
    }

    private static String uidOrEmpty() {
        FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
        return u != null ? u.getUid() : "";
    }

    public void refresh() {
        ((FinanceApp) getApplication()).databaseIo().execute(() ->
                wallets.postValue(walletRepository.getWallets(uidOrEmpty())));
    }
}
