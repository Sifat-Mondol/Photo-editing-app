package com.sifat.bcsbnagla.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.sifat.bcsbnagla.model.Photo;
import com.sifat.bcsbnagla.network.ApiClient;
import com.sifat.bcsbnagla.network.ApiService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PhotoRepository {
    private ApiService apiService;

    public PhotoRepository() {
        apiService = ApiClient.getClient().create(ApiService.class);
    }

    public LiveData<List<Photo>> getPhotos(String token) {
        MutableLiveData<List<Photo>> photosLiveData = new MutableLiveData<>();

        Log.d("PhotoRepository", "Fetching photos with token: " + token);

        Call<List<Photo>> call = apiService.getPhotos(token);
        call.enqueue(new Callback<List<Photo>>() {
            @Override
            public void onResponse(Call<List<Photo>> call, Response<List<Photo>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("PhotoRepository", "Photos fetched successfully: " + response.body().size());
                    photosLiveData.setValue(response.body());
                } else {
                    Log.e("PhotoRepository", "Failed to fetch photos: " + response.code());
                    photosLiveData.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<List<Photo>> call, Throwable t) {
                Log.e("PhotoRepository", "Error fetching photos: " + t.getMessage());
                photosLiveData.setValue(null);
            }
        });

        return photosLiveData;
    }
}