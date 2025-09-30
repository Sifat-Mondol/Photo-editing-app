package com.sifat.bcsbnagla.view.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.sifat.bcsbnagla.R;
import com.sifat.bcsbnagla.view.activities.LoginActivity;

public class ProfileFragment extends Fragment {

    private TextView userInfoTv;
    private Button logoutBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        userInfoTv = view.findViewById(R.id.userInfoTv);
        logoutBtn = view.findViewById(R.id.logoutBtn);

        // Display user info (optional)
        userInfoTv.setText("Welcome to your profile!");

        // Logout button click
        logoutBtn.setOnClickListener(v -> {
            // Clear any saved data if needed
            // For example: SharedPreferences, tokens, etc.

            // Navigate to LoginActivity
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        });

        return view;
    }
}