package com.vibify.songs.ingestion;

import com.vibify.songs.model.Song;
import com.vibify.songs.model.SongStatus;
import com.vibify.songs.repository.SongRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;

public class SongProcessingWorker implements Runnable {

    private static final Logger logger =
            LoggerFactory.getLogger(SongProcessingWorker.class);

    private final Path mp3Path;
    private final MetadataExtractor metadataExtractor;
    private final HlsConverter hlsConverter;
    private final R2Uploader r2Uploader;
    private final SongRepository songRepository;
    private final String publicUrl;

    public SongProcessingWorker(
            Path mp3Path,
            MetadataExtractor metadataExtractor,
            HlsConverter hlsConverter,
            R2Uploader r2Uploader,
            SongRepository songRepository,
            String publicUrl
    ) {
        this.mp3Path = mp3Path;
        this.metadataExtractor = metadataExtractor;
        this.hlsConverter = hlsConverter;
        this.r2Uploader = r2Uploader;
        this.songRepository = songRepository;
        this.publicUrl = publicUrl;
    }

    @Override
    public void run() {

        logger.info("Starting processing for {}", mp3Path.getFileName());
        Song song = null;
        Path hlsDirectory = null;
        Path coverImage = null;

        try {

            /*
             STEP 1
             Extract metadata
             */

            SongMetadata metadata = metadataExtractor.extract(mp3Path);
            coverImage = metadata.getCoverImagePath();

            logger.info(
                    "Metadata extracted: {} - {}",
                    metadata.getArtist(),
                    metadata.getTitle()
            );

            /*
             STEP 2
             Create DB record (PROCESSING)
             */

            song = new Song();
            song.setTitle(metadata.getTitle());
            song.setArtist(metadata.getArtist());
            song.setGenre(metadata.getGenre());
            song.setDuration(metadata.getDuration());
            song.setStatus(SongStatus.PROCESSING);

            song = songRepository.save(song);

            logger.info("Created DB record for song id {}", song.getId());

            /*
             STEP 3
             Convert MP3 → HLS
             */

            hlsDirectory = hlsConverter.convert(mp3Path);

            logger.info("HLS conversion completed for song {}", song.getId());

            /*
             STEP 4
             Upload HLS files to R2
             */

            r2Uploader.uploadDirectory(hlsDirectory, song.getId());

            logger.info("Uploaded HLS files to R2 for song {}", song.getId());

            if (coverImage != null) {
                r2Uploader.uploadFile(
                        coverImage,
                        "songs/" + song.getId() + "/cover.jpg"
                );
                song.setCoverImage(
                        publicUrl + "/songs/" + song.getId() + "/cover.jpg"
                );
            }

            /*
             STEP 5
             Update DB record → READY
             */

            String hlsUrl =
                    publicUrl + "/songs/" + song.getId() + "/index.m3u8";

            song.setHlsUrl(hlsUrl);
            song.setStatus(SongStatus.READY);

            songRepository.save(song);

            logger.info("Song {} ingestion completed successfully", song.getId());

        } catch (Exception e) {

            logger.error("Processing failed for {}", mp3Path, e);

            if (song != null) {

                song.setStatus(SongStatus.FAILED);
                song.setProcessingError(e.getMessage());

                songRepository.save(song);
            }
        } finally {
            deleteIfExists(mp3Path);
            deleteIfExists(coverImage);
            deleteDirectory(hlsDirectory);
        }
    }


    // for safe deletation and cleanup
    private void deleteIfExists(Path path) {
        try {
            if (path != null && Files.exists(path)) {
                Files.delete(path);
            }
        } catch (Exception e) {
            logger.warn("Failed to delete temp file {}", path);
        }
    }

    private void deleteDirectory(Path dir) {
        try {
            if (dir == null || !Files.exists(dir)) {
                return;
            }

            try (var paths = Files.walk(dir)) {

                paths.sorted((a, b) -> b.compareTo(a))
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (Exception e) {
                                logger.warn("Failed deleting {}", path);
                            }
                        });
            }
        } catch (Exception e) {
            logger.warn("Directory cleanup failed {}", dir);
        }
    }

}