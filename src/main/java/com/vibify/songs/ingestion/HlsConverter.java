package com.vibify.songs.ingestion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Component
public class HlsConverter {

    private static final Logger logger =
            LoggerFactory.getLogger(HlsConverter.class);

    private static final int PROCESS_TIMEOUT_SECONDS = 120;

    public Path convert(Path mp3Path) {

        try {

            Path outputDir = mp3Path
                    .getParent()
                    .resolve("hls_" + System.nanoTime());

            Files.createDirectories(outputDir);

            Path playlist = outputDir.resolve("index.m3u8");

            ProcessBuilder builder = new ProcessBuilder(

                    "ffmpeg",
                    "-i", mp3Path.toAbsolutePath().toString(),
                    "-vn",
                    "-c:a", "aac",
                    "-b:a", "128k",
                    "-hls_time", "4",
                    "-hls_playlist_type", "vod",
                    "-hls_list_size", "0",
                    "-hls_segment_filename",
                    outputDir.resolve("seg_%03d.ts").toString(),
                    playlist.toString()
            );

            builder.redirectErrorStream(true);

            Process process = builder.start();

            BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(process.getInputStream())
                    );

            String line;

            while ((line = reader.readLine()) != null) {

                logger.debug("FFmpeg: {}", line);
            }

            boolean finished =
                    process.waitFor(PROCESS_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            if (!finished) {

                process.destroyForcibly();

                throw new RuntimeException("FFmpeg conversion timeout");
            }

            int exitCode = process.exitValue();

            if (exitCode != 0) {

                throw new RuntimeException("FFmpeg failed with code " + exitCode);
            }

            logger.info("HLS conversion completed for {}", mp3Path.getFileName());

            return outputDir;

        } catch (Exception e) {

            logger.error("HLS conversion failed for {}", mp3Path, e);

            throw new RuntimeException("HLS conversion failed", e);
        }
    }
}
