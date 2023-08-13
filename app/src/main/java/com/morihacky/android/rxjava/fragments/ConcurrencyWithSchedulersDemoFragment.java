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
import com.morihacky.android.rxjava.databinding.FragmentConcurrencySchedulersBinding;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.observers.DisposableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;

import java.util.ArrayList;
import java.util.List;

public class ConcurrencyWithSchedulersDemoFragment extends Fragment {

  private FragmentConcurrencySchedulersBinding binding;

  private LogAdapter _adapter;
  private List<String> _logs;
  private final CompositeDisposable compositeDisposable = new CompositeDisposable();

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    binding = FragmentConcurrencySchedulersBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    initListeners();
  }

  private void initListeners() {
    binding.btnStartOperation.setOnClickListener(view -> startLongOperation());
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    compositeDisposable.clear();
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    _setupLogger();
  }


  public void startLongOperation() {
    binding.progressOperationRunning.setVisibility(View.VISIBLE);
    _log("Button Clicked");

    DisposableObserver<Boolean> d = _getDisposableObserver();

    _getObservable()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(d);

    compositeDisposable.add(d);
  }

  private Observable<Boolean> _getObservable() {
    return Observable.just(true)
        .map(
            aBoolean -> {
              _log("Within Observable");
              _doSomeLongOperation_thatBlocksCurrentThread();
              return aBoolean;
            });
  }

  /**
   * Observer that handles the result through the 3 important actions:
   *
   * <p>1. onCompleted 2. onError 3. onNext
   */
  private DisposableObserver<Boolean> _getDisposableObserver() {
    return new DisposableObserver<Boolean>() {

      @Override
      public void onComplete() {
        _log("On complete");
        binding.progressOperationRunning.setVisibility(View.INVISIBLE);
      }

      @Override
      public void onError(Throwable e) {
        Timber.e(e, "Error in RxJava Demo concurrency");
        _log(String.format("Boo! Error %s", e.getMessage()));
        binding.progressOperationRunning.setVisibility(View.INVISIBLE);
      }

      @Override
      public void onNext(Boolean bool) {
        _log(String.format("onNext with return value \"%b\"", bool));
      }
    };
  }

  // -----------------------------------------------------------------------------------
  // Method that help wiring up the example (irrelevant to RxJava)

  private void _doSomeLongOperation_thatBlocksCurrentThread() {
    _log("performing long operation");

    try {
      Thread.sleep(3000);
    } catch (InterruptedException e) {
      Timber.d("Operation was interrupted");
    }
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
  }

  private boolean _isCurrentlyOnMainThread() {
    return Looper.myLooper() == Looper.getMainLooper();
  }

  private static class LogAdapter extends ArrayAdapter<String> {

    public LogAdapter(Context context, List<String> logs) {
      super(context, R.layout.item_log, R.id.item_log, logs);
    }
  }
}