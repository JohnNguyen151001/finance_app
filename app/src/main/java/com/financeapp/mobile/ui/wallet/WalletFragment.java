package com.financeapp.mobile.ui.wallet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.financeapp.mobile.R;
import com.financeapp.mobile.databinding.FragmentWalletBinding;

public class WalletFragment extends Fragment {

    private FragmentWalletBinding binding;
    private WalletViewModel viewModel;
    private final WalletAdapter adapter = new WalletAdapter();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentWalletBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(WalletViewModel.class);

        binding.toolbarWallet.setNavigationOnClickListener(v ->
                Navigation.findNavController(v).navigateUp());

        binding.rvWallets.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvWallets.setAdapter(adapter);

        binding.fabAddWallet.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.nav_add_wallet));

        viewModel.getWallets().observe(getViewLifecycleOwner(), adapter::submit);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.refresh();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
