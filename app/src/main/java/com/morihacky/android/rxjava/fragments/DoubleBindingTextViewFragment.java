package com.morihacky.android.rxjava.fragments;

import static android.text.TextUtils.isEmpty;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.morihacky.android.rxjava.databinding.FragmentDoubleBindingTextviewBinding;

import io.reactivex.disposables.Disposable;
import io.reactivex.processors.PublishProcessor;

public class DoubleBindingTextViewFragment extends Fragment {

    private FragmentDoubleBindingTextviewBinding binding;

    Disposable _disposable;
    PublishProcessor<Float> _resultEmitterSubject;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDoubleBindingTextviewBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        _resultEmitterSubject = PublishProcessor.create();

        _disposable = _resultEmitterSubject.subscribe(aFloat -> {
            binding.doubleBindingResult.setText(String.valueOf(aFloat));
        });

        onNumberChanged();
        binding.doubleBindingNum2.requestFocus();

        initListeners();
    }

    private void initListeners() {
        binding.doubleBindingNum1.setOnClickListener(view -> onNumberChanged());
        binding.doubleBindingNum2.setOnClickListener(view -> onNumberChanged());
    }

    public void onNumberChanged() {
        float num1 = 0;
        float num2 = 0;
        if (!isEmpty(binding.doubleBindingNum1.getText().toString())) {
            num1 = Float.parseFloat(binding.doubleBindingNum1.getText().toString());
        }
        if (!isEmpty(binding.doubleBindingNum2.getText().toString())) {
            num2 = Float.parseFloat(binding.doubleBindingNum2.getText().toString());
        }
        _resultEmitterSubject.onNext(num1 + num2);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        _disposable.dispose();
    }
}