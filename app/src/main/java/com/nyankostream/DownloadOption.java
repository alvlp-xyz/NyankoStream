package com.nyankostream;

public class DownloadOption {
    private String resolution;
    private String fileSize;
    private String provider;
    private String url;

    public DownloadOption(String resolution, String fileSize, String provider, String url) {
        this.resolution = resolution;
        this.fileSize = fileSize;
        this.provider = provider;
        this.url = url;
    }

    public String getResolution() {
        return resolution;
    }

    public String getFileSize() {
        return fileSize;
    }

    public String getProvider() {
        return provider;
    }

    public String getUrl() {
        return url;
    }
}
