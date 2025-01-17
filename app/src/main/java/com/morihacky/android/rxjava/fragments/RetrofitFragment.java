package com.morihacky.android.rxjava.fragments;

import static android.text.TextUtils.isEmpty;
import static java.lang.String.format;

import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.morihacky.android.rxjava.R;
import com.morihacky.android.rxjava.databinding.FragmentRetrofitBinding;
import com.morihacky.android.rxjava.retrofit.Contributor;
import com.morihacky.android.rxjava.retrofit.GithubApi;
import com.morihacky.android.rxjava.retrofit.GithubService;
import com.morihacky.android.rxjava.retrofit.User;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.observers.DisposableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;

public class RetrofitFragment extends Fragment {

    private FragmentRetrofitBinding binding;

    private ArrayAdapter<String> _adapter;
    private GithubApi _githubService;
    private CompositeDisposable _disposables;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String githubToken = getResources().getString(R.string.github_oauth_token);
        _githubService = GithubService.createGithubService(githubToken);
        _disposables = new CompositeDisposable();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRetrofitBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        _adapter = new ArrayAdapter<>(getActivity(), R.layout.item_log, R.id.item_log, new ArrayList<>());
        //_adapter.setNotifyOnChange(true);
        binding.logList.setAdapter(_adapter);

        binding.btnDemoRetrofitContributors.setOnClickListener(view1 -> onListContributorsClicked());
        binding.btnDemoRetrofitContributorsWithUserInfo.setOnClickListener(view1 -> onListContributorsWithFullUserInfoClicked());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        _disposables.dispose();
        binding = null;
    }

    public void onListContributorsClicked() {
        _adapter.clear();
        _disposables.add(
                _githubService
                        .contributors(binding.demoRetrofitContributorsUsername.getText().toString(), binding.demoRetrofitContributorsRepository.getText().toString())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(
                                new DisposableObserver<List<Contributor>>() {

                                    @Override
                                    public void onComplete() {
                                        Timber.d("Retrofit call 1 completed");
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        Timber.e(e, "woops we got an error while getting the list of contributors");
                                    }

                                    @Override
                                    public void onNext(List<Contributor> contributors) {
                                        for (Contributor c : contributors) {
                                            _adapter.add(
                                                    format(
                                                            "%s has made %d contributions to %s",
                                                            c.login, c.contributions, binding.demoRetrofitContributorsRepository.getText().toString()));

                                            Timber.d(
                                                    "%s has made %d contributions to %s",
                                                    c.login, c.contributions, binding.demoRetrofitContributorsRepository.getText().toString());
                                        }
                                    }
                                }));
    }

    public void onListContributorsWithFullUserInfoClicked() {
        _adapter.clear();

        _disposables.add(
                _githubService
                        .contributors(binding.demoRetrofitContributorsUsername.getText().toString(), binding.demoRetrofitContributorsRepository.getText().toString())
                        .flatMap(Observable::fromIterable)
                        .flatMap( contributor -> { Observable<User> _userObservable = _githubService
                                                    .user(contributor.login)
                                                    .filter(user -> !isEmpty(user.name) && !isEmpty(user.email));

                                    return Observable.zip(_userObservable, Observable.just(contributor), Pair::new);
                                })
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(
                                new DisposableObserver<Pair<User, Contributor>>() {
                                    @Override
                                    public void onComplete() {
                                        Timber.d("Retrofit call 2 completed ");
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        Timber.e(e,"error while getting the list of contributors along with full " + "names");
                                    }

                                    @Override
                                    public void onNext(Pair<User, Contributor> pair) {
                                        User user = pair.first;
                                        Contributor contributor = pair.second;

                                        _adapter.add(
                                                format(
                                                        "%s(%s) has made %d contributions to %s",
                                                        user.name,
                                                        user.email,
                                                        contributor.contributions,
                                                        binding.demoRetrofitContributorsRepository.getText().toString()));

                                        _adapter.notifyDataSetChanged();

                                        Timber.d(
                                                "%s(%s) has made %d contributions to %s",
                                                user.name,
                                                user.email,
                                                contributor.contributions,
                                                binding.demoRetrofitContributorsRepository.getText().toString());
                                    }
                                }));
    }
}