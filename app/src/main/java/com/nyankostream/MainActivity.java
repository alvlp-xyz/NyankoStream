package com.nyankostream;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import androidx.appcompat.app.AppCompatActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.GridLayoutManager;

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

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AnimeAdapter animeAdapter;
    private ArrayList<Anime> animeList = new ArrayList<>();

    private boolean isLoading = false;
    private int currentPage = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Ensure this matches your XML file name


        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);
        animeAdapter = new AnimeAdapter(animeList, MainActivity.this);
        recyclerView.setAdapter(animeAdapter);

        // Fetch the first page
        fetchAnimeList(currentPage);

        // Add scroll listener for pagination
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (!isLoading && layoutManager != null && layoutManager.findLastVisibleItemPosition() == animeList.size() - 1) {
                    // End of the list has been reached, load next page
                    currentPage++;
                    fetchAnimeList(currentPage);
                    isLoading = true; // Set to true to prevent multiple requests at once
                }
            }
        });
    }

    private void fetchAnimeList(int page) {
        OkHttpClient client = new OkHttpClient();
        String url = "http://maverick.caligo.asia:9044/v2/otakudesu/ongoing-anime/" + page;

        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String jsonData = response.body().string();
                        JSONObject jsonObject = new JSONObject(jsonData);
                        JSONArray dataArray = jsonObject.getJSONArray("data");

                        // Avoid duplicating items by creating a temporary list
                        ArrayList<Anime> newAnimes = new ArrayList<>();

                        for (int i = 0; i < dataArray.length(); i++) {
                            JSONObject animeObject = dataArray.getJSONObject(i);
                            Anime anime = new Anime(
                                    animeObject.getString("title"),
                                    animeObject.getString("slug"),
                                    animeObject.getString("poster"),
                                    animeObject.getString("current_episode"),
                                    animeObject.getString("release_day"),
                                    animeObject.getString("newest_release_date")
                            );
                            newAnimes.add(anime);
                        }

                        runOnUiThread(() -> {
                            animeList.addAll(newAnimes); // Add new data to the list
                            animeAdapter.notifyDataSetChanged(); // Notify adapter for updates
                            isLoading = false; // Reset the loading flag
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
