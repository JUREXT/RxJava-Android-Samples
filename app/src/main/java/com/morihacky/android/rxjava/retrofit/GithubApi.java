package com.morihacky.android.rxjava.retrofit;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface GithubApi {

  /** See <a href="https://developer.github.com/v3/repos/#list-contributors">...</a> */
  @GET("/repos/{owner}/{repo}/contributors")
  Observable<List<Contributor>> contributors(
      @Path("owner") String owner, @Path("repo") String repo);

  @GET("/repos/{owner}/{repo}/contributors")
  List<Contributor> getContributors(@Path("owner") String owner, @Path("repo") String repo);

  /** See <a href="https://developer.github.com/v3/users/">...</a> */
  @GET("/users/{user}")
  Observable<User> user(@Path("user") String user);

  /** See <a href="https://developer.github.com/v3/users/">...</a> */
  @GET("/users/{user}")
  User getUser(@Path("user") String user);
}
