package com.nyankostream;

public class Anime {
    private String title;
    private String slug;
    private String poster;
    private String currentEpisode;
    private String releaseDay;
    private String newestReleaseDate;
    private String status;
    private String rating;
    private String genre;


    public Anime(String title, String slug, String poster, String currentEpisode, String releaseDay, String newestReleaseDate) {
        this.currentEpisode = currentEpisode;
        this.releaseDay = releaseDay;
        this.newestReleaseDate = newestReleaseDate;
        this.title = title;
        this.slug = slug;
        this.poster = poster;
        this.status = status;
        this.rating = rating;
        this.genre = genre;

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

    public String getStatus() {
        return status;
    }

    public String getRating() {
        return rating;
    }

    public String getGenre() {
        return genre;
    }

}
