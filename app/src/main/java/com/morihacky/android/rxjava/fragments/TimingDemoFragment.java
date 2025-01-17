package com.morihacky.android.rxjava.fragments;

import static android.os.Looper.getMainLooper;
import static android.os.Looper.myLooper;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.morihacky.android.rxjava.databinding.FragmentDemoTimingBinding;
import com.morihacky.android.rxjava.wiring.LogAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.subscribers.DefaultSubscriber;
import io.reactivex.rxjava3.subscribers.DisposableSubscriber;
import timber.log.Timber;

public class TimingDemoFragment extends Fragment {

    private FragmentDemoTimingBinding binding;

    private LogAdapter _adapter;
    private List<String> _logs;

    private DisposableSubscriber<Long> _subscriber1;
    private DisposableSubscriber<Long> _subscriber2;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDemoTimingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        _setupLogger();

        binding.btnDemoTiming1.setOnClickListener(view -> btn1_RunSingleTaskAfter2s());
        binding.btnDemoTiming2.setOnClickListener(view -> btn2_RunTask_IntervalOf1s());
        binding.btnDemoTiming3.setOnClickListener(view -> btn3_RunTask_IntervalOf1s_StartImmediately());
        binding.btnDemoTiming4.setOnClickListener(view -> btn4_RunTask5Times_IntervalOf3s());
        binding.btnDemoTiming5.setOnClickListener(view -> btn5_RunTask5Times_IntervalOf3s());
        binding.btnClr.setOnClickListener(view -> OnClearLog());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        _subscriber1.dispose();
        _subscriber2.dispose();
    }
    // -----------------------------------------------------------------------------------

    public void btn1_RunSingleTaskAfter2s() {
        _log(String.format("A1 [%s] --- BTN click", _getCurrentTimestamp()));

        Flowable.timer(2, TimeUnit.SECONDS)
                .subscribe(
                        new DefaultSubscriber<Long>() {
                            @Override
                            public void onNext(Long number) {
                                _log(String.format("A1 [%s]     NEXT", _getCurrentTimestamp()));
                            }

                            @Override
                            public void onError(Throwable e) {
                                Timber.e(e, "something went wrong in TimingDemoFragment example");
                            }

                            @Override
                            public void onComplete() {
                                _log(String.format("A1 [%s] XXX COMPLETE", _getCurrentTimestamp()));
                            }
                        });
    }

    public void btn2_RunTask_IntervalOf1s() {
        if (_subscriber1 != null && !_subscriber1.isDisposed()) {
            _subscriber1.dispose();
            _log(String.format("B2 [%s] XXX BTN KILLED", _getCurrentTimestamp()));
            return;
        }

        _log(String.format("B2 [%s] --- BTN click", _getCurrentTimestamp()));

        _subscriber1 =
                new DisposableSubscriber<Long>() {
                    @Override
                    public void onComplete() {
                        _log(String.format("B2 [%s] XXXX COMPLETE", _getCurrentTimestamp()));
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e, "something went wrong in TimingDemoFragment example");
                    }

                    @Override
                    public void onNext(Long number) {
                        _log(String.format("B2 [%s]     NEXT", _getCurrentTimestamp()));
                    }
                };

        Flowable.interval(1, TimeUnit.SECONDS).subscribe(_subscriber1);
    }

    public void btn3_RunTask_IntervalOf1s_StartImmediately() {
        if (_subscriber2 != null && !_subscriber2.isDisposed()) {
            _subscriber2.dispose();
            _log(String.format("C3 [%s] XXX BTN KILLED", _getCurrentTimestamp()));
            return;
        }

        _log(String.format("C3 [%s] --- BTN click", _getCurrentTimestamp()));

        _subscriber2 =
                new DisposableSubscriber<Long>() {
                    @Override
                    public void onNext(Long number) {
                        _log(String.format("C3 [%s]     NEXT", _getCurrentTimestamp()));
                    }

                    @Override
                    public void onComplete() {
                        _log(String.format("C3 [%s] XXXX COMPLETE", _getCurrentTimestamp()));
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e, "something went wrong in TimingDemoFragment example");
                    }
                };

        Flowable.interval(0, 1, TimeUnit.SECONDS).subscribe(_subscriber2);
    }

    public void btn4_RunTask5Times_IntervalOf3s() {
        _log(String.format("D4 [%s] --- BTN click", _getCurrentTimestamp()));

        Flowable.interval(3, TimeUnit.SECONDS)
                .take(5)
                .subscribe(
                        new DefaultSubscriber<Long>() {
                            @Override
                            public void onNext(Long number) {
                                _log(String.format("D4 [%s]     NEXT", _getCurrentTimestamp()));
                            }

                            @Override
                            public void onError(Throwable e) {
                                Timber.e(e, "something went wrong in TimingDemoFragment example");
                            }

                            @Override
                            public void onComplete() {
                                _log(String.format("D4 [%s] XXX COMPLETE", _getCurrentTimestamp()));
                            }
                        });
    }

    public void btn5_RunTask5Times_IntervalOf3s() {
        _log(String.format("D5 [%s] --- BTN click", _getCurrentTimestamp()));

        Flowable.just("Do task A right away")
                .doOnNext(input -> _log(String.format("D5 %s [%s]", input, _getCurrentTimestamp())))
                .delay(1, TimeUnit.SECONDS)
                .doOnNext(
                        oldInput ->
                                _log(
                                        String.format(
                                                "D5 %s [%s]", "Doing Task B after a delay", _getCurrentTimestamp())))
                .subscribe(
                        new DefaultSubscriber<String>() {
                            @Override
                            public void onComplete() {
                                _log(String.format("D5 [%s] XXX COMPLETE", _getCurrentTimestamp()));
                            }

                            @Override
                            public void onError(Throwable e) {
                                Timber.e(e, "something went wrong in TimingDemoFragment example");
                            }

                            @Override
                            public void onNext(String number) {
                                _log(String.format("D5 [%s]     NEXT", _getCurrentTimestamp()));
                            }
                        });
    }

    // -----------------------------------------------------------------------------------
    // Method that help wiring up the example (irrelevant to RxJava)

    public void OnClearLog() {
        _logs = new ArrayList<>();
        _adapter.clear();
    }

    private void _setupLogger() {
        _logs = new ArrayList<>();
        _adapter = new LogAdapter(getActivity(), new ArrayList<>());
        binding.listThreadingLog.setAdapter(_adapter);
    }

    private void _log(String logMsg) {
        _logs.add(0, String.format(logMsg + " [MainThread: %b]", getMainLooper() == myLooper()));

        // You can only do below stuff on main thread.
        new Handler(getMainLooper())
                .post(
                        () -> {
                            _adapter.clear();
                            _adapter.addAll(_logs);
                        });
    }

    private String _getCurrentTimestamp() {
        return new SimpleDateFormat("k:m:s:S a", Locale.getDefault()).format(new Date());
    }
}