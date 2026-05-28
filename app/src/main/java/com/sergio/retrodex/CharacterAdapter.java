package com.sergio.retrodex;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class CharacterAdapter extends RecyclerView.Adapter<CharacterAdapter.ViewHolder> {

    public interface OnCharacterActionListener {
        void onEdit(Character character);
        void onDelete(Character character);
        void onShare(Character character);
        void onClick(Character character);
    }

    private final Context context;
    private List<Character> characters;
    private final OnCharacterActionListener listener;

    public CharacterAdapter(Context context, List<Character> characters,
                            OnCharacterActionListener listener) {
        this.context = context;
        this.characters = characters;
        this.listener = listener;
    }

    public void setCharacters(List<Character> characters) {
        this.characters = characters;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_character_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Character character = characters.get(position);

        holder.tvName.setText(character.getName());
        holder.tvDecade.setText(character.getDecade());
        holder.tvOrigin.setText(character.getOrigin());
        holder.tvCategoryBadge.setText(character.getCategory());

        applyCategoryColor(holder.tvCategoryBadge, character.getCategory());
        CharacterImageHelper.loadCharacterImage(context, holder.ivImage, holder.tvEmoji, character);

        holder.card.setOnClickListener(v -> listener.onClick(character));
        holder.card.setOnLongClickListener(v -> {
            showContextMenu(v, character);
            return true;
        });
    }

    private void showContextMenu(View anchor, Character character) {
        PopupMenu popup = new PopupMenu(context, anchor);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_context_character, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.context_edit) {
                listener.onEdit(character);
                return true;
            } else if (id == R.id.context_delete) {
                listener.onDelete(character);
                return true;
            } else if (id == R.id.context_share) {
                listener.onShare(character);
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void applyCategoryColor(TextView badge, String category) {
        badge.setBackgroundResource(R.drawable.bg_chip);
        if (category == null) {
            badge.setTextColor(Color.WHITE);
            return;
        }

        switch (category) {
            case "Videojuego":
                badge.setTextColor(Color.parseColor("#00F5FF"));
                break;
            case "Anime":
                badge.setTextColor(Color.parseColor("#FF2D78"));
                break;
            case "Animación":
                badge.setTextColor(Color.parseColor("#39FF14"));
                break;
            default:
                badge.setTextColor(Color.WHITE);
        }
    }

    @Override
    public int getItemCount() {
        return characters == null ? 0 : characters.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView card;
        ImageView ivImage;
        TextView tvEmoji;
        TextView tvName;
        TextView tvDecade;
        TextView tvOrigin;
        TextView tvCategoryBadge;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.card_character);
            ivImage = itemView.findViewById(R.id.iv_character_image);
            tvEmoji = itemView.findViewById(R.id.tv_emoji_placeholder);
            tvName = itemView.findViewById(R.id.tv_character_name);
            tvDecade = itemView.findViewById(R.id.tv_character_decade);
            tvOrigin = itemView.findViewById(R.id.tv_character_origin);
            tvCategoryBadge = itemView.findViewById(R.id.tv_category_badge);
        }
    }
}
