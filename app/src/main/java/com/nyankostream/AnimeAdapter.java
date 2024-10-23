package com.nyankostream;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.GridLayoutManager;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class AnimeAdapter extends RecyclerView.Adapter<AnimeAdapter.AnimeViewHolder> {
    private ArrayList<Anime> animeList;
    private Context context;

    public AnimeAdapter(ArrayList<Anime> animeList, Context context) {
        this.animeList = animeList;
        this.context = context;
    }

    @NonNull
    @Override
    public AnimeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_anime, parent, false);
        return new AnimeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AnimeViewHolder holder, int position) {
        Anime anime = animeList.get(position);
        holder.titleTextView.setText(anime.getTitle());
        holder.currentEpisodeTextView.setText(anime.getCurrentEpisode());
        holder.releaseDayTextView.setText(anime.getReleaseDay());
        holder.newestReleaseDateTextView.setText(anime.getNewestReleaseDate());

        Glide.with(context).load(anime.getPoster()).into(holder.posterImageView);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AnimeDetailActivity.class);
            intent.putExtra("slug", anime.getSlug());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return animeList.size();
    }

    static class AnimeViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, currentEpisodeTextView, releaseDayTextView, newestReleaseDateTextView;
        ImageView posterImageView;

        public AnimeViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            currentEpisodeTextView = itemView.findViewById(R.id.currentEpisodeTextView);
            releaseDayTextView = itemView.findViewById(R.id.releaseDayTextView);
            newestReleaseDateTextView = itemView.findViewById(R.id.newestReleaseDateTextView);
            posterImageView = itemView.findViewById(R.id.posterImageView);
        }
    }
}
