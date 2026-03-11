package com.vibify.songs.ingestion;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
public class ZipExtractor {

    private static final int MAX_SONGS_PER_UPLOAD = 300;

    public List<Path> extract(Path zipFilePath, Path jobDirectory) {

        List<Path> mp3Files = new ArrayList<>();

        try (InputStream fis = Files.newInputStream(zipFilePath);
             ZipInputStream zis = new ZipInputStream(fis)) {

            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {

                if (entry.isDirectory()) {
                    continue;
                }

                /*
                 Extract only the file name (removes folders)
                 */

                String filename = Path.of(entry.getName())
                        .getFileName()
                        .toString();

                if (!filename.toLowerCase().endsWith(".mp3")) {
                    continue;
                }

                if (mp3Files.size() >= MAX_SONGS_PER_UPLOAD) {
                    throw new RuntimeException(
                            "Upload limit exceeded. Maximum allowed songs: "
                                    + MAX_SONGS_PER_UPLOAD
                    );
                }

                Path outputPath = jobDirectory.resolve(filename).normalize();

                /*
                 Zip Slip protection
                 */

                if (!outputPath.startsWith(jobDirectory)) {
                    throw new RuntimeException("Invalid ZIP entry detected");
                }

                Files.copy(zis, outputPath, StandardCopyOption.REPLACE_EXISTING);

                mp3Files.add(outputPath);
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to extract ZIP file", e);
        }

        if (mp3Files.isEmpty()) {
            throw new RuntimeException("No MP3 files found in ZIP");
        }

        return mp3Files;
    }
}