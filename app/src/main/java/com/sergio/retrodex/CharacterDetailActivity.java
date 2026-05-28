package com.sergio.retrodex;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

public class CharacterDetailActivity extends AppCompatActivity {

    public static final String EXTRA_CHARACTER_ID = "character_id";

    private DatabaseHelper db;
    private Character character;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applySavedTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_character_detail);

        Toolbar toolbar = new Toolbar(this); // usamos el AppCompat soporte interno
        // No necesitamos toolbar propio en esta activity porque el layout es ScrollView
        // Habilitamos el botón Up en el action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_detail);
        }

        db = new DatabaseHelper(this);

        int id = getIntent().getIntExtra(EXTRA_CHARACTER_ID, -1);
        if (id == -1) { finish(); return; }

        character = db.getById(id);
        if (character == null) { finish(); return; }

        bindViews();
    }

    private void bindViews() {
        ImageView ivImage     = findViewById(R.id.iv_detail_image);
        TextView tvEmoji      = findViewById(R.id.tv_detail_emoji);
        TextView tvName       = findViewById(R.id.tv_detail_name);
        TextView tvDecade     = findViewById(R.id.tv_detail_decade_chip);
        TextView tvCategory   = findViewById(R.id.tv_detail_category_chip);
        TextView tvOrigin     = findViewById(R.id.tv_detail_origin);
        TextView tvDesc       = findViewById(R.id.tv_detail_description);
        MaterialButton btnEdit   = findViewById(R.id.btn_detail_edit);
        MaterialButton btnDelete = findViewById(R.id.btn_detail_delete);
        MaterialButton btnShare  = findViewById(R.id.btn_detail_share);

        tvName.setText(character.getName());
        tvDecade.setText(character.getDecade());
        tvCategory.setText(character.getCategory());
        tvOrigin.setText(character.getOrigin());
        tvDesc.setText(character.getDescription());

        CharacterImageHelper.loadCharacterImage(this, ivImage, tvEmoji, character);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(character.getName());
        }

        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditCharacterActivity.class);
            intent.putExtra(AddEditCharacterActivity.EXTRA_CHARACTER_ID, character.getId());
            startActivity(intent);
            finish();
        });

        btnDelete.setOnClickListener(v -> {
            String msg = getString(R.string.dialog_delete_message, character.getName());
            new AlertDialog.Builder(this, R.style.RetroDex_Dialog)
                    .setTitle(R.string.dialog_delete_title)
                    .setMessage(msg)
                    .setPositiveButton(R.string.dialog_confirm, (d, w) -> {
                        db.delete(character.getId());
                        Snackbar.make(findViewById(android.R.id.content),
                                R.string.snack_character_deleted,
                                Snackbar.LENGTH_SHORT).show();
                        finish();
                    })
                    .setNegativeButton(R.string.dialog_cancel, null)
                    .show();
        });

        btnShare.setOnClickListener(v -> {
            String text = getString(R.string.share_text,
                    character.getName(), character.getDecade(),
                    character.getCategory(), character.getOrigin());
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_title));
            shareIntent.putExtra(Intent.EXTRA_TEXT, text);
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_title)));
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar por si se editó
        if (character != null) {
            character = db.getById(character.getId());
            if (character != null) bindViews();
        }
    }
}
