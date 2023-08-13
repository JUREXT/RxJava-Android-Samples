package com.morihacky.android.rxjava.fragments;

import static java.lang.String.format;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.morihacky.android.rxjava.R;
import com.morihacky.android.rxjava.databinding.FragmentRetrofitAsyncTaskDeathBinding;
import com.morihacky.android.rxjava.retrofit.GithubApi;
import com.morihacky.android.rxjava.retrofit.GithubService;
import com.morihacky.android.rxjava.retrofit.User;

import java.util.ArrayList;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.observers.DisposableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class RetrofitAsyncTaskDeathFragment extends Fragment {

    private FragmentRetrofitAsyncTaskDeathBinding binding;

    private GithubApi _githubService;
    private ArrayAdapter<String> _adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRetrofitAsyncTaskDeathBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String githubToken = getResources().getString(R.string.github_oauth_token);
        _githubService = GithubService.createGithubService(githubToken);

        _adapter = new ArrayAdapter<>(getActivity(), R.layout.item_log, R.id.item_log, new ArrayList<>());
        //_adapter.setNotifyOnChange(true);
        binding.logList.setAdapter(_adapter);

        binding.btnDemoRetrofitAsyncDeath.setOnClickListener(view1 -> onGetGithubUserClicked());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void onGetGithubUserClicked() {
        _adapter.clear();

    /*new AsyncTask<String, Void, User>() {
        @Override
        protected User doInBackground(String... params) {
            return _githubService.getUser(params[0]);
        }

        @Override
        protected void onPostExecute(User user) {
            _adapter.add(format("%s  = [%s: %s]", _username.getText(), user.name, user.email));
        }
    }.execute(_username.getText().toString());*/

        _githubService
                .user(binding.btnDemoRetrofitAsyncDeathUsername.getText().toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new DisposableObserver<User>() {
                            @Override
                            public void onComplete() {
                            }

                            @Override
                            public void onError(Throwable e) {
                            }

                            @Override
                            public void onNext(User user) {
                                _adapter.add(format("%s  = [%s: %s]", binding.btnDemoRetrofitAsyncDeathUsername.getText(), user.name, user.email));
                            }
                        });
    }

    // -----------------------------------------------------------------------------------

    private class GetGithubUser extends AsyncTask<String, Void, User> {

        @Override
        protected User doInBackground(String... params) {
            return _githubService.getUser(params[0]);
        }

        @Override
        protected void onPostExecute(User user) {
            _adapter.add(format("%s  = [%s: %s]", binding.btnDemoRetrofitAsyncDeathUsername.getText(), user.name, user.email));
        }
    }
}