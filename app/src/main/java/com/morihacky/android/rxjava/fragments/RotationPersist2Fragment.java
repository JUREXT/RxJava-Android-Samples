package com.morihacky.android.rxjava.fragments;

import static android.os.Looper.getMainLooper;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.morihacky.android.rxjava.databinding.FragmentRotationPersistBinding;
import com.morihacky.android.rxjava.wiring.LogAdapter;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subscribers.DisposableSubscriber;
import timber.log.Timber;

public class RotationPersist2Fragment extends Fragment implements RotationPersist2WorkerFragment.IAmYourMaster {

    public static final String TAG = RotationPersist2Fragment.class.toString();

    private FragmentRotationPersistBinding binding;

    private LogAdapter _adapter;
    private List<String> _logs;

    private final CompositeDisposable _disposables = new CompositeDisposable();

    // -----------------------------------------------------------------------------------

    public void startOperationFromWorkerFrag() {
        _logs = new ArrayList<>();
        _adapter.clear();

//        FragmentManager fm = requireActivity().getSupportFragmentManager(); // TODO: Fix
//        RotationPersist2WorkerFragment frag =
//                (RotationPersist2WorkerFragment) fm.findFragmentByTag(RotationPersist2WorkerFragment.TAG);
//
//        if (frag == null) {
//            frag = new RotationPersist2WorkerFragment();
//            fm.beginTransaction().add(frag, RotationPersist2WorkerFragment.TAG).commit();
//        } else {
//            Timber.d("Worker frag already spawned");
//        }
    }

    @Override
    public void setStream(Flowable<Integer> intStream) {
        DisposableSubscriber<Integer> d =
                new DisposableSubscriber<Integer>() {
                    @Override
                    public void onNext(Integer integer) {
                        _log(String.format("Worker frag spits out - %d", integer));
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e, "Error in worker demo frag observable");
                        _log("Dang! something went wrong.");
                    }

                    @Override
                    public void onComplete() {
                        _log("Observable is complete");
                    }
                };

        intStream.doOnSubscribe(subscription -> _log("Subscribing to intsObservable")).subscribe(d);

        _disposables.add(d);
    }

    // -----------------------------------------------------------------------------------
    // Boilerplate
    // -----------------------------------------------------------------------------------

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRotationPersistBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        _setupLogger();
        initListeners();
    }

    private void initListeners() {
        binding.btnRotatePersist.setOnClickListener(view -> startOperationFromWorkerFrag());

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        _disposables.clear();
        binding = null;
    }

    private void _setupLogger() {
        _logs = new ArrayList<>();
        _adapter = new LogAdapter(getActivity(), new ArrayList<>());
        binding.listThreadingLog.setAdapter(_adapter);
    }

    private void _log(String logMsg) {
        _logs.add(0, logMsg);

        // You can only do below stuff on main thread.
        new Handler(getMainLooper())
                .post(() -> {
                            _adapter.clear();
                            _adapter.addAll(_logs);
                        });
    }
}