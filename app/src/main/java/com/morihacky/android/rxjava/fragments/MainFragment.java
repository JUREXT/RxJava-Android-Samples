package com.morihacky.android.rxjava.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.morihacky.android.rxjava.databinding.FragmentMainBinding;
import com.morihacky.android.rxjava.pagination.PaginationAutoFragment;
import com.morihacky.android.rxjava.rxbus.RxBusDemoFragment;

public class MainFragment extends Fragment {

    private FragmentMainBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMainBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initListeners();
    }

    private void initListeners() {
        binding.btnDemoSchedulers.setOnClickListener(view -> clickedOn(new ConcurrencyWithSchedulersDemoFragment()));
        binding.btnDemoBuffer.setOnClickListener(view -> clickedOn(new BufferDemoFragment()));
        binding.btnDemoDebounce.setOnClickListener(view -> clickedOn(new DebounceSearchEmitterFragment()));
        binding.btnDemoRetrofit.setOnClickListener(view -> clickedOn(new RetrofitFragment()));
        binding.btnDemoPolling.setOnClickListener(view -> clickedOn(new PollingFragment()));
        binding.btnDemoDoubleBindingTextview.setOnClickListener(view -> clickedOn(new DoubleBindingTextViewFragment()));
        binding.btnDemoRxbus.setOnClickListener(view -> clickedOn(new RxBusDemoFragment()));
        binding.btnDemoFormValidationCombinel.setOnClickListener(view -> clickedOn(new FormValidationCombineLatestFragment()));
        binding.btnDemoPseudoCache.setOnClickListener(view -> clickedOn(new PseudoCacheFragment()));
        binding.btnDemoTiming.setOnClickListener(view -> clickedOn(new TimingDemoFragment()));
        binding.btnDemoTimeout.setOnClickListener(view -> clickedOn(new TimeoutDemoFragment()));
        binding.btnDemoExponentialBackoff.setOnClickListener(view -> clickedOn(new ExponentialBackoffFragment()));
        binding.btnDemoRotationPersist.setOnClickListener(view -> demoRotationPersist());
        binding.btnDemoPagination.setOnClickListener(view -> demoPaging());
        binding.btnDemoNetworkDetector.setOnClickListener(view -> clickedOn(new NetworkDetectorFragment()));
        binding.btnDemoUsing.setOnClickListener(view -> clickedOn(new UsingFragment()));
        binding.btnDemoMulticastPlayground.setOnClickListener(view -> clickedOn(new MulticastPlaygroundFragment()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // TODO: Add onDestroyView everywhere.
    }

    void demoRotationPersist() {
        clickedOn(new RotationPersist3Fragment());
        // clickedOn(new RotationPersist2Fragment());
        // clickedOn(new RotationPersist1Fragment());
    }

    void demoPaging() {
        clickedOn(new PaginationAutoFragment());
        //clickedOn(new PaginationFragment());
    }

    private void clickedOn(@NonNull Fragment fragment) {
        final String tag = fragment.getClass().toString();
        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(tag)
                .replace(android.R.id.content, fragment, tag)
                .commit();
    }
}