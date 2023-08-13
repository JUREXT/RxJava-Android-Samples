package com.morihacky.android.rxjava.rxbus;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.morihacky.android.rxjava.MainActivity;
import com.morihacky.android.rxjava.databinding.FragmentRxbusTopBinding;

public class RxBusDemo_TopFragment extends Fragment {

    private FragmentRxbusTopBinding binding;
    private RxBus _rxBus;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRxbusTopBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        _rxBus = ((MainActivity) requireActivity()).getRxBusSingleton();

        binding.btnDemoRxbusTap.setOnClickListener(view -> onTapButtonClicked());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        _rxBus = null;
    }

    public void onTapButtonClicked() {
        if (_rxBus.hasObservers()) {
            _rxBus.send(new RxBusDemoFragment.TapEvent());
        }
    }
}