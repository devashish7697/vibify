package com.vibify.songs.ingestion;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.datatype.Artwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class MetadataExtractor {

    private static final Logger logger =
            LoggerFactory.getLogger(MetadataExtractor.class);

    public SongMetadata extract(Path mp3Path) {

        try {

            logger.info("Extracting metadata from {}", mp3Path.getFileName());

            AudioFile audioFile = AudioFileIO.read(mp3Path.toFile());

            Tag tag = audioFile.getTag();
            AudioHeader header = audioFile.getAudioHeader();

            String title = null;
            String artist = null;
            String genre = null;

            if (tag != null) {

                title = tag.getFirst(FieldKey.TITLE);
                artist = tag.getFirst(FieldKey.ARTIST);
                genre = tag.getFirst(FieldKey.GENRE);
            }

            int duration = header.getTrackLength();

            /*
             Fallback handling
             */

            if (title == null || title.isBlank()) {
                title = mp3Path.getFileName()
                        .toString()
                        .replace(".mp3", "");

                logger.warn("Missing title metadata for {}, using filename",
                        mp3Path.getFileName());
            }

            if (artist == null || artist.isBlank()) {

                artist = "Unknown Artist";

                logger.warn("Missing artist metadata for {}",
                        mp3Path.getFileName());
            }

            if (genre == null || genre.isBlank()) {

                genre = "Unknown";
            }

            /*
             Extract cover artwork
             */

            Path coverImagePath = null;

            if (tag != null) {

                Artwork artwork = tag.getFirstArtwork();

                if (artwork != null) {

                    byte[] imageData = artwork.getBinaryData();

                    coverImagePath = mp3Path
                            .getParent()
                            .resolve("cover_" + System.nanoTime() + ".jpg");

                    Files.write(coverImagePath, imageData);

                    logger.info("Extracted cover art for {}",
                            mp3Path.getFileName());
                }
            }

            logger.info("Metadata extracted: {} - {} ({}s)",
                    artist,
                    title,
                    duration);

            return new SongMetadata(
                    title,
                    artist,
                    genre,
                    duration,
                    coverImagePath
            );

        } catch (Exception e) {

            logger.error("Failed to extract metadata from {}", mp3Path, e);

            throw new RuntimeException("Metadata extraction failed", e);
        }
    }
}
