package com.nyankostream;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout; // Ensure to import LinearLayout
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnimeDetailActivity extends AppCompatActivity {

    private ImageView posterImageView;
    private TextView titleTextView, japaneseTitleTextView, ratingTextView, producerTextView, typeTextView, statusTextView,
            episodeCountTextView, durationTextView, releaseDateTextView, studioTextView, genresTextView, synopsisTextView;
    private LinearLayout episodeListContainer; // Container to hold episode views
    private String animeSlug; // Declare animeSlug here

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anime_detail); // Ensure this matches your XML file name

        // Initialize Views
        posterImageView = findViewById(R.id.posterImageView);
        titleTextView = findViewById(R.id.titleTextView);
        japaneseTitleTextView = findViewById(R.id.japaneseTitleTextView);
        ratingTextView = findViewById(R.id.ratingTextView);
        producerTextView = findViewById(R.id.produserTextView); // Ensure correct ID
        typeTextView = findViewById(R.id.typeTextView);
        statusTextView = findViewById(R.id.statusTextView);
        episodeCountTextView = findViewById(R.id.episodeCountTextView);
        durationTextView = findViewById(R.id.durationTextView);
        releaseDateTextView = findViewById(R.id.releaseDateTextView);
        studioTextView = findViewById(R.id.studioTextView);
        genresTextView = findViewById(R.id.genresTextView);
        synopsisTextView = findViewById(R.id.synopsisTextView);
        episodeListContainer = findViewById(R.id.episodeListContainer); // Initialize the episode list container

        // Get the slug from the Intent
        Intent intent = getIntent();
        String slug = intent.getStringExtra("slug");
        animeSlug = intent.getStringExtra("anime_slug"); // Initialize animeSlug
        // Fetch anime details using the slug
        fetchAnimeDetails(slug);
    }

    private void fetchAnimeDetails(String slug) {
        OkHttpClient client = new OkHttpClient();
        String url = "http://maverick.caligo.asia:9044/v2/otakudesu/anime/" + slug;

        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(AnimeDetailActivity.this, "Failed to fetch anime details", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String jsonData = response.body().string();
                        JSONObject jsonObject = new JSONObject(jsonData);
                        JSONObject data = jsonObject.optJSONObject("data");

                        runOnUiThread(() -> {
                            try {
                                // Set values to views
                                titleTextView.setText(data.optString("title", "N/A"));
                                japaneseTitleTextView.setText(data.optString("japanese_title", "N/A"));
                                ratingTextView.setText(data.optString("rating", "N/A"));
                                producerTextView.setText(data.optString("produser", "N/A"));
                                typeTextView.setText(data.optString("type", "N/A"));
                                statusTextView.setText(data.optString("status", "N/A"));
                                episodeCountTextView.setText(data.optString("episode_count", "N/A"));
                                durationTextView.setText(data.optString("duration", "N/A"));
                                releaseDateTextView.setText(data.optString("release_date", "N/A"));
                                studioTextView.setText(data.optString("studio", "N/A"));
                                genresTextView.setText(getGenres(data.optJSONArray("genres")));
                                synopsisTextView.setText(data.optString("synopsis", "N/A"));

                                Glide.with(AnimeDetailActivity.this)
                                        .load(data.optString("poster", ""))
                                        .into(posterImageView);

                                // After setting the anime details, fetch the episode list
                                fetchEpisodeList(slug);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(AnimeDetailActivity.this, "Error parsing anime details", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }


    private void fetchEpisodeList(String slug) {
        OkHttpClient client = new OkHttpClient();
        String url = "http://maverick.caligo.asia:9044/v2/otakudesu/anime/" + slug;

        Log.d("AnimeDetailActivity", "Fetching episodes from: " + url);

        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("AnimeDetailActivity", "Failed to fetch episode list: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(AnimeDetailActivity.this, "Failed to fetch episode list", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String jsonData = response.body().string();
                        Log.d("AnimeDetailActivity", "API response: " + jsonData);

                        JSONObject jsonObject = new JSONObject(jsonData);
                        JSONObject data = jsonObject.optJSONObject("data");

                        if (data != null) {
                            JSONArray episodeArray = data.optJSONArray("episode_lists");

                            if (episodeArray != null) {
                                List<JSONObject> episodeList = new ArrayList<>();
                                for (int i = 0; i < episodeArray.length(); i++) {
                                    episodeList.add(episodeArray.optJSONObject(i));
                                }

                                // Sort episodes by episode number extracted from the episode title
                                Collections.sort(episodeList, new Comparator<JSONObject>() {
                                    @Override
                                    public int compare(JSONObject o1, JSONObject o2) {
                                        String episode1 = o1.optString("episode", "0").replaceAll("[^0-9]", "");
                                        String episode2 = o2.optString("episode", "0").replaceAll("[^0-9]", "");
                                        return Integer.compare(Integer.parseInt(episode1), Integer.parseInt(episode2));
                                    }
                                });

                                runOnUiThread(() -> {
                                    episodeListContainer.removeAllViews();
                                    for (JSONObject episodeObject : episodeList) {
                                        if (episodeObject != null) {
                                            String episodeTitle = episodeObject.optString("episode", "Unknown Episode");
                                            String episodeSlug = episodeObject.optString("slug", "unknown-slug");

                                            // Create a Button for each episode
                                            Button episodeButton = new Button(AnimeDetailActivity.this);
                                            episodeButton.setText(episodeTitle);

                                            // Set onClickListener for the button
                                            episodeButton.setOnClickListener(v -> {
                                                Intent downloadIntent = new Intent(AnimeDetailActivity.this, DownloadActivity.class);
                                                downloadIntent.putExtra("episode_slug", episodeSlug); // Pass the episode slug
                                                startActivity(downloadIntent);
                                            });


                                            episodeListContainer.addView(episodeButton);
                                        } else {
                                            Log.e("AnimeDetailActivity", "Episode object is null");
                                        }
                                    }
                                });
                            } else {
                                Log.e("AnimeDetailActivity", "Episode list is null");
                            }
                        } else {
                            Log.e("AnimeDetailActivity", "Data object is null");
                        }
                    } catch (JSONException e) {
                        Log.e("AnimeDetailActivity", "JSON parsing error: " + e.getMessage());
                        e.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(AnimeDetailActivity.this, "Error parsing episode data", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    Log.e("AnimeDetailActivity", "API response failed: " + response.message());
                }
            }
        });
    }

// ... (your existing methods)


    // Helper method to extract episode number from slug
    private int extractEpisodeNumber(String slug) {
        // Use regex to find the episode number in the slug
        Pattern pattern = Pattern.compile("episode-(\\d+)");
        Matcher matcher = pattern.matcher(slug);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1)); // Return the episode number
        }
        return 0; // Default value if no number is found
    }



    private String getGenres(JSONArray genresArray) throws JSONException {
        StringBuilder genres = new StringBuilder();
        for (int i = 0; i < genresArray.length(); i++) {
            JSONObject genre = genresArray.getJSONObject(i);
            genres.append(genre.getString("name"));
            if (i < genresArray.length() - 1) {
                genres.append(", ");
            }
        }
        return genres.toString();
    }
}
