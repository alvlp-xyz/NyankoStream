package com.nyankostream;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
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

public class DownloadActivity extends AppCompatActivity {

    private TextView episodeTitleTextView;
    private LinearLayout resolutionContainer;
    private String episodeSlug;
    private Button previousEpisodeButton, nextEpisodeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        episodeTitleTextView = findViewById(R.id.episodeTitleTextView);
        resolutionContainer = findViewById(R.id.resolutionContainer);
        previousEpisodeButton = findViewById(R.id.previousEpisodeButton);
        nextEpisodeButton = findViewById(R.id.nextEpisodeButton);

        Intent intent = getIntent();
        episodeSlug = intent.getStringExtra("episode_slug");
        Log.d("DownloadActivity", "Episode Slug: " + episodeSlug);

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
                        JSONObject dataObject = jsonResponse.getJSONObject("data");

                        String episodeTitle = dataObject.getString("episode");

                        boolean hasNext = dataObject.getBoolean("has_next_episode");
                        boolean hasPrevious = dataObject.getBoolean("has_previous_episode");
                        String nextSlug = dataObject.optString("next_episode", null);
                        JSONObject previousEpisode = dataObject.optJSONObject("previous_episode");

                        runOnUiThread(() -> {
                            episodeTitleTextView.setText(episodeTitle);
                            if (hasPrevious && previousEpisode != null) {
                                String previousSlugValue = previousEpisode.optString("slug", null);
                                if (previousSlugValue != null) {
                                    previousEpisodeButton.setEnabled(true);
                                    previousEpisodeButton.setBackgroundColor(getResources().getColor(R.color.enabled_button_color));
                                    previousEpisodeButton.setTextColor(getResources().getColor(R.color.enabled_text_color));
                                    previousEpisodeButton.setOnClickListener(v -> fetchEpisodeDetails(previousSlugValue));
                                }
                            } else {
                                previousEpisodeButton.setEnabled(false);
                                previousEpisodeButton.setBackgroundColor(getResources().getColor(R.color.disabled_button_color));
                                previousEpisodeButton.setTextColor(getResources().getColor(R.color.disabled_text_color));
                            }
                            
                            if (hasNext && nextSlug != null) {
                                nextEpisodeButton.setEnabled(true);
                                nextEpisodeButton.setBackgroundColor(getResources().getColor(R.color.enabled_button_color));
                                nextEpisodeButton.setTextColor(getResources().getColor(R.color.enabled_text_color));
                                nextEpisodeButton.setOnClickListener(v -> fetchEpisodeDetails(nextSlug));
                            } else {
                                nextEpisodeButton.setEnabled(false);
                                nextEpisodeButton.setBackgroundColor(getResources().getColor(R.color.disabled_button_color));
                                nextEpisodeButton.setTextColor(getResources().getColor(R.color.disabled_text_color));
                            }

                            try {
                                JSONObject downloadUrls = dataObject.getJSONObject("download_urls");
                                populateDownloadLinks(downloadUrls);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }



    private void populateDownloadLinks(JSONObject downloadUrls) {
        resolutionContainer.removeAllViews();

        JSONArray keys = downloadUrls.names();
        if (keys != null) {
            for (int k = 0; k < keys.length(); k++) {
                String format = keys.optString(k);
                JSONArray resolutions = downloadUrls.optJSONArray(format);

                if (resolutions != null) {
                    TextView formatTextView = new TextView(this);
                    formatTextView.setText(format.toUpperCase());
                    formatTextView.setTextSize(18);
                    formatTextView.setTypeface(null, Typeface.BOLD);
                    resolutionContainer.addView(formatTextView);

                    for (int i = 0; i < resolutions.length(); i++) {
                        JSONObject resolutionObj = resolutions.optJSONObject(i);
                        if (resolutionObj != null) {
                            String resolution = resolutionObj.optString("resolution", "Unknown Resolution");
                            JSONArray urls = resolutionObj.optJSONArray("urls");

                            TextView qualityTextView = new TextView(this);
                            qualityTextView.setText("Quality: " + resolution);
                            qualityTextView.setPadding(0, 8, 0, 8);
                            resolutionContainer.addView(qualityTextView);

                            if (urls != null) {
                                for (int j = 0; j < urls.length(); j++) {
                                    JSONObject urlObj = urls.optJSONObject(j);
                                    if (urlObj != null) {
                                        String provider = urlObj.optString("provider", "Unknown Provider");
                                        String rawUrl = urlObj.optString("url");

                                        Button providerButton = new Button(this);
                                        providerButton.setText(provider);
                                        providerButton.setOnClickListener(v -> {

                                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(rawUrl));
                                            startActivity(browserIntent);
                                        });

                                        resolutionContainer.addView(providerButton);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    private void resolveUrl(String pdrainUrl, UrlResolvedCallback callback) {
        OkHttpClient client = new OkHttpClient();
        String resolveUrl = "http://maverick.caligo.asia:9044/v2/otakudesu/resolveUri?url=" + Uri.encode(pdrainUrl);

        Request request = new Request.Builder().url(resolveUrl).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("DownloadActivity", "Failed to resolve URL: " + e.getMessage());
                runOnUiThread(() -> callback.onError(e));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        String resolvedUrl = jsonResponse.getString("data");
                        runOnUiThread(() -> callback.onSuccess(resolvedUrl));
                    } catch (JSONException e) {
                        runOnUiThread(() -> callback.onError(e));
                    }
                } else {
                    runOnUiThread(() -> callback.onError(new IOException("Failed to resolve URL with response: " + response)));
                }
            }
        });
    }

    private void openWithExternalDownloadManager(String downloadUrl) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(downloadUrl));
        startActivity(intent);
    }

    public interface UrlResolvedCallback {
        void onSuccess(String resolvedUrl);
        void onError(Exception e);
    }
}
