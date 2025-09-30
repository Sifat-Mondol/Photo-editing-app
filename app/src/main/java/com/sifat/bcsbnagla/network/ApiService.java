package com.sifat.bcsbnagla.network;

import com.sifat.bcsbnagla.model.LoginResponse;
import com.sifat.bcsbnagla.model.Photo;
import com.sifat.bcsbnagla.model.SignupResponse;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiService {

    @FormUrlEncoded
    @POST("login.php")
    Call<LoginResponse> login(
            @Field("email") String email,
            @Field("password") String password
    );

    @FormUrlEncoded
    @POST("signup.php")
    Call<SignupResponse> signup(
            @Field("username") String username,
            @Field("email") String email,
            @Field("password") String password
    );

    @FormUrlEncoded
    @POST("upload-photo.php")
    Call<ResponseBody> uploadPhoto(
            @Header("Authorization") String token,
            @Field("image") String image,
            @Field("description") String description
    );

    @GET("photos.php")
    Call<List<Photo>> getPhotos(
            @Header("Authorization") String token
    );
}