package com.nyankostream;

public class Anime {
    private String title;
    private String slug;
    private String poster;
    private String currentEpisode;
    private String releaseDay;
    private String newestReleaseDate;

    public Anime(String title, String slug, String poster, String currentEpisode, String releaseDay, String newestReleaseDate) {
        this.title = title;
        this.slug = slug;
        this.poster = poster;
        this.currentEpisode = currentEpisode;
        this.releaseDay = releaseDay;
        this.newestReleaseDate = newestReleaseDate;
    }

    public String getTitle() {
        return title;
    }

    public String getSlug() {
        return slug;
    }

    public String getPoster() {
        return poster;
    }

    public String getCurrentEpisode() {
        return currentEpisode;
    }

    public String getReleaseDay() {
        return releaseDay;
    }

    public String getNewestReleaseDate() {
        return newestReleaseDate;
    }
}
