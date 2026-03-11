package com.vibify.songs.ingestion;

import org.springframework.web.multipart.MultipartFile;

public interface MusicIngestionService {

    void ingestSongs(MultipartFile zipFile);

}
