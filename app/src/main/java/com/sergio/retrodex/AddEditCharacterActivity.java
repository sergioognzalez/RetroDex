package com.sergio.retrodex;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AddEditCharacterActivity extends AppCompatActivity {

    public static final String EXTRA_CHARACTER_ID = "character_id";

    private DatabaseHelper db;
    private Character editCharacter;
    private String selectedImagePath;

    private ImageView ivImage;
    private TextView tvEmoji;
    private TextInputEditText etName;
    private TextInputEditText etDescription;
    private TextInputEditText etOrigin;
    private Spinner spinnerDecade;
    private Spinner spinnerCategory;

    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applySavedTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_character);

        db = new DatabaseHelper(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        bindViews();
        setupImagePicker();
        setupSpinners();

        int id = getIntent().getIntExtra(EXTRA_CHARACTER_ID, -1);
        if (id != -1) {
            editCharacter = db.getById(id);
            populateFields();
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(R.string.title_edit_character);
            }
        } else if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_add_character);
        }

        MaterialButton btnSave = findViewById(R.id.btn_save_character);
        btnSave.setOnClickListener(v -> saveCharacter());
    }

    private void bindViews() {
        ivImage = findViewById(R.id.iv_add_image);
        tvEmoji = findViewById(R.id.tv_add_emoji);
        etName = findViewById(R.id.et_name);
        etDescription = findViewById(R.id.et_description);
        etOrigin = findViewById(R.id.et_origin);
        spinnerDecade = findViewById(R.id.spinner_decade);
        spinnerCategory = findViewById(R.id.spinner_category);

        ImageButton btnPickImage = findViewById(R.id.btn_pick_image);
        btnPickImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        try {
                            selectedImagePath = copyImageToLocal(uri);
                            ivImage.setVisibility(android.view.View.VISIBLE);
                            tvEmoji.setVisibility(android.view.View.GONE);
                            Glide.with(this).load(uri).centerCrop().into(ivImage);
                        } catch (IOException e) {
                            Toast.makeText(this, R.string.toast_image_error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void setupSpinners() {
        String[] decades = getResources().getStringArray(R.array.decades_values);
        ArrayAdapter<String> decadeAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, decades);
        decadeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDecade.setAdapter(decadeAdapter);

        String[] categories = getResources().getStringArray(R.array.categories_values);
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);
    }

    private void populateFields() {
        if (editCharacter == null) return;

        etName.setText(editCharacter.getName());
        etDescription.setText(editCharacter.getDescription());
        etOrigin.setText(editCharacter.getOrigin());

        setSpinnerValue(spinnerDecade,
                getResources().getStringArray(R.array.decades_values),
                editCharacter.getDecade());
        setSpinnerValue(spinnerCategory,
                getResources().getStringArray(R.array.categories_values),
                editCharacter.getCategory());

        selectedImagePath = editCharacter.getImagePath();
        CharacterImageHelper.loadCharacterImage(this, ivImage, tvEmoji, editCharacter);
    }

    private void setSpinnerValue(Spinner spinner, String[] values, String target) {
        if (target == null) return;
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(target)) {
                spinner.setSelection(i);
                return;
            }
        }
    }

    private void saveCharacter() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        if (!CharacterCatalogService.hasRequiredName(name)) {
            Toast.makeText(this, R.string.toast_error_name, Toast.LENGTH_SHORT).show();
            return;
        }

        String description = etDescription.getText() != null
                ? etDescription.getText().toString().trim() : "";
        String origin = etOrigin.getText() != null
                ? etOrigin.getText().toString().trim() : "";
        String decade = (String) spinnerDecade.getSelectedItem();
        String category = (String) spinnerCategory.getSelectedItem();

        if (editCharacter == null) {
            Character character = CharacterCatalogService.createCharacter(
                    name, description, decade, category, origin, selectedImagePath);
            db.insert(character);
            Toast.makeText(this, R.string.toast_character_added, Toast.LENGTH_SHORT).show();
            sendFcmNotification(name);
        } else {
            CharacterCatalogService.updateCharacter(
                    editCharacter, name, description, decade, category, origin, selectedImagePath);
            db.update(editCharacter);
            Toast.makeText(this, R.string.toast_character_updated, Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    private void sendFcmNotification(String characterName) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean notificationsEnabled = prefs.getBoolean("notifications_enabled", true);
        if (!notificationsEnabled) return;

        String title = getString(R.string.notif_new_character_title);
        String body = getString(R.string.notif_new_character_body, characterName);

        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL(getString(R.string.notif_server_url));
                java.net.HttpURLConnection conn =
                        (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                String data = "nombre=" + java.net.URLEncoder.encode(title, "UTF-8")
                        + "&mensaje=" + java.net.URLEncoder.encode(body, "UTF-8");

                OutputStream os = conn.getOutputStream();
                os.write(data.getBytes("UTF-8"));
                os.flush();
                os.close();
                conn.getResponseCode();
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private String copyImageToLocal(Uri sourceUri) throws IOException {
        File imagesDir = new File(getFilesDir(), "character_images");
        if (!imagesDir.exists()) {
            imagesDir.mkdirs();
        }

        String fileName = "img_" + System.currentTimeMillis() + ".jpg";
        File destFile = new File(imagesDir, fileName);

        try (InputStream in = getContentResolver().openInputStream(sourceUri);
             OutputStream out = new FileOutputStream(destFile)) {
            byte[] buf = new byte[4096];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        }

        return destFile.getAbsolutePath();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
