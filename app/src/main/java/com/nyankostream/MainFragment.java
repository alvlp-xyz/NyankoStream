package com.nyankostream;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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

public class MainFragment extends Fragment {

    private RecyclerView recyclerView;
    private AnimeAdapter animeAdapter;
    private ArrayList<Anime> animeList = new ArrayList<>();
    private boolean isLoading = false;
    private int currentPage = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(gridLayoutManager);

        animeAdapter = new AnimeAdapter(animeList, getContext());
        recyclerView.setAdapter(animeAdapter);

        fetchAnimeList(currentPage);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (!isLoading && layoutManager != null && layoutManager.findLastVisibleItemPosition() == animeList.size() - 1) {
                    currentPage++;
                    fetchAnimeList(currentPage);
                    isLoading = true;
                }
            }
        });

        return view;
    }

    private void fetchAnimeList(int page) {
        OkHttpClient client = new OkHttpClient();
        String url = "http://maverick.caligo.asia:9044/v2/otakudesu/ongoing-anime/" + page;

        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getActivity(), "Failed to load data", Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String jsonData = response.body().string();
                        JSONObject jsonObject = new JSONObject(jsonData);
                        JSONArray dataArray = jsonObject.getJSONArray("data");

                        ArrayList<Anime> newAnimes = new ArrayList<>();

                        for (int i = 0; i < dataArray.length(); i++) {
                            JSONObject animeObject = dataArray.getJSONObject(i);
                            Anime anime = new Anime(
                                    animeObject.getString("title"),
                                    animeObject.getString("slug"),
                                    animeObject.getString("poster"),
                                    animeObject.optString("current_episode", null),
                                    animeObject.optString("release_day", null),
                                    animeObject.optString("newest_release_date", null),
                                    animeObject.optString("status", null),                                    animeObject.optString("rating", null)
                            );
                            newAnimes.add(anime);
                        }


                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                animeList.addAll(newAnimes);
                                animeAdapter.notifyDataSetChanged();
                                isLoading = false;
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
