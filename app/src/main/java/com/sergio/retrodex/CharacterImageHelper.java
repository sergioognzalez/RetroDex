package com.sergio.retrodex;

import android.content.Context;
import android.content.res.AssetManager;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.io.File;
import java.io.IOException;

public final class CharacterImageHelper {

    private static final String ASSET_PREFIX = "asset:";

    private CharacterImageHelper() {
    }

    public static boolean loadCharacterImage(Context context,
                                             ImageView imageView,
                                             TextView emojiView,
                                             Character character) {
        if (character == null) {
            showEmojiFallback(imageView, emojiView, null);
            return false;
        }

        String imagePath = character.getImagePath();
        if (imagePath != null && !imagePath.isEmpty()) {
            if (imagePath.startsWith(ASSET_PREFIX)) {
                String assetPath = imagePath.substring(ASSET_PREFIX.length());
                if (assetExists(context, assetPath)) {
                    imageView.setVisibility(View.VISIBLE);
                    emojiView.setVisibility(View.GONE);
                    Glide.with(context)
                            .load("file:///android_asset/" + assetPath)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .fitCenter()
                            .into(imageView);
                    return true;
                }
            } else {
                File imgFile = new File(imagePath);
                if (imgFile.exists()) {
                    imageView.setVisibility(View.VISIBLE);
                    emojiView.setVisibility(View.GONE);
                    Glide.with(context)
                            .load(Uri.fromFile(imgFile))
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .fitCenter()
                            .into(imageView);
                    return true;
                }
            }
        }

        showEmojiFallback(imageView, emojiView, character.getCategory());
        return false;
    }

    public static String getPresetToken(String name) {
        if (name == null) return null;

        switch (name) {
            case "Space Invader": return asset("space_invader.png");
            case "Mazinger Z": return asset("mazinger_z.png");
            case "Heidi": return asset("heidi.png");
            case "Speedy Gonzales": return asset("speedy_gonzales.png");
            case "Mario": return asset("mario.png");
            case "Pac-Man": return asset("pac_man.png");
            case "He-Man": return asset("he_man.png");
            case "Link": return asset("link.png");
            case "Mega Man": return asset("mega_man.png");
            case "Sonic": return asset("sonic.png");
            case "Goku": return asset("goku.png");
            case "Bart Simpson": return asset("bart_simpson.png");
            case "Lara Croft": return asset("lara_croft.png");
            case "Ash Ketchum": return asset("ash_ketchum.png");
            case "Master Chief": return asset("master_chief.png");
            case "Naruto": return asset("naruto.png");
            case "Bob Esponja": return asset("bob_esponja.png");
            case "Kratos": return asset("kratos.png");
            case "Shrek": return asset("shrek.png");
            case "Mickey Mouse": return asset("mickey.png");
            case "Scooby-Doo": return asset("scooby.png");
            default: return null;
        }
    }

    private static String asset(String fileName) {
        return ASSET_PREFIX + "preloaded_characters/" + fileName;
    }

    private static boolean assetExists(Context context, String assetPath) {
        AssetManager assetManager = context.getAssets();
        try {
            assetManager.open(assetPath).close();
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }

    private static void showEmojiFallback(ImageView imageView, TextView emojiView, String category) {
        imageView.setVisibility(View.GONE);
        emojiView.setVisibility(View.VISIBLE);
        emojiView.setText(getEmojiForCategory(category));
    }

    private static String getEmojiForCategory(String category) {
        if (category == null) return "★";
        switch (category) {
            case "Videojuego": return "\uD83C\uDFAE";
            case "Anime": return "⚔";
            case "Animación": return "\uD83C\uDFAC";
            default: return "★";
        }
    }
}
