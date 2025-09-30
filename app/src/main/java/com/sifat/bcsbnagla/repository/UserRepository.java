package com.sifat.bcsbnagla.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.sifat.bcsbnagla.model.LoginResponse;
import com.sifat.bcsbnagla.model.SignupResponse;
import com.sifat.bcsbnagla.network.ApiClient;
import com.sifat.bcsbnagla.network.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRepository {
    private ApiService apiService = ApiClient.getClient().create(ApiService.class);

    public LiveData<LoginResponse> login(String email, String password) {
        MutableLiveData<LoginResponse> data = new MutableLiveData<>();
        apiService.login(email, password).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful()) {
                    data.setValue(response.body());
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                data.setValue(null);
            }
        });
        return data;
    }

    public LiveData<SignupResponse> signup(String username, String email, String password) {
        MutableLiveData<SignupResponse> data = new MutableLiveData<>();
        apiService.signup(username, email, password).enqueue(new Callback<SignupResponse>() {
            @Override
            public void onResponse(Call<SignupResponse> call, Response<SignupResponse> response) {
                if (response.isSuccessful()) {
                    data.setValue(response.body());
                }
            }

            @Override
            public void onFailure(Call<SignupResponse> call, Throwable t) {
                data.setValue(null);
            }
        });
        return data;
    }
}