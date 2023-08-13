package com.morihacky.android.rxjava.fragments;

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
import com.morihacky.android.rxjava.databinding.FragmentPseudoCacheBinding;
import com.morihacky.android.rxjava.retrofit.Contributor;
import com.morihacky.android.rxjava.retrofit.GithubApi;
import com.morihacky.android.rxjava.retrofit.GithubService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class PseudoCacheFragment extends Fragment {

    private FragmentPseudoCacheBinding binding;

    private ArrayAdapter<String> adapterDetail, adapterSubscriptionInfo;
    private HashMap<String, Long> contributionMap = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPseudoCacheBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initListeners();
    }

    private void initListeners() {
        binding.btnPseudoCacheConcat.setOnClickListener(view -> onConcatBtnClicked());
        binding.btnPseudoCacheConcatEager.setOnClickListener(view -> onConcatEagerBtnClicked());
        binding.btnPseudoCacheMerge.setOnClickListener(view -> onMergeBtnClicked());
        binding.btnPseudoCacheMergeSlowDisk.setOnClickListener(view -> onMergeSlowBtnClicked());
        binding.btnPseudoCacheMergeOptimized.setOnClickListener(view -> onMergeOptimizedBtnClicked());
        binding.btnPseudoCacheMergeOptimizedSlowDisk.setOnClickListener(view -> onMergeOptimizedWithSlowDiskBtnClicked());

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void onConcatBtnClicked() {
        binding.infoPseudoCacheDemo.setText(R.string.msg_pseudoCache_demoInfo_concat);
        wireupDemo();

        Observable.concat(getSlowCachedDiskData(), getFreshNetworkData())
                .subscribeOn(Schedulers.io()) // we want to add a list item at time of subscription
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new DisposableObserver<Contributor>() {
                            @Override
                            public void onComplete() {
                                Timber.d("done loading all data");
                            }

                            @Override
                            public void onError(Throwable e) {
                                Timber.e(e, "arr something went wrong");
                            }

                            @Override
                            public void onNext(Contributor contributor) {
                                contributionMap.put(contributor.login, contributor.contributions);
                                adapterDetail.clear();
                                adapterDetail.addAll(mapAsList(contributionMap));
                            }
                        });
    }

    public void onConcatEagerBtnClicked() {
        binding.infoPseudoCacheDemo.setText(R.string.msg_pseudoCache_demoInfo_concatEager);
        wireupDemo();

        List<Observable<Contributor>> observables = new ArrayList<>(2);
        observables.add(getSlowCachedDiskData());
        observables.add(getFreshNetworkData());

        Observable.concatEager(observables)
                .subscribeOn(Schedulers.io()) // we want to add a list item at time of subscription
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new DisposableObserver<Contributor>() {
                            @Override
                            public void onComplete() {
                                Timber.d("done loading all data");
                            }

                            @Override
                            public void onError(Throwable e) {
                                Timber.e(e, "arr something went wrong");
                            }

                            @Override
                            public void onNext(Contributor contributor) {
                                contributionMap.put(contributor.login, contributor.contributions);
                                adapterDetail.clear();
                                adapterDetail.addAll(mapAsList(contributionMap));
                            }
                        });
    }

    public void onMergeBtnClicked() {
        binding.infoPseudoCacheDemo.setText(R.string.msg_pseudoCache_demoInfo_merge);
        wireupDemo();

        Observable.merge(getCachedDiskData(), getFreshNetworkData())
                .subscribeOn(Schedulers.io()) // we want to add a list item at time of subscription
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new DisposableObserver<Contributor>() {
                            @Override
                            public void onComplete() {
                                Timber.d("done loading all data");
                            }

                            @Override
                            public void onError(Throwable e) {
                                Timber.e(e, "arr something went wrong");
                            }

                            @Override
                            public void onNext(Contributor contributor) {
                                contributionMap.put(contributor.login, contributor.contributions);
                                adapterDetail.clear();
                                adapterDetail.addAll(mapAsList(contributionMap));
                            }
                        });
    }


    public void onMergeSlowBtnClicked() {
        binding.infoPseudoCacheDemo.setText(R.string.msg_pseudoCache_demoInfo_mergeSlowDisk);
        wireupDemo();

        Observable.merge(getSlowCachedDiskData(), getFreshNetworkData())
                .subscribeOn(Schedulers.io()) // we want to add a list item at time of subscription
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new DisposableObserver<Contributor>() {
                            @Override
                            public void onComplete() {
                                Timber.d("done loading all data");
                            }

                            @Override
                            public void onError(Throwable e) {
                                Timber.e(e, "arr something went wrong");
                            }

                            @Override
                            public void onNext(Contributor contributor) {
                                contributionMap.put(contributor.login, contributor.contributions);
                                adapterDetail.clear();
                                adapterDetail.addAll(mapAsList(contributionMap));
                            }
                        });
    }

    public void onMergeOptimizedBtnClicked() {
        binding.infoPseudoCacheDemo.setText(R.string.msg_pseudoCache_demoInfo_mergeOptimized);
        wireupDemo();

        getFreshNetworkData() //
                .publish(
                        network -> //
                                Observable.merge(
                                        network, //
                                        getCachedDiskData().takeUntil(network)))
                .subscribeOn(Schedulers.io()) // we want to add a list item at time of subscription
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new DisposableObserver<Contributor>() {
                            @Override
                            public void onComplete() {
                                Timber.d("done loading all data");
                            }

                            @Override
                            public void onError(Throwable e) {
                                Timber.e(e, "arr something went wrong");
                            }

                            @Override
                            public void onNext(Contributor contributor) {
                                contributionMap.put(contributor.login, contributor.contributions);
                                adapterDetail.clear();
                                adapterDetail.addAll(mapAsList(contributionMap));
                            }
                        });
    }

    public void onMergeOptimizedWithSlowDiskBtnClicked() {
        binding.infoPseudoCacheDemo.setText(R.string.msg_pseudoCache_demoInfo_mergeOptimizedSlowDisk);
        wireupDemo();

        getFreshNetworkData() //
                .publish(
                        network -> //
                                Observable.merge(
                                        network, //
                                        getSlowCachedDiskData().takeUntil(network)))
                .subscribeOn(Schedulers.io()) // we want to add a list item at time of subscription
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new DisposableObserver<Contributor>() {
                            @Override
                            public void onComplete() {
                                Timber.d("done loading all data");
                            }

                            @Override
                            public void onError(Throwable e) {
                                Timber.e(e, "arr something went wrong");
                            }

                            @Override
                            public void onNext(Contributor contributor) {
                                contributionMap.put(contributor.login, contributor.contributions);
                                adapterDetail.clear();
                                adapterDetail.addAll(mapAsList(contributionMap));
                            }
                        });
    }

    // -----------------------------------------------------------------------------------
    // WIRING for example

    private void wireupDemo() {
        contributionMap = new HashMap<>();

        adapterDetail = new ArrayAdapter<>(getActivity(), R.layout.item_log_white, R.id.item_log, new ArrayList<>());
        binding.infoPseudoCacheListDtl.setAdapter(adapterDetail);

        adapterSubscriptionInfo = new ArrayAdapter<>(getActivity(), R.layout.item_log_white, R.id.item_log, new ArrayList<>());
        binding.infoPseudoCacheListSubscription.setAdapter(adapterSubscriptionInfo);
    }

    private Observable<Contributor> getSlowCachedDiskData() {
        return Observable.timer(1, TimeUnit.SECONDS).flatMap(dummy -> getCachedDiskData());
    }

    private Observable<Contributor> getCachedDiskData() {
        List<Contributor> list = new ArrayList<>();
        Map<String, Long> map = dummyDiskData();

        for (String username : map.keySet()) {
            Contributor c = new Contributor();
            c.login = username;
            c.contributions = map.get(username);
            list.add(c);
        }

        return Observable.fromIterable(list) //
                .doOnSubscribe((data) -> new Handler(Looper.getMainLooper()) //
                        .post(() -> adapterSubscriptionInfo.add("(disk) cache subscribed"))) //
                .doOnComplete(() -> new Handler(Looper.getMainLooper()) //
                        .post(() -> adapterSubscriptionInfo.add("(disk) cache completed")));
    }

    private Observable<Contributor> getFreshNetworkData() {
        String githubToken = getResources().getString(R.string.github_oauth_token);
        GithubApi githubService = GithubService.createGithubService(githubToken);

        return githubService
                .contributors("square", "retrofit")
                .flatMap(Observable::fromIterable)
                .doOnSubscribe((data) -> new Handler(Looper.getMainLooper()) //
                        .post(() -> adapterSubscriptionInfo.add("(network) subscribed"))) //
                .doOnComplete(() -> new Handler(Looper.getMainLooper()) //
                        .post(() -> adapterSubscriptionInfo.add("(network) completed")));
    }

    private List<String> mapAsList(HashMap<String, Long> map) {
        List<String> list = new ArrayList<>();

        for (String username : map.keySet()) {
            String rowLog = String.format("%s [%d]", username, contributionMap.get(username));
            list.add(rowLog);
        }

        return list;
    }

    private Map<String, Long> dummyDiskData() {
        Map<String, Long> map = new HashMap<>();
        map.put("JakeWharton", 0L);
        map.put("pforhan", 0L);
        map.put("edenman", 0L);
        map.put("swankjesse", 0L);
        map.put("bruceLee", 0L);
        return map;
    }
}