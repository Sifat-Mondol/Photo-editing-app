package com.sifat.bcsbnagla.view.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sifat.bcsbnagla.R;
import com.sifat.bcsbnagla.network.ApiClient;
import com.sifat.bcsbnagla.network.ApiService;
import com.sifat.bcsbnagla.view.activities.EditorActivity;
import com.sifat.bcsbnagla.view.adapters.PhotoAdapter;
import com.sifat.bcsbnagla.viewmodel.PhotoViewModel;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView emptyView;
    private PhotoViewModel viewModel;
    private PhotoAdapter adapter;
    private String userToken;
    private FloatingActionButton fabAddPhoto;

    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<String> galleryLauncher;
    private ActivityResultLauncher<String> permissionLauncher;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        emptyView = view.findViewById(R.id.emptyView);
        fabAddPhoto = view.findViewById(R.id.fabAddPhoto);

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        userToken = getActivity().getIntent().getStringExtra("token");
        Log.d("HomeFragment", "Token: " + userToken);

        setupActivityLaunchers();

        adapter = new PhotoAdapter(photo -> {
            Intent intent = new Intent(getContext(), EditorActivity.class);
            intent.putExtra("imageUrl", photo.getImageUrl());
            intent.putExtra("token", userToken);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(PhotoViewModel.class);

        loadPhotos();

        fabAddPhoto.setOnClickListener(v -> showPhotoSourceDialog());

        return view;
    }

    private void setupActivityLaunchers() {
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        try {
                            Bundle extras = result.getData().getExtras();
                            Bitmap imageBitmap = (Bitmap) extras.get("data");
                            if (imageBitmap != null) {
                                uploadPhoto(imageBitmap);
                            } else {
                                Toast.makeText(getContext(), "Failed to capture photo", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Log.e("HomeFragment", "Camera error: " + e.getMessage());
                            Toast.makeText(getContext(), "Camera error", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        // <CHANGE> Open EditorActivity directly with selected image URI
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        Log.d("HomeFragment", "Image URI: " + uri.toString());

                        // Open EditorActivity with the selected image
                        Intent intent = new Intent(getContext(), EditorActivity.class);
                        intent.setData(uri);  // Pass URI using setData()
                        startActivity(intent);
                    } else {
                        Log.e("HomeFragment", "URI is null");
                        Toast.makeText(getContext(), "No image selected", Toast.LENGTH_SHORT).show();
                    }
                });

        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openCamera();
                    } else {
                        Toast.makeText(getContext(), "Camera permission denied", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadBitmapFromUri(Uri uri) {
        try {
            Log.d("HomeFragment", "Loading bitmap from URI...");
            ContentResolver contentResolver = getActivity().getContentResolver();
            InputStream inputStream = contentResolver.openInputStream(uri);

            if (inputStream == null) {
                Log.e("HomeFragment", "InputStream is null");
                Toast.makeText(getContext(), "Cannot access image", Toast.LENGTH_SHORT).show();
                return;
            }

            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            if (bitmap != null) {
                Log.d("HomeFragment", "Bitmap loaded: " + bitmap.getWidth() + "x" + bitmap.getHeight());
                uploadPhoto(bitmap);
            } else {
                Log.e("HomeFragment", "Bitmap is null after decode");
                Toast.makeText(getContext(), "Failed to decode image", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("HomeFragment", "Error loading bitmap: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showPhotoSourceDialog() {
        String[] options = {"Take Photo", "Choose from Gallery"};

        new AlertDialog.Builder(getContext())
                .setTitle("Add Photo")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        checkCameraPermissionAndOpen();
                    } else {
                        openGallery();
                    }
                })
                .show();
    }

    private void checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            cameraLauncher.launch(takePictureIntent);
        } else {
            Toast.makeText(getContext(), "No camera app found", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        galleryLauncher.launch("image/*");
    }

    private void uploadPhoto(Bitmap bitmap) {
        if (bitmap == null) {
            Toast.makeText(getContext(), "Invalid image", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userToken == null || userToken.isEmpty()) {
            Toast.makeText(getContext(), "Not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(getContext(), "Uploading...", Toast.LENGTH_SHORT).show();
        Log.d("HomeFragment", "Starting upload");

        int maxSize = 800;
        if (bitmap.getWidth() > maxSize || bitmap.getHeight() > maxSize) {
            float scale = Math.min(
                    ((float) maxSize / bitmap.getWidth()),
                    ((float) maxSize / bitmap.getHeight())
            );
            int newWidth = Math.round(bitmap.getWidth() * scale);
            int newHeight = Math.round(bitmap.getHeight() * scale);
            bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
            Log.d("HomeFragment", "Resized to: " + newWidth + "x" + newHeight);
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String encodedImage = Base64.encodeToString(byteArray, Base64.NO_WRAP);

        Log.d("HomeFragment", "Encoded size: " + encodedImage.length() + " chars");

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<ResponseBody> call = apiService.uploadPhoto(userToken, encodedImage, "Uploaded from app");

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d("HomeFragment", "Upload successful!");
                    Toast.makeText(getContext(), "Photo uploaded!", Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(() -> loadPhotos(), 1500);
                } else {
                    Log.e("HomeFragment", "Upload failed: " + response.code());
                    Toast.makeText(getContext(), "Upload failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("HomeFragment", "Network error: " + t.getMessage());
                t.printStackTrace();
                Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPhotos() {
        Log.d("HomeFragment", "Loading photos...");

        viewModel.getPhotos(userToken).observe(getViewLifecycleOwner(), photos -> {
            if (photos != null && !photos.isEmpty()) {
                Log.d("HomeFragment", "Loaded " + photos.size() + " photos");
                recyclerView.setVisibility(View.VISIBLE);
                emptyView.setVisibility(View.GONE);
                adapter.setPhotos(photos);
            } else {
                Log.d("HomeFragment", "No photos found");
                recyclerView.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPhotos();
    }
}