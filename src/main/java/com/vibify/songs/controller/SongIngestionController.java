package com.vibify.songs.controller;

import com.vibify.songs.ingestion.MusicIngestionService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/songs")
public class SongIngestionController {

    private final MusicIngestionService musicIngestionService;

    public SongIngestionController(MusicIngestionService musicIngestionService) {
        this.musicIngestionService = musicIngestionService;
    }

    @PostMapping("/upload")
    public String uploadSongs(@RequestParam("file") MultipartFile file) {
        musicIngestionService.ingestSongs(file);
        return "Upload started";
    }
}
