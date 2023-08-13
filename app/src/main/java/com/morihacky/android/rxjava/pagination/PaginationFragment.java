package com.morihacky.android.rxjava.pagination;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.morihacky.android.rxjava.MainActivity;
import com.morihacky.android.rxjava.databinding.FragmentPaginationBinding;
import com.morihacky.android.rxjava.rxbus.RxBus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.processors.PublishProcessor;

public class PaginationFragment extends Fragment {

    private FragmentPaginationBinding binding;

    private PaginationAdapter _adapter;
    private RxBus _bus;
    private CompositeDisposable _disposables;
    private PublishProcessor<Integer> _paginator;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        _bus = ((MainActivity) requireActivity()).getRxBusSingleton();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        binding.listPaging.setLayoutManager(layoutManager);

        _adapter = new PaginationAdapter(_bus);
        binding.listPaging.setAdapter(_adapter);

        _paginator = PublishProcessor.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        _disposables = new CompositeDisposable();

        Disposable d2 = _paginator
                        .onBackpressureDrop()
                        .concatMap(nextPage -> _itemsFromNetworkCall(nextPage + 1, 10))
                        .observeOn(AndroidSchedulers.mainThread())
                        .map(
                                items -> {
                                    int start = _adapter.getItemCount() - 1;

                                    _adapter.addItems(items);
                                    _adapter.notifyItemRangeInserted(start, 10);

                                    binding.progressPaging.setVisibility(View.INVISIBLE);

                                    return items;
                                })
                        .subscribe();

        // I'm using an Rxbus purely to hear from a nested button click
        // we don't really need Rx for this part. it's just easy ¯\_(ツ)_/¯
        Disposable d1 = _bus.asFlowable()
                        .subscribe(event -> {
                                    if (event instanceof PaginationAdapter.ItemBtnViewHolder.PageEvent) {
                                        // trigger the paginator for the next event
                                        int nextPage = _adapter.getItemCount() - 1;
                                        _paginator.onNext(nextPage);
                                    }
                                });

        _disposables.add(d1);
        _disposables.add(d2);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        _disposables.clear();
        binding = null;
    }

    /**
     * Fake Observable that simulates a network call and then sends down a list of items
     */
    private Flowable<List<String>> _itemsFromNetworkCall(int start, int count) {
        return Flowable.just(true)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(dummy -> binding.progressPaging.setVisibility(View.VISIBLE))
                .delay(2, TimeUnit.SECONDS)
                .map(
                        dummy -> {
                            List<String> items = new ArrayList<>();
                            for (int i = 0; i < count; i++) {
                                items.add("Item " + (start + i));
                            }
                            return items;
                        });
    }

    // -----------------------------------------------------------------------------------
    // WIRING up the views required for this example

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPaginationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}