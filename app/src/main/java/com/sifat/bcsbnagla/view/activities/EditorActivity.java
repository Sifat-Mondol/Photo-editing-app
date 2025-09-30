package com.sifat.bcsbnagla.view.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sifat.bcsbnagla.R;
import com.sifat.bcsbnagla.view.adapters.FilterAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class EditorActivity extends AppCompatActivity {

    // Views
    private ImageView imageView;
    private ImageView imageViewOriginal;
    private ImageButton btnBack;
    private ImageButton btnCompare;
    private ImageButton btnSave;
    private Button tabAdjust;
    private Button tabFilters;
    private Button tabTools;
    private Button tabEffects;
    private HorizontalScrollView adjustContent;
    private RecyclerView filtersRecyclerView;
    private LinearLayout toolsContent;
    private LinearLayout effectsContent;
    private LinearLayout seekBarContainer;
    private SeekBar seekBar;
    private TextView seekBarLabel;
    private TextView seekBarValue;

    // Adjust buttons
    private LinearLayout btnBrightness;
    private LinearLayout btnContrast;
    private LinearLayout btnSaturation;
    private LinearLayout btnHighlights;
    private LinearLayout btnShadows;
    private LinearLayout btnSharpness;

    // Tool buttons
    private LinearLayout btnCrop;
    private LinearLayout btnRotate;
    private LinearLayout btnFlip;
    private LinearLayout btnUndo;
    private LinearLayout btnRedo;
    private LinearLayout btnShare;

    // Image data
    private Bitmap originalBitmap;
    private Bitmap currentBitmap;
    private Bitmap baselineBitmap;
    private Uri imageUri;

    // Edit state
    private EditMode currentEditMode = EditMode.NONE;
    private Stack<Bitmap> undoStack = new Stack<>();
    private Stack<Bitmap> redoStack = new Stack<>();

    // Store adjustment values for each mode (50 = no adjustment)
    private Map<EditMode, Integer> adjustmentValues = new HashMap<>();

    // Adapters
    private FilterAdapter filterAdapter;

    // Dialog reference for lifecycle management
    private AlertDialog resumeDialog;

    // Edit modes
    private enum EditMode {
        NONE, BRIGHTNESS, CONTRAST, SATURATION, HIGHLIGHTS, SHADOWS, SHARPNESS
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        try {
            // Initialize adjustment values to default (50 = 0 adjustment)
            adjustmentValues.put(EditMode.BRIGHTNESS, 50);
            adjustmentValues.put(EditMode.CONTRAST, 50);
            adjustmentValues.put(EditMode.SATURATION, 50);
            adjustmentValues.put(EditMode.HIGHLIGHTS, 50);
            adjustmentValues.put(EditMode.SHADOWS, 50);
            adjustmentValues.put(EditMode.SHARPNESS, 50);

            initViews();
            setupListeners();
            setupFilters();
            setupIconTints();

            switchTab(0);

            setupData();
        } catch (Exception e) {
            Log.e("EditorActivity", "Error in onCreate: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Error initializing editor", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        try {
            // Main views
            imageView = findViewById(R.id.imageView);
            imageViewOriginal = findViewById(R.id.imageViewOriginal);

            // Top bar
            btnBack = findViewById(R.id.btnBack);
            btnCompare = findViewById(R.id.btnCompare);
            btnSave = findViewById(R.id.btnSave);

            // Tabs
            tabAdjust = findViewById(R.id.tabAdjust);
            tabFilters = findViewById(R.id.tabFilters);
            tabTools = findViewById(R.id.tabTools);
            tabEffects = findViewById(R.id.tabEffects);

            // Content containers
            adjustContent = findViewById(R.id.adjustContent);
            filtersRecyclerView = findViewById(R.id.filtersRecyclerView);
            toolsContent = findViewById(R.id.toolsContent);
            effectsContent = findViewById(R.id.effectsContent);

            // Seekbar
            seekBarContainer = findViewById(R.id.seekBarContainer);
            seekBar = findViewById(R.id.seekBar);
            seekBarLabel = findViewById(R.id.seekBarLabel);
            seekBarValue = findViewById(R.id.seekBarValue);

            // Adjust buttons
            btnBrightness = findViewById(R.id.btnBrightness);
            btnContrast = findViewById(R.id.btnContrast);
            btnSaturation = findViewById(R.id.btnSaturation);
            btnHighlights = findViewById(R.id.btnHighlights);
            btnShadows = findViewById(R.id.btnShadows);
            btnSharpness = findViewById(R.id.btnSharpness);

            // Tool buttons
            btnCrop = findViewById(R.id.btnCrop);
            btnRotate = findViewById(R.id.btnRotate);
            btnFlip = findViewById(R.id.btnFlip);
            btnUndo = findViewById(R.id.btnUndo);
            btnRedo = findViewById(R.id.btnRedo);
            btnShare = findViewById(R.id.btnShare);

            Log.d("EditorActivity", "All views initialized successfully");
        } catch (Exception e) {
            Log.e("EditorActivity", "Error initializing views: " + e.getMessage());
            throw e;
        }
    }

    private void setupListeners() {
        // Top bar - with null checks
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> onBackPressed());
        }
        if (btnCompare != null) {
            btnCompare.setOnClickListener(v -> toggleCompare());
        }
        if (btnSave != null) {
            btnSave.setOnClickListener(v -> saveImage());
        }

        // Tabs - with null checks
        if (tabAdjust != null) {
            tabAdjust.setOnClickListener(v -> switchTab(0));
        }
        if (tabFilters != null) {
            tabFilters.setOnClickListener(v -> switchTab(1));
        }
        if (tabTools != null) {
            tabTools.setOnClickListener(v -> switchTab(2));
        }
        if (tabEffects != null) {
            tabEffects.setOnClickListener(v -> switchTab(3));
        }

        // Adjust buttons - with null checks
        if (btnBrightness != null) {
            btnBrightness.setOnClickListener(v -> showAdjustmentControl(EditMode.BRIGHTNESS, "Brightness"));
        }
        if (btnContrast != null) {
            btnContrast.setOnClickListener(v -> showAdjustmentControl(EditMode.CONTRAST, "Contrast"));
        }
        if (btnSaturation != null) {
            btnSaturation.setOnClickListener(v -> showAdjustmentControl(EditMode.SATURATION, "Saturation"));
        }
        if (btnHighlights != null) {
            btnHighlights.setOnClickListener(v -> showAdjustmentControl(EditMode.HIGHLIGHTS, "Highlights"));
        }
        if (btnShadows != null) {
            btnShadows.setOnClickListener(v -> showAdjustmentControl(EditMode.SHADOWS, "Shadows"));
        }
        if (btnSharpness != null) {
            btnSharpness.setOnClickListener(v -> showAdjustmentControl(EditMode.SHARPNESS, "Sharpness"));
        }

        // Tool buttons - with null checks
        if (btnRotate != null) {
            btnRotate.setOnClickListener(v -> rotateImage());
        }
        if (btnFlip != null) {
            btnFlip.setOnClickListener(v -> flipImage());
        }
        if (btnCrop != null) {
            btnCrop.setOnClickListener(v -> showCropDialog());
        }
        if (btnUndo != null) {
            btnUndo.setOnClickListener(v -> undo());
        }
        if (btnRedo != null) {
            btnRedo.setOnClickListener(v -> redo());
        }
        if (btnShare != null) {
            btnShare.setOnClickListener(v -> shareImage());
        }

        // Seekbar - with null checks
        if (seekBar != null) {
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser && seekBarValue != null) {
                        int value = progress - 50;
                        seekBarValue.setText(String.valueOf(value));
                        applyAdjustment(progress);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    // Always use original bitmap as baseline for cumulative adjustments
                    if (originalBitmap != null) {
                        baselineBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
                    }
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    saveToUndoStack();
                    baselineBitmap = null;
                }
            });
        }

        // Compare gesture - with null checks
        if (imageView != null) {
            imageView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            showOriginal();
                            return true;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            hideOriginal();
                            return true;
                    }
                    return false;
                }
            });
        }
    }

    private void setupFilters() {
        if (filtersRecyclerView == null) {
            Log.e("EditorActivity", "filtersRecyclerView is NULL, skipping filter setup");
            return;
        }

        try {
            List<String> filterNames = new ArrayList<>();
            filterNames.add("Original");
            filterNames.add("Grayscale");
            filterNames.add("Sepia");
            filterNames.add("Vintage");
            filterNames.add("Cool");
            filterNames.add("Warm");
            filterNames.add("High Contrast");
            filterNames.add("Soft");
            filterNames.add("Invert");
            filterNames.add("Vignette");

            filterAdapter = new FilterAdapter(this, filterNames, new FilterAdapter.OnFilterClickListener() {
                @Override
                public void onFilterClick(int position) {
                    saveToUndoStack();
                    applyFilter(position);
                }
            });

            filtersRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            filtersRecyclerView.setAdapter(filterAdapter);

            Log.d("EditorActivity", "Filters setup completed successfully");
        } catch (Exception e) {
            Log.e("EditorActivity", "Error in setupFilters: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupIconTints() {
        try {
            // Top bar icons
            if (btnBack != null) {
                androidx.core.widget.ImageViewCompat.setImageTintList(btnBack,
                        android.content.res.ColorStateList.valueOf(Color.parseColor("#FFFFFF")));
            }
            if (btnCompare != null) {
                androidx.core.widget.ImageViewCompat.setImageTintList(btnCompare,
                        android.content.res.ColorStateList.valueOf(Color.parseColor("#8E8E93")));
            }
            if (btnSave != null) {
                androidx.core.widget.ImageViewCompat.setImageTintList(btnSave,
                        android.content.res.ColorStateList.valueOf(Color.parseColor("#007AFF")));
            }

            // Adjust icons
            ImageView iconBrightness = findViewById(R.id.iconBrightness);
            ImageView iconContrast = findViewById(R.id.iconContrast);
            ImageView iconSaturation = findViewById(R.id.iconSaturation);
            ImageView iconHighlights = findViewById(R.id.iconHighlights);
            ImageView iconShadows = findViewById(R.id.iconShadows);
            ImageView iconSharpness = findViewById(R.id.iconSharpness);

            int grayColor = Color.parseColor("#8E8E93");

            if (iconBrightness != null) {
                androidx.core.widget.ImageViewCompat.setImageTintList(iconBrightness,
                        android.content.res.ColorStateList.valueOf(grayColor));
            }
            if (iconContrast != null) {
                androidx.core.widget.ImageViewCompat.setImageTintList(iconContrast,
                        android.content.res.ColorStateList.valueOf(grayColor));
            }
            if (iconSaturation != null) {
                androidx.core.widget.ImageViewCompat.setImageTintList(iconSaturation,
                        android.content.res.ColorStateList.valueOf(grayColor));
            }
            if (iconHighlights != null) {
                androidx.core.widget.ImageViewCompat.setImageTintList(iconHighlights,
                        android.content.res.ColorStateList.valueOf(grayColor));
            }
            if (iconShadows != null) {
                androidx.core.widget.ImageViewCompat.setImageTintList(iconShadows,
                        android.content.res.ColorStateList.valueOf(grayColor));
            }
            if (iconSharpness != null) {
                androidx.core.widget.ImageViewCompat.setImageTintList(iconSharpness,
                        android.content.res.ColorStateList.valueOf(grayColor));
            }

            // Tool icons
            ImageView iconCrop = findViewById(R.id.iconCrop);
            ImageView iconRotate = findViewById(R.id.iconRotate);
            ImageView iconFlip = findViewById(R.id.iconFlip);
            ImageView iconUndo = findViewById(R.id.iconUndo);
            ImageView iconRedo = findViewById(R.id.iconRedo);
            ImageView iconShare = findViewById(R.id.iconShare);

            if (iconCrop != null) {
                androidx.core.widget.ImageViewCompat.setImageTintList(iconCrop,
                        android.content.res.ColorStateList.valueOf(grayColor));
            }
            if (iconRotate != null) {
                androidx.core.widget.ImageViewCompat.setImageTintList(iconRotate,
                        android.content.res.ColorStateList.valueOf(grayColor));
            }
            if (iconFlip != null) {
                androidx.core.widget.ImageViewCompat.setImageTintList(iconFlip,
                        android.content.res.ColorStateList.valueOf(grayColor));
            }
            if (iconUndo != null) {
                androidx.core.widget.ImageViewCompat.setImageTintList(iconUndo,
                        android.content.res.ColorStateList.valueOf(grayColor));
            }
            if (iconRedo != null) {
                androidx.core.widget.ImageViewCompat.setImageTintList(iconRedo,
                        android.content.res.ColorStateList.valueOf(grayColor));
            }
            if (iconShare != null) {
                androidx.core.widget.ImageViewCompat.setImageTintList(iconShare,
                        android.content.res.ColorStateList.valueOf(grayColor));
            }
        } catch (Exception e) {
            Log.e("EditorActivity", "Error setting icon tints: " + e.getMessage());
        }
    }

    private void setupData() {
        Log.d("EditorActivity", "Starting setupData()");

        // Method 1: Try getData() - most common way
        imageUri = getIntent().getData();
        Log.d("EditorActivity", "Method 1 - getData(): " + imageUri);

        // Method 2: Check extras with key "IMAGE_URI"
        if (imageUri == null) {
            String uriString = getIntent().getStringExtra("IMAGE_URI");
            if (uriString != null && !uriString.isEmpty()) {
                imageUri = Uri.parse(uriString);
                Log.d("EditorActivity", "Method 2 - IMAGE_URI extra: " + imageUri);
            }
        }

        // Method 3: Check EXTRA_STREAM (common for sharing)
        if (imageUri == null) {
            imageUri = getIntent().getParcelableExtra(Intent.EXTRA_STREAM);
            Log.d("EditorActivity", "Method 3 - EXTRA_STREAM: " + imageUri);
        }

        // Method 4: Check if path was passed
        if (imageUri == null) {
            String imagePath = getIntent().getStringExtra("IMAGE_PATH");
            if (imagePath != null && !imagePath.isEmpty()) {
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    imageUri = Uri.fromFile(imageFile);
                    Log.d("EditorActivity", "Method 4 - IMAGE_PATH: " + imageUri);
                }
            }
        }

        // Method 5: Check "image_uri" (lowercase)
        if (imageUri == null) {
            String uriString = getIntent().getStringExtra("image_uri");
            if (uriString != null && !uriString.isEmpty()) {
                imageUri = Uri.parse(uriString);
                Log.d("EditorActivity", "Method 5 - image_uri extra: " + imageUri);
            }
        }

        // Method 6: Check "imageUrl" (from PhotoAdapter)
        if (imageUri == null) {
            String imageUrl = getIntent().getStringExtra("imageUrl");
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Log.d("EditorActivity", "Method 6 - imageUrl (remote): " + imageUrl);
                Toast.makeText(this, "Loading image from server...", Toast.LENGTH_SHORT).show();
                downloadAndLoadImage(imageUrl);
                return;
            }
        }

        Log.d("EditorActivity", "Final Image URI: " + imageUri);

        if (imageUri != null) {
            loadImage(imageUri);
        } else {
            Toast.makeText(this, "No image provided. Please select an image first.", Toast.LENGTH_LONG).show();
            Log.e("EditorActivity", "No image URI found in Intent.");
            finish();
        }
    }

    private void downloadAndLoadImage(String imageUrl) {
        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL(imageUrl);
                java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();

                InputStream input = connection.getInputStream();
                originalBitmap = BitmapFactory.decodeStream(input);
                input.close();

                runOnUiThread(() -> {
                    if (originalBitmap != null) {
                        currentBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
                        if (imageView != null) {
                            imageView.setImageBitmap(currentBitmap);
                        }
                        if (imageViewOriginal != null) {
                            imageViewOriginal.setImageBitmap(originalBitmap);
                        }
                        saveToUndoStack();
                        Log.d("EditorActivity", "Remote image loaded successfully");
                    } else {
                        Toast.makeText(this, "Failed to load remote image", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            } catch (Exception e) {
                Log.e("EditorActivity", "Error downloading image: " + e.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error loading image from server", Toast.LENGTH_LONG).show();
                    finish();
                });
            }
        }).start();
    }

    private void loadImage(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            originalBitmap = BitmapFactory.decodeStream(inputStream);
            if (originalBitmap != null) {
                currentBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
                if (imageView != null) {
                    imageView.setImageBitmap(currentBitmap);
                }
                if (imageViewOriginal != null) {
                    imageViewOriginal.setImageBitmap(originalBitmap);
                }
                saveToUndoStack();
                Log.d("EditorActivity", "Image loaded successfully");
            } else {
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (Exception e) {
            Log.e("EditorActivity", "Error loading image: " + e.getMessage());
            Toast.makeText(this, "Error loading image: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void switchTab(int tabIndex) {
        Log.d("EditorActivity", "Switching to tab: " + tabIndex);

        // Reset all tabs
        if (tabAdjust != null) {
            tabAdjust.setBackgroundResource(R.drawable.tab_unselected);
            tabAdjust.setTextColor(Color.parseColor("#8E8E93"));
        }
        if (tabFilters != null) {
            tabFilters.setBackgroundResource(R.drawable.tab_unselected);
            tabFilters.setTextColor(Color.parseColor("#8E8E93"));
        }
        if (tabTools != null) {
            tabTools.setBackgroundResource(R.drawable.tab_unselected);
            tabTools.setTextColor(Color.parseColor("#8E8E93"));
        }
        if (tabEffects != null) {
            tabEffects.setBackgroundResource(R.drawable.tab_unselected);
            tabEffects.setTextColor(Color.parseColor("#8E8E93"));
        }

        // Hide all content
        if (adjustContent != null) {
            adjustContent.setVisibility(View.GONE);
        }
        if (filtersRecyclerView != null) {
            filtersRecyclerView.setVisibility(View.GONE);
        }
        if (toolsContent != null) {
            toolsContent.setVisibility(View.GONE);
        }
        if (effectsContent != null) {
            effectsContent.setVisibility(View.GONE);
        }
        hideSeekBar();

        // Show selected tab
        switch (tabIndex) {
            case 0: // Adjust
                if (tabAdjust != null) {
                    tabAdjust.setBackgroundResource(R.drawable.tab_selected);
                    tabAdjust.setTextColor(Color.WHITE);
                }
                if (adjustContent != null) {
                    adjustContent.setVisibility(View.VISIBLE);
                }
                break;

            case 1: // Filters
                if (tabFilters != null) {
                    tabFilters.setBackgroundResource(R.drawable.tab_selected);
                    tabFilters.setTextColor(Color.WHITE);
                }
                if (filtersRecyclerView != null) {
                    filtersRecyclerView.setVisibility(View.VISIBLE);
                    if (originalBitmap != null && filterAdapter != null) {
                        filterAdapter.setOriginalBitmap(originalBitmap);
                    }
                }
                break;

            case 2: // Tools
                if (tabTools != null) {
                    tabTools.setBackgroundResource(R.drawable.tab_selected);
                    tabTools.setTextColor(Color.WHITE);
                }
                if (toolsContent != null) {
                    toolsContent.setVisibility(View.VISIBLE);
                }
                break;

            case 3: // Effects
                if (tabEffects != null) {
                    tabEffects.setBackgroundResource(R.drawable.tab_selected);
                    tabEffects.setTextColor(Color.WHITE);
                }
                if (effectsContent != null) {
                    effectsContent.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    private void showAdjustmentControl(EditMode mode, String label) {
        currentEditMode = mode;

        if (seekBarLabel != null) {
            seekBarLabel.setText(label);
        }

        // Restore previous value for this adjustment
        int savedProgress = adjustmentValues.getOrDefault(mode, 50);

        if (seekBar != null) {
            seekBar.setProgress(savedProgress);
        }

        if (seekBarValue != null) {
            int displayValue = savedProgress - 50;
            seekBarValue.setText(String.valueOf(displayValue));
        }

        if (seekBarContainer != null) {
            seekBarContainer.setVisibility(View.VISIBLE);
        }

        Log.d("EditorActivity", "Showing " + label + " with saved value: " + (savedProgress - 50));
    }

    private void hideSeekBar() {
        if (seekBarContainer != null) {
            seekBarContainer.setVisibility(View.GONE);
        }
        currentEditMode = EditMode.NONE;
        baselineBitmap = null;
    }

    private void applyAdjustment(int progress) {
        if (originalBitmap == null || imageView == null) return;

        // Save the current adjustment value
        adjustmentValues.put(currentEditMode, progress);

        // Apply ALL adjustments cumulatively from original bitmap
        Bitmap result = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);

        // Apply brightness
        int brightnessValue = adjustmentValues.getOrDefault(EditMode.BRIGHTNESS, 50);
        if (brightnessValue != 50) {
            float value = (brightnessValue - 50) / 50f;
            result = adjustBrightness(result, value);
        }

        // Apply contrast
        int contrastValue = adjustmentValues.getOrDefault(EditMode.CONTRAST, 50);
        if (contrastValue != 50) {
            float value = (contrastValue - 50) / 50f;
            result = adjustContrast(result, value);
        }

        // Apply saturation
        int saturationValue = adjustmentValues.getOrDefault(EditMode.SATURATION, 50);
        if (saturationValue != 50) {
            float value = (saturationValue - 50) / 50f;
            result = adjustSaturation(result, value);
        }

        // Apply highlights
        int highlightsValue = adjustmentValues.getOrDefault(EditMode.HIGHLIGHTS, 50);
        if (highlightsValue != 50) {
            float value = (highlightsValue - 50) / 50f;
            result = adjustHighlights(result, value);
        }

        // Apply shadows
        int shadowsValue = adjustmentValues.getOrDefault(EditMode.SHADOWS, 50);
        if (shadowsValue != 50) {
            float value = (shadowsValue - 50) / 50f;
            result = adjustShadows(result, value);
        }

        // Apply sharpness
        int sharpnessValue = adjustmentValues.getOrDefault(EditMode.SHARPNESS, 50);
        if (sharpnessValue != 50) {
            float value = (sharpnessValue - 50) / 50f;
            result = adjustSharpness(result, value);
        }

        currentBitmap = result;
        imageView.setImageBitmap(currentBitmap);

        Log.d("EditorActivity", "Applied cumulative adjustments - B:" + (brightnessValue-50) +
                " C:" + (contrastValue-50) + " S:" + (saturationValue-50));
    }

    private Bitmap adjustBrightness(Bitmap bitmap, float value) {
        ColorMatrix cm = new ColorMatrix();
        cm.set(new float[]{
                1, 0, 0, 0, value * 255,
                0, 1, 0, 0, value * 255,
                0, 0, 1, 0, value * 255,
                0, 0, 0, 1, 0
        });
        return applyColorMatrix(bitmap, cm);
    }

    private Bitmap adjustContrast(Bitmap bitmap, float value) {
        float contrast = value + 1;
        float translate = (-.5f * contrast + .5f) * 255.f;
        ColorMatrix cm = new ColorMatrix();
        cm.set(new float[]{
                contrast, 0, 0, 0, translate,
                0, contrast, 0, 0, translate,
                0, 0, contrast, 0, translate,
                0, 0, 0, 1, 0
        });
        return applyColorMatrix(bitmap, cm);
    }

    private Bitmap adjustSaturation(Bitmap bitmap, float value) {
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(1 + value);
        return applyColorMatrix(bitmap, cm);
    }

    private Bitmap adjustHighlights(Bitmap bitmap, float value) {
        return adjustBrightness(bitmap, value * 0.5f);
    }

    private Bitmap adjustShadows(Bitmap bitmap, float value) {
        return adjustBrightness(bitmap, value * 0.5f);
    }

    private Bitmap adjustSharpness(Bitmap bitmap, float value) {
        return bitmap;
    }

    private Bitmap applyColorMatrix(Bitmap bitmap, ColorMatrix colorMatrix) {
        Bitmap result = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return result;
    }

    private void applyFilter(int filterIndex) {
        if (originalBitmap == null || imageView == null) return;

        Bitmap filtered = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);

        switch (filterIndex) {
            case 0: // Original
                filtered = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
                break;
            case 1: // Grayscale
                filtered = applyGrayscale(filtered);
                break;
            case 2: // Sepia
                filtered = applySepia(filtered);
                break;
            case 3: // Vintage
                filtered = applyVintage(filtered);
                break;
            case 4: // Cool
                filtered = applyCool(filtered);
                break;
            case 5: // Warm
                filtered = applyWarm(filtered);
                break;
            case 6: // High Contrast
                filtered = adjustContrast(filtered, 0.5f);
                break;
            case 7: // Soft
                filtered = adjustContrast(filtered, -0.3f);
                break;
            case 8: // Invert
                filtered = applyInvert(filtered);
                break;
            case 9: // Vignette
                filtered = applyVignette(filtered);
                break;
        }

        currentBitmap = filtered;
        imageView.setImageBitmap(currentBitmap);
    }

    private Bitmap applyGrayscale(Bitmap bitmap) {
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        return applyColorMatrix(bitmap, cm);
    }

    private Bitmap applySepia(Bitmap bitmap) {
        ColorMatrix cm = new ColorMatrix();
        cm.set(new float[]{
                0.393f, 0.769f, 0.189f, 0, 0,
                0.349f, 0.686f, 0.168f, 0, 0,
                0.272f, 0.534f, 0.131f, 0, 0,
                0, 0, 0, 1, 0
        });
        return applyColorMatrix(bitmap, cm);
    }

    private Bitmap applyVintage(Bitmap bitmap) {
        Bitmap result = applySepia(bitmap);
        return adjustContrast(result, -0.2f);
    }

    private Bitmap applyCool(Bitmap bitmap) {
        ColorMatrix cm = new ColorMatrix();
        cm.set(new float[]{
                0.9f, 0, 0, 0, 0,
                0, 0.9f, 0, 0, 0,
                0, 0, 1.1f, 0, 0,
                0, 0, 0, 1, 0
        });
        return applyColorMatrix(bitmap, cm);
    }

    private Bitmap applyWarm(Bitmap bitmap) {
        ColorMatrix cm = new ColorMatrix();
        cm.set(new float[]{
                1.1f, 0, 0, 0, 0,
                0, 1.0f, 0, 0, 0,
                0, 0, 0.9f, 0, 0,
                0, 0, 0, 1, 0
        });
        return applyColorMatrix(bitmap, cm);
    }

    private Bitmap applyInvert(Bitmap bitmap) {
        ColorMatrix cm = new ColorMatrix();
        cm.set(new float[]{
                -1, 0, 0, 0, 255,
                0, -1, 0, 0, 255,
                0, 0, -1, 0, 255,
                0, 0, 0, 1, 0
        });
        return applyColorMatrix(bitmap, cm);
    }

    private Bitmap applyVignette(Bitmap bitmap) {
        return bitmap;
    }

    private void rotateImage() {
        if (currentBitmap == null) return;
        saveToUndoStack();
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        currentBitmap = Bitmap.createBitmap(currentBitmap, 0, 0,
                currentBitmap.getWidth(), currentBitmap.getHeight(), matrix, true);
        if (imageView != null) {
            imageView.setImageBitmap(currentBitmap);
        }
        // Update original bitmap for future adjustments
        originalBitmap = currentBitmap.copy(Bitmap.Config.ARGB_8888, true);
    }

    private void flipImage() {
        if (currentBitmap == null) return;
        saveToUndoStack();
        Matrix matrix = new Matrix();
        matrix.preScale(-1, 1);
        currentBitmap = Bitmap.createBitmap(currentBitmap, 0, 0,
                currentBitmap.getWidth(), currentBitmap.getHeight(), matrix, true);
        if (imageView != null) {
            imageView.setImageBitmap(currentBitmap);
        }
        // Update original bitmap for future adjustments
        originalBitmap = currentBitmap.copy(Bitmap.Config.ARGB_8888, true);
    }

    private void showCropDialog() {
        Toast.makeText(this, "Crop feature coming soon", Toast.LENGTH_SHORT).show();
    }

    private void undo() {
        if (!undoStack.isEmpty()) {
            redoStack.push(currentBitmap);
            currentBitmap = undoStack.pop();
            if (imageView != null) {
                imageView.setImageBitmap(currentBitmap);
            }
            // Reset adjustment values when undoing
            adjustmentValues.put(EditMode.BRIGHTNESS, 50);
            adjustmentValues.put(EditMode.CONTRAST, 50);
            adjustmentValues.put(EditMode.SATURATION, 50);
            adjustmentValues.put(EditMode.HIGHLIGHTS, 50);
            adjustmentValues.put(EditMode.SHADOWS, 50);
            adjustmentValues.put(EditMode.SHARPNESS, 50);
        }
    }

    private void redo() {
        if (!redoStack.isEmpty()) {
            undoStack.push(currentBitmap);
            currentBitmap = redoStack.pop();
            if (imageView != null) {
                imageView.setImageBitmap(currentBitmap);
            }
        }
    }

    private void saveToUndoStack() {
        if (currentBitmap != null) {
            undoStack.push(currentBitmap.copy(Bitmap.Config.ARGB_8888, true));
            redoStack.clear();
        }
    }

    private void toggleCompare() {
        if (imageViewOriginal != null) {
            if (imageViewOriginal.getVisibility() == View.VISIBLE) {
                imageViewOriginal.setVisibility(View.GONE);
            } else {
                imageViewOriginal.setVisibility(View.VISIBLE);
            }
        }
    }

    private void showOriginal() {
        if (imageViewOriginal != null) {
            imageViewOriginal.setVisibility(View.VISIBLE);
        }
    }

    private void hideOriginal() {
        if (imageViewOriginal != null) {
            imageViewOriginal.setVisibility(View.GONE);
        }
    }

    private void saveImage() {
        if (currentBitmap == null) {
            Toast.makeText(this, "No image to save", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            File cacheDir = new File(getCacheDir(), "images");
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }

            File imageFile = new File(cacheDir, "edited_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream fos = new FileOutputStream(imageFile);
            currentBitmap.compress(Bitmap.CompressFormat.JPEG, 95, fos);
            fos.flush();
            fos.close();

            Toast.makeText(this, "Image saved successfully", Toast.LENGTH_SHORT).show();

            Intent resultIntent = new Intent();
            resultIntent.setData(Uri.fromFile(imageFile));
            setResult(RESULT_OK, resultIntent);
            finish();
        } catch (IOException e) {
            Log.e("EditorActivity", "Error saving image: " + e.getMessage());
            Toast.makeText(this, "Error saving image", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareImage() {
        if (currentBitmap == null) {
            Toast.makeText(this, "No image to share", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            File cacheDir = new File(getCacheDir(), "images");
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }

            File imageFile = new File(cacheDir, "share_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream fos = new FileOutputStream(imageFile);
            currentBitmap.compress(Bitmap.CompressFormat.JPEG, 95, fos);
            fos.flush();
            fos.close();

            Uri imageUri = FileProvider.getUriForFile(this,
                    getPackageName() + ".fileprovider", imageFile);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/jpeg");
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Share Image"));
        } catch (IOException e) {
            Log.e("EditorActivity", "Error sharing image: " + e.getMessage());
            Toast.makeText(this, "Error sharing image", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Dismiss any open dialogs to prevent WindowLeaked error
        if (resumeDialog != null && resumeDialog.isShowing()) {
            try {
                resumeDialog.dismiss();
            } catch (Exception e) {
                Log.e("EditorActivity", "Error dismissing dialog: " + e.getMessage());
            }
        }

        // Clean up bitmaps
        if (originalBitmap != null && !originalBitmap.isRecycled()) {
            originalBitmap.recycle();
        }
        if (currentBitmap != null && !currentBitmap.isRecycled()) {
            currentBitmap.recycle();
        }
        if (baselineBitmap != null && !baselineBitmap.isRecycled()) {
            baselineBitmap.recycle();
        }
    }

    @Override
    public void onBackPressed() {
        if (undoStack.size() > 1) {
            new AlertDialog.Builder(this)
                    .setTitle("Discard Changes?")
                    .setMessage("You have unsaved changes. Are you sure you want to exit?")
                    .setPositiveButton("Discard", (dialog, which) -> {
                        super.onBackPressed();
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } else {
            super.onBackPressed();
            finish();
        }
    }
}