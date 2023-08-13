package com.morihacky.android.rxjava.rxbus;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;

import com.morihacky.android.rxjava.MainActivity;
import com.morihacky.android.rxjava.databinding.FragmentRxbusBottomBinding;

import io.reactivex.rxjava3.disposables.CompositeDisposable;


public class RxBusDemo_Bottom1Fragment extends Fragment {

    private FragmentRxbusBottomBinding binding;
    TextView _tapEventTxtShow;

    private CompositeDisposable _disposables;
    private RxBus _rxBus;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRxbusBottomBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        _rxBus = ((MainActivity) requireActivity()).getRxBusSingleton();
    }

    @Override
    public void onStart() {
        super.onStart();
        _disposables = new CompositeDisposable();

        _disposables.add(_rxBus
                .asFlowable()
                .subscribe(event -> {
                    if (event instanceof RxBusDemoFragment.TapEvent) {
                        _showTapText();
                    }
                }));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        _disposables.clear();
        binding = null;
    }

    private void _showTapText() {
        binding.demoRxbusTapTxt.setVisibility(View.VISIBLE);
        binding.demoRxbusTapTxt.setAlpha(1f);
        ViewCompat.animate(_tapEventTxtShow).alphaBy(-1f).setDuration(400);
    }
}
