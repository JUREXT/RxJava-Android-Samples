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

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class RxBusDemo_Bottom2Fragment extends Fragment {

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

        Flowable<Object> tapEventEmitter = _rxBus.asFlowable().share();

        _disposables.add(
                tapEventEmitter.subscribe(
                        event -> {
                            if (event instanceof RxBusDemoFragment.TapEvent) {
                                _showTapText();
                            }
                        }));

        Flowable<Object> debouncedEmitter = tapEventEmitter.debounce(1, TimeUnit.SECONDS);
        Flowable<List<Object>> debouncedBufferEmitter = tapEventEmitter.buffer(debouncedEmitter);

        _disposables.add(
                debouncedBufferEmitter
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(taps -> {
                            _showTapCount(taps.size());
                        }));
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
        binding.demoRxbusTapCount.setVisibility(View.VISIBLE);
        binding.demoRxbusTapCount.setAlpha(1f);
        ViewCompat.animate(binding.demoRxbusTapCount).alphaBy(-1f).setDuration(400);
    }

    private void _showTapCount(int size) {
        binding.demoRxbusTapTxt.setText(String.valueOf(size));
        binding.demoRxbusTapTxt.setVisibility(View.VISIBLE);
        binding.demoRxbusTapTxt.setScaleX(1f);
        binding.demoRxbusTapTxt.setScaleY(1f);
        ViewCompat.animate(binding.demoRxbusTapTxt)
                .scaleXBy(-1f)
                .scaleYBy(-1f)
                .setDuration(800)
                .setStartDelay(100);
    }
}
