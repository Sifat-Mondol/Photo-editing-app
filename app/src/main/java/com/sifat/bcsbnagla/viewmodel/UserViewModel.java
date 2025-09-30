package com.sifat.bcsbnagla.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.sifat.bcsbnagla.model.LoginResponse;
import com.sifat.bcsbnagla.model.SignupResponse;
import com.sifat.bcsbnagla.repository.UserRepository;

public class UserViewModel extends ViewModel {
    private UserRepository repository = new UserRepository();

    public LiveData<LoginResponse> login(String email, String password) {
        return repository.login(email, password);
    }

    public LiveData<SignupResponse> signup(String username, String email, String password) {
        return repository.signup(username, email, password);
    }
}