package com.morihacky.android.rxjava.rxbus;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;

import com.morihacky.android.rxjava.MainActivity;
import com.morihacky.android.rxjava.databinding.FragmentRxbusBottomBinding;

import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.flowables.ConnectableFlowable;

public class RxBusDemo_Bottom3Fragment extends Fragment {

    private FragmentRxbusBottomBinding binding;

    private RxBus _rxBus;
    private CompositeDisposable _disposables;

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

        ConnectableFlowable<Object> tapEventEmitter = _rxBus.asFlowable().publish();

        _disposables.add(tapEventEmitter
                .subscribe(event -> {
                    if (event instanceof RxBusDemoFragment.TapEvent) {
                        _showTapText();
                    }
                }));

        _disposables.add(tapEventEmitter
                .publish(stream -> stream.buffer(stream.debounce(1, TimeUnit.SECONDS)))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        taps -> {
                            _showTapCount(taps.size());
                        }));
        _disposables.add(tapEventEmitter.connect());
    }

    @Override
    public void onStop() {
        super.onStop();
        _disposables.clear();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        _disposables.clear();
        binding = null;
    }

    // -----------------------------------------------------------------------------------
    // Helper to show the text via an animation

    private void _showTapText() {
        binding.demoRxbusTapTxt.setVisibility(View.VISIBLE);
        binding.demoRxbusTapTxt.setAlpha(1f);
        ViewCompat.animate(binding.demoRxbusTapTxt).alphaBy(-1f).setDuration(400);
    }

    private void _showTapCount(int size) {
        binding.demoRxbusTapCount.setText(String.valueOf(size));
        binding.demoRxbusTapCount.setVisibility(View.VISIBLE);
        binding.demoRxbusTapCount.setScaleX(1f);
        binding.demoRxbusTapCount.setScaleY(1f);
        ViewCompat.animate(binding.demoRxbusTapCount)
                .scaleXBy(-1f)
                .scaleYBy(-1f)
                .setDuration(800)
                .setStartDelay(100);
    }
}
