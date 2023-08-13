package com.morihacky.android.rxjava.rxbus;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.morihacky.android.rxjava.R;
import com.morihacky.android.rxjava.databinding.FragmentRxbusDemoBinding;

public class RxBusDemoFragment extends Fragment {

    private FragmentRxbusDemoBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRxbusDemoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.demo_rxbus_frag_1, new RxBusDemo_TopFragment())
                .replace(R.id.demo_rxbus_frag_2, new RxBusDemo_Bottom3Fragment())
                //.replace(R.id.demo_rxbus_frag_2, new RxBusDemo_Bottom2Fragment())
                //.replace(R.id.demo_rxbus_frag_2, new RxBusDemo_Bottom1Fragment())
                .commit();
    }

    public static class TapEvent {
    }
}