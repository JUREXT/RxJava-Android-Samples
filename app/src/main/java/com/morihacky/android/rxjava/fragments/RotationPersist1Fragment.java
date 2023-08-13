package com.morihacky.android.rxjava.fragments;

import static android.os.Looper.getMainLooper;

import static com.morihacky.android.rxjava.fragments.RotationPersist1WorkerFragment.*;

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

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.subscribers.DisposableSubscriber;
import timber.log.Timber;

public class RotationPersist1Fragment extends Fragment implements IAmYourMaster {

    public static final String TAG = RotationPersist1Fragment.class.toString();

    private FragmentRotationPersistBinding binding;

    private LogAdapter _adapter;
    private List<String> _logs;

    private final CompositeDisposable _disposables = new CompositeDisposable();

    // -----------------------------------------------------------------------------------

    public void startOperationFromWorkerFrag() {
        _logs = new ArrayList<>();
        _adapter.clear();

       // FragmentManager fm = requireActivity().getSupportFragmentManager(); // TODO: fix
       // RotationPersist1WorkerFragment frag = new RotationPersist1WorkerFragment();
      //  fm.beginTransaction().add(frag, RotationPersist1WorkerFragment.TAG).commit();
    }

    @Override
    public void observeResults(Flowable<Integer> intsFlowable) {
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

        intsFlowable.doOnSubscribe(subscription -> {
            _log("Subscribing to intsObservable");
        }).subscribe(d);

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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        _setupLogger();
        initListeners();
    }

    private void initListeners() {
        binding.btnRotatePersist.setOnClickListener(view -> startOperationFromWorkerFrag());
    }

    @Override
    public void onPause() {
        super.onPause();
        _disposables.clear();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        _adapter.clear();
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