package com.morihacky.android.rxjava.retrofit;

import static java.lang.String.format;

import android.text.TextUtils;


import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GithubService {

    public static GithubApi createGithubService(final String githubToken) {
        Retrofit.Builder builder =
                new Retrofit.Builder()
                        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                        .addConverterFactory(GsonConverterFactory.create())
                        .baseUrl("https://api.github.com");

        if (!TextUtils.isEmpty(githubToken)) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(
                            chain -> {
                                Request request = chain.request();
                                Request newReq =
                                        request
                                                .newBuilder()
                                                .addHeader("Authorization", format("token %s", githubToken))
                                                .build();
                                return chain.proceed(newReq);
                            }).build();

            builder.client(client);
        }
        return builder.build().create(GithubApi.class);
    }
}