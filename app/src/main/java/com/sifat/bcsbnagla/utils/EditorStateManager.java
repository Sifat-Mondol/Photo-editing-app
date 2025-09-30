package com.sifat.bcsbnagla.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;

public class EditorStateManager {
    private static final String PREF_NAME = "EditorState";
    private static final String KEY_HAS_SAVED_STATE = "has_saved_state";
    private static final String KEY_IMAGE_URL = "image_url";
    private static final String KEY_EDIT_MODE = "edit_mode";
    private static final String KEY_SEEKBAR_PROGRESS = "seekbar_progress";

    private Context context;
    private SharedPreferences prefs;

    public EditorStateManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveEditingState(String imageUrl, Bitmap currentBitmap, String editMode, int seekbarProgress) {
        try {
            // Save bitmap to internal storage
            File file = new File(context.getFilesDir(), "editing_state.jpg");
            FileOutputStream fos = new FileOutputStream(file);
            currentBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();

            // Save metadata to SharedPreferences
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(KEY_HAS_SAVED_STATE, true);
            editor.putString(KEY_IMAGE_URL, imageUrl);
            editor.putString(KEY_EDIT_MODE, editMode);
            editor.putInt(KEY_SEEKBAR_PROGRESS, seekbarProgress);
            editor.apply();

            Log.d("EditorStateManager", "State saved successfully");
        } catch (Exception e) {
            Log.e("EditorStateManager", "Error saving state: " + e.getMessage());
        }
    }

    public boolean hasSavedState(String imageUrl) {
        boolean hasSaved = prefs.getBoolean(KEY_HAS_SAVED_STATE, false);
        String savedUrl = prefs.getString(KEY_IMAGE_URL, "");
        return hasSaved && savedUrl.equals(imageUrl);
    }

    public Bitmap loadSavedBitmap() {
        try {
            File file = new File(context.getFilesDir(), "editing_state.jpg");
            if (file.exists()) {
                return BitmapFactory.decodeFile(file.getAbsolutePath());
            }
        } catch (Exception e) {
            Log.e("EditorStateManager", "Error loading bitmap: " + e.getMessage());
        }
        return null;
    }

    public String getSavedEditMode() {
        return prefs.getString(KEY_EDIT_MODE, "NONE");
    }

    public int getSavedSeekbarProgress() {
        return prefs.getInt(KEY_SEEKBAR_PROGRESS, 50);
    }

    public void clearSavedState() {
        try {
            // Delete saved bitmap
            File file = new File(context.getFilesDir(), "editing_state.jpg");
            if (file.exists()) {
                file.delete();
            }

            // Clear SharedPreferences
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();

            Log.d("EditorStateManager", "State cleared successfully");
        } catch (Exception e) {
            Log.e("EditorStateManager", "Error clearing state: " + e.getMessage());
        }
    }
}