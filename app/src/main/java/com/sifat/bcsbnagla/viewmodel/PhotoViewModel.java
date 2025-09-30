package com.sifat.bcsbnagla.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.sifat.bcsbnagla.model.Photo;
import com.sifat.bcsbnagla.repository.PhotoRepository;

import java.util.List;

public class PhotoViewModel extends ViewModel {
    private PhotoRepository repository;

    public PhotoViewModel() {
        repository = new PhotoRepository();
    }

    public LiveData<List<Photo>> getPhotos(String token) {
        return repository.getPhotos(token);
    }
}