package com.nyankostream;

import java.util.ArrayList;

public class Anime {
    private String title;
    private String slug;
    private String poster;
    private String currentEpisode;
    private String releaseDay;
    private String newestReleaseDate;
    private String status; // Added status
    private String rating; // Added rating
    private ArrayList<String> genres; // Optional if you want to store genres later

    // Updated constructor with status and rating
    public Anime(String title, String slug, String poster, String currentEpisode, String releaseDay, String newestReleaseDate, String status, String rating) {
        this.title = title;
        this.slug = slug;
        this.poster = poster;
        this.currentEpisode = currentEpisode;
        this.releaseDay = releaseDay;
        this.newestReleaseDate = newestReleaseDate;
        this.status = status; // Initialize status
        this.rating = rating; // Initialize rating
    }

    // Getters (no changes needed here)
    public String getTitle() { return title; }
    public String getSlug() { return slug; }
    public String getPoster() { return poster; }
    public String getCurrentEpisode() { return currentEpisode; }
    public String getReleaseDay() { return releaseDay; }
    public String getNewestReleaseDate() { return newestReleaseDate; }
    public String getStatus() { return status; }
    public String getRating() { return rating; }
}
