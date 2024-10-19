package com.nyankostream;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
import java.util.List;

public class DownloadActivity extends AppCompatActivity {

    private TextView episodeTitleTextView;
    private TextView animeTitleTextView, downloadUrlsTextView;
    private Button previousEpisodeButton, nextEpisodeButton;
    private LinearLayout resolutionContainer;
    private String episodeSlug;
    private int episodeNumber;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        // Initialize Views
        episodeTitleTextView = findViewById(R.id.episodeTitleTextView);
        previousEpisodeButton = findViewById(R.id.previousEpisodeButton);
        nextEpisodeButton = findViewById(R.id.nextEpisodeButton);
        resolutionContainer = findViewById(R.id.resolutionContainer);
        downloadUrlsTextView = findViewById(R.id.downloadUrlsTextView);
        Intent intent = getIntent();
        episodeSlug = intent.getStringExtra("episode_slug"); // Get the slug
        Log.d("DownloadActivity", "Episode Slug: " + episodeSlug);

        // Fetch episode details using the episode slug
        fetchEpisodeDetails(episodeSlug);

    }



    private void fetchEpisodeDetails(String episodeSlug) {
        OkHttpClient client = new OkHttpClient();
        String url = "http://maverick.caligo.asia:9044/v2/otakudesu/episode/" + episodeSlug;

        Log.d("DownloadActivity", "Fetching Episode Details from URL: " + url);

        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("DownloadActivity", "Failed to fetch episode details: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(DownloadActivity.this, "Failed to fetch episode details", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseBody);

                        // Extract the 'download_urls' from the JSON response
                        JSONObject dataObject = jsonResponse.getJSONObject("data");
                        JSONObject downloadUrls = dataObject.getJSONObject("download_urls"); // Make sure to reference this correctly

                        // Now you can update the UI based on 'downloadUrls'
                        runOnUiThread(() -> {
                            LinearLayout downloadContainer = findViewById(R.id.downloadContainer);
                            downloadContainer.removeAllViews(); // Clear previous views if needed

                            // Extract formats like mp4, mkv, etc.
                            JSONArray keys = downloadUrls.names();
                            if (keys != null) {
                                for (int k = 0; k < keys.length(); k++) {
                                    String format = keys.optString(k);
                                    JSONArray resolutions = downloadUrls.optJSONArray(format);

                                    if (resolutions != null) {
                                        // Create a TextView for the format (Mp4/Mkv)
                                        TextView formatTextView = new TextView(DownloadActivity.this);
                                        formatTextView.setText(format.toUpperCase()); // Mp4 or Mkv
                                        formatTextView.setTextSize(18);
                                        formatTextView.setTypeface(null, Typeface.BOLD);
                                        downloadContainer.addView(formatTextView);

                                        for (int i = 0; i < resolutions.length(); i++) {
                                            JSONObject resolutionObj = resolutions.optJSONObject(i);
                                            if (resolutionObj != null) {
                                                String resolution = resolutionObj.optString("resolution", "Unknown Resolution");
                                                JSONArray urls = resolutionObj.optJSONArray("urls");

                                                // Create a TextView for the resolution (e.g., 360p)
                                                TextView qualityTextView = new TextView(DownloadActivity.this);
                                                qualityTextView.setText("Quality: " + resolution);
                                                qualityTextView.setPadding(0, 8, 0, 8);
                                                downloadContainer.addView(qualityTextView);

                                                if (urls != null) {
                                                    for (int j = 0; j < urls.length(); j++) {
                                                        JSONObject urlObj = urls.optJSONObject(j);
                                                        if (urlObj != null) {
                                                            String provider = urlObj.optString("provider", "Unknown Provider");
                                                            String downloadUrl = urlObj.optString("url");

                                                            // Create a Button for each provider
                                                            Button providerButton = new Button(DownloadActivity.this);
                                                            providerButton.setText(provider);
                                                            providerButton.setOnClickListener(v -> {
                                                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl));
                                                                startActivity(browserIntent);
                                                            });

                                                            // Add the button to the layout
                                                            downloadContainer.addView(providerButton);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }








        });
    }

    private void addDownloadButtons(JSONObject data) throws JSONException {
        Log.d("DownloadActivity", "Adding download buttons");

        // Clear previous buttons
        resolutionContainer.removeAllViews();

        // Access download_urls from data
        JSONObject downloadUrls = data.optJSONObject("download_urls");
        if (downloadUrls != null) {
            // Create a list for available buttons
            JSONArray allFormats = new JSONArray();

            // Check and add MP4 buttons
            JSONArray mp4Array = downloadUrls.optJSONArray("mp4");
            if (mp4Array != null && mp4Array.length() > 0) {
                for (int i = 0; i < mp4Array.length(); i++) {
                    allFormats.put(mp4Array.getJSONObject(i));
                }
            }

            // Check and add MKV buttons
            JSONArray mkvArray = downloadUrls.optJSONArray("mkv");
            if (mkvArray != null && mkvArray.length() > 0) {
                for (int i = 0; i < mkvArray.length(); i++) {
                    allFormats.put(mkvArray.getJSONObject(i));
                }
            }

            // Shuffle the order of the formats to randomize display
            for (int i = 0; i < allFormats.length(); i++) {
                JSONObject resolutionObject = allFormats.optJSONObject(i);
                if (resolutionObject != null) {
                    String resolution = resolutionObject.optString("resolution", "Unknown");
                    JSONArray urls = resolutionObject.optJSONArray("urls");

                    Log.d("DownloadActivity", "Resolution: " + resolution);

                    // Create a button for this resolution
                    Button resolutionButton = new Button(this);
                    resolutionButton.setText(resolution);
                    resolutionButton.setOnClickListener(v -> {
                        Log.d("DownloadActivity", "Resolution button clicked for: " + resolution);
                        if (urls != null && urls.length() > 0) {
                            try {
                                String providerUrl = urls.getJSONObject(0).optString("url", null);
                                String providerName = urls.getJSONObject(0).optString("provider", "Unknown Provider");

                                // Create a button to display the provider
                                Button providerButton = new Button(this);
                                providerButton.setText("Provider: " + providerName);
                                providerButton.setOnClickListener(v1 -> {
                                    if (providerUrl != null) {
                                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(providerUrl));
                                        startActivity(browserIntent);
                                    } else {
                                        Toast.makeText(DownloadActivity.this, "No valid URL available for this resolution", Toast.LENGTH_SHORT).show();
                                    }
                                });

                                // Add the provider button to the container
                                resolutionContainer.addView(providerButton);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.e("DownloadActivity", "Error fetching provider URL");
                                Toast.makeText(DownloadActivity.this, "Error fetching provider URL", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e("DownloadActivity", "No download URLs available for resolution: " + resolution);
                            Toast.makeText(DownloadActivity.this, "No download URLs available", Toast.LENGTH_SHORT).show();
                        }
                    });

                    // Add the resolution button to the container
                    resolutionContainer.addView(resolutionButton);
                    Log.d("DownloadActivity", "Added button for resolution: " + resolution);
                }
            }
        } else {
            Log.e("DownloadActivity", "Download URLs JSON is null");
        }
    }



}
