package com.morihacky.android.rxjava.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.morihacky.android.rxjava.R;
import com.morihacky.android.rxjava.databinding.FragmentPollingBinding;

import org.reactivestreams.Publisher;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Function;
import timber.log.Timber;

public class PollingFragment extends Fragment {

    private FragmentPollingBinding binding;

    private static final int INITIAL_DELAY = 0;
    private static final int POLLING_INTERVAL = 1000;
    private static final int POLL_COUNT = 8;

    private LogAdapter _adapter;
    private int _counter = 0;
    private CompositeDisposable _disposables;
    private List<String> _logs;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _disposables = new CompositeDisposable();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPollingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        _setupLogger();
        initListeners();
    }

    private void initListeners() {
        binding.btnStartSimplePolling.setOnClickListener(view -> onStartSimplePollingClicked());
        binding.btnStartIncreasinglyDelayedPolling.setOnClickListener(view -> onStartIncreasinglyDelayedPolling());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        _disposables.clear();
        binding = null;
    }

    public void onStartSimplePollingClicked() {
        Disposable d = Flowable.interval(INITIAL_DELAY, POLLING_INTERVAL, TimeUnit.MILLISECONDS)
                .map(this::_doNetworkCallAndGetStringResult)
                .take(POLL_COUNT)
                .doOnSubscribe(subscription -> {
                    _log(String.format("Start simple polling - %s", _counter));
                }).subscribe(taskName -> {
                    _log(
                            String.format(
                                    Locale.US,
                                    "Executing polled task [%s] now time : [xx:%02d]",
                                    taskName,
                                    _getSecondHand()));
                });
        _disposables.add(d);
    }


    public void onStartIncreasinglyDelayedPolling() {
        _setupLogger();
      _log(String.format(Locale.US, "Start increasingly delayed polling now time: [xx:%02d]", _getSecondHand()));

        _disposables.add(
                Flowable.just(1L)
                        .repeatWhen(new RepeatWithDelay(POLL_COUNT, POLLING_INTERVAL))
                        .subscribe(
                                o ->
                                        _log(
                                                String.format(
                                                        Locale.US,
                                                        "Executing polled task now time : [xx:%02d]",
                                                        _getSecondHand())),
                                e -> Timber.d(e, "arrrr. Error")));
    }

    // -----------------------------------------------------------------------------------

    // CAUTION:
    // --------------------------------------
    // THIS notificationHandler class HAS NO BUSINESS BEING non-static
    // I ONLY did this cause i wanted access to the `_log` method from inside here
    // for the purpose of demonstration. In the real world, make it static and LET IT BE!!

    // It's 12am in the morning and i feel lazy dammit !!!

    private String _doNetworkCallAndGetStringResult(long attempt) {
        try {
            if (attempt == 4) {
                // randomly make one event super long so we test that the repeat logic waits
                // and accounts for this.
                Thread.sleep(9000);
            } else {
                Thread.sleep(3000);
            }

        } catch (InterruptedException e) {
            Timber.d("Operation was interrupted");
        }
        _counter++;

        return String.valueOf(_counter);
    }

    // -----------------------------------------------------------------------------------
    // Method that help wiring up the example (irrelevant to RxJava)

    private int _getSecondHand() {
        long millis = System.currentTimeMillis();
        return (int)
                (TimeUnit.MILLISECONDS.toSeconds(millis)
                        - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }

    private void _log(String logMsg) {
        if (_isCurrentlyOnMainThread()) {
            _logs.add(0, logMsg + " (main thread) ");
            _adapter.clear();
            _adapter.addAll(_logs);
        } else {
            _logs.add(0, logMsg + " (NOT main thread) ");

            // You can only do below stuff on main thread.
            new Handler(Looper.getMainLooper())
                    .post(
                            () -> {
                                _adapter.clear();
                                _adapter.addAll(_logs);
                            });
        }
    }

    private void _setupLogger() {
        _logs = new ArrayList<>();
        _adapter = new LogAdapter(getActivity(), new ArrayList<>());
        binding.listThreadingLog.setAdapter(_adapter);
        _counter = 0;
    }

    private boolean _isCurrentlyOnMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    //public static class RepeatWithDelay
    public class RepeatWithDelay implements Function<Flowable<Object>, Publisher<Long>> {

        private final int _repeatLimit;
        private final int _pollingInterval;
        private int _repeatCount = 1;

        RepeatWithDelay(int repeatLimit, int pollingInterval) {
            _pollingInterval = pollingInterval;
            _repeatLimit = repeatLimit;
        }

        // this is a notificationhandler, all we care about is
        // the emission "type" not emission "content"
        // only onNext triggers a re-subscription

        @Override
        public Publisher<Long> apply(Flowable<Object> inputFlowable) throws Exception {
            // it is critical to use inputObservable in the chain for the result
            // ignoring it and doing your own thing will break the sequence

            return inputFlowable.flatMap(
                    new Function<Object, Publisher<Long>>() {
                        @Override
                        public Publisher<Long> apply(Object o) throws Exception {
                            if (_repeatCount >= _repeatLimit) {
                                // terminate the sequence cause we reached the limit
                                _log("Completing sequence");
                                return Flowable.empty();
                            }

                            // since we don't get an input
                            // we store state in this handler to tell us the point of time we're firing
                            _repeatCount++;

                            return Flowable.timer(_repeatCount * _pollingInterval, TimeUnit.MILLISECONDS);
                        }
                    });
        }
    }

    private static class LogAdapter extends ArrayAdapter<String> {
        public LogAdapter(Context context, List<String> logs) {
            super(context, R.layout.item_log, R.id.item_log, logs);
        }
    }
}