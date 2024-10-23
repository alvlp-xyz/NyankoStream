package com.nyankostream;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import android.app.AlertDialog;

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
    private LinearLayout episodeListContainer;
    private String animeSlug;
    private Button batchDownloadButton;
    private String batchSlug;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anime_detail);

        // Initialize Views
        posterImageView = findViewById(R.id.posterImageView);
        titleTextView = findViewById(R.id.titleTextView);
        japaneseTitleTextView = findViewById(R.id.japaneseTitleTextView);
        ratingTextView = findViewById(R.id.ratingTextView);
        producerTextView = findViewById(R.id.produserTextView);
        typeTextView = findViewById(R.id.typeTextView);
        statusTextView = findViewById(R.id.statusTextView);
        episodeCountTextView = findViewById(R.id.episodeCountTextView);
        durationTextView = findViewById(R.id.durationTextView);
        releaseDateTextView = findViewById(R.id.releaseDateTextView);
        studioTextView = findViewById(R.id.studioTextView);
        genresTextView = findViewById(R.id.genresTextView);
        synopsisTextView = findViewById(R.id.synopsisTextView);
        episodeListContainer = findViewById(R.id.episodeListContainer);

        batchDownloadButton = findViewById(R.id.batchDownloadButton);
        batchDownloadButton.setVisibility(View.GONE); 

        batchDownloadButton.setOnClickListener(v -> showBatchDownloadDialog(batchSlug));

        Intent intent = getIntent();
        String slug = intent.getStringExtra("slug");
        animeSlug = intent.getStringExtra("anime_slug");

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
                                JSONObject batch = data.optJSONObject("batch");
                                if (batch != null) {
                                    batchSlug = batch.optString("slug", null);
                                    if (batchSlug != null) {
                                        batchDownloadButton.setVisibility(View.VISIBLE);
                                    }
                                }

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

    private void showBatchDownloadDialog(String batchSlug) {
        if (batchSlug == null || batchSlug.isEmpty()) {
            Toast.makeText(this, "No batch download available", Toast.LENGTH_SHORT).show();
            return;
        }

        OkHttpClient client = new OkHttpClient();
        String url = "http://maverick.caligo.asia:9044/v2/otakudesu/batch/" + batchSlug;

        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(AnimeDetailActivity.this, "Failed to fetch batch download", Toast.LENGTH_SHORT).show());
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
                                JSONArray downloadUrls = data.optJSONArray("download_urls");

                                AlertDialog.Builder builder = new AlertDialog.Builder(AnimeDetailActivity.this);
                                builder.setTitle("Download Batch");

                                StringBuilder message = new StringBuilder();
                                for (int i = 0; i < downloadUrls.length(); i++) {
                                    JSONObject downloadOption = downloadUrls.getJSONObject(i);
                                    String resolution = downloadOption.optString("resolution", "Unknown Resolution");
                                    String fileSize = downloadOption.optString("file_size", "Unknown Size");

                                    message.append("Resolution: ").append(resolution)
                                            .append("\nSize: ").append(fileSize)
                                            .append("\n\n");
                                }

                                builder.setMessage(message.toString());

                                builder.setPositiveButton("Download", (dialog, which) -> {
                                    try {
                                        JSONObject firstUrl = downloadUrls.getJSONObject(0).optJSONArray("urls").getJSONObject(0);
                                        String downloadUrl = firstUrl.optString("url", "");
                                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl));
                                        startActivity(intent);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                });

                                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
                                builder.show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(AnimeDetailActivity.this, "Error parsing batch download data", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
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

                                            Button episodeButton = new Button(AnimeDetailActivity.this);
                                            episodeButton.setText(episodeTitle);

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
}






