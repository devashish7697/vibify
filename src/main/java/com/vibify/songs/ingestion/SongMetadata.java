package com.vibify.songs.ingestion;

import java.nio.file.Path;

public class SongMetadata {

    private final String title;
    private final String artist;
    private final String genre;
    private final int duration;
    private final Path coverImagePath;

    public SongMetadata(String title,
                        String artist,
                        String genre,
                        int duration,
                        Path coverImagePath) {

        this.title = title;
        this.artist = artist;
        this.genre = genre;
        this.duration = duration;
        this.coverImagePath = coverImagePath;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getGenre() {
        return genre;
    }

    public int getDuration() {
        return duration;
    }

    public Path getCoverImagePath() {
        return coverImagePath;
    }
}