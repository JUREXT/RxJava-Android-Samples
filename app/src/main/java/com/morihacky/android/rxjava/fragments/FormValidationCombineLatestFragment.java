package com.morihacky.android.rxjava.fragments;

import static android.text.TextUtils.isEmpty;
import static android.util.Patterns.EMAIL_ADDRESS;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.jakewharton.rxbinding2.widget.RxTextView;
import com.morihacky.android.rxjava.R;
import com.morihacky.android.rxjava.databinding.FragmentFormValidationCombLatestBinding;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.subscribers.DisposableSubscriber;
import timber.log.Timber;

public class FormValidationCombineLatestFragment extends Fragment {

    private FragmentFormValidationCombLatestBinding binding;

    private DisposableSubscriber<Boolean> _disposableObserver = null;
    private Flowable<CharSequence> _emailChangeObservable;
    private Flowable<CharSequence> _numberChangeObservable;
    private Flowable<CharSequence> _passwordChangeObservable;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFormValidationCombLatestBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        _emailChangeObservable = RxTextView.textChanges(binding.demoComblEmail).skip(1).toFlowable(BackpressureStrategy.LATEST);
        _passwordChangeObservable = RxTextView.textChanges(binding.demoComblPassword).skip(1).toFlowable(BackpressureStrategy.LATEST);
        _numberChangeObservable = RxTextView.textChanges(binding.demoComblNum).skip(1).toFlowable(BackpressureStrategy.LATEST);
        _combineLatestEvents();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        _disposableObserver.dispose();
    }

    private void _combineLatestEvents() {

        _disposableObserver =
                new DisposableSubscriber<Boolean>() {
                    @Override
                    public void onNext(Boolean formValid) {
                        if (formValid) {
                            binding.btnDemoFormValid.setBackgroundColor(
                                    ContextCompat.getColor(requireContext(), R.color.blue));
                        } else {
                            binding.btnDemoFormValid.setBackgroundColor(
                                    ContextCompat.getColor(requireContext(), R.color.gray));
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e, "there was an error");
                    }

                    @Override
                    public void onComplete() {
                        Timber.d("completed");
                    }
                };

        Flowable.combineLatest(
                        _emailChangeObservable,
                        _passwordChangeObservable,
                        _numberChangeObservable,
                        (newEmail, newPassword, newNumber) -> {
                            boolean emailValid = !isEmpty(newEmail) && EMAIL_ADDRESS.matcher(newEmail).matches();
                            if (!emailValid) {
                                binding.demoComblEmail.setError("Invalid Email!");
                            }

                            boolean passValid = !isEmpty(newPassword) && newPassword.length() > 8;
                            if (!passValid) {
                                binding.demoComblPassword.setError("Invalid Password!");
                            }

                            boolean numValid = !isEmpty(newNumber);
                            if (numValid) {
                                int num = Integer.parseInt(newNumber.toString());
                                numValid = num > 0 && num <= 100;
                            }
                            if (!numValid) {
                                binding.demoComblNum.setError("Invalid Number!");
                            }
                            return emailValid && passValid && numValid;
                        })
                .subscribe(_disposableObserver);
    }
}