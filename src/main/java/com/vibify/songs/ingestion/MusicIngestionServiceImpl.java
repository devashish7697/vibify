package com.vibify.songs.ingestion;

import com.vibify.songs.repository.SongRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Service
public class MusicIngestionServiceImpl implements MusicIngestionService {

    private static final Logger logger =
            LoggerFactory.getLogger(MusicIngestionServiceImpl.class);

    private static final String TEMP_UPLOAD_DIR =
            System.getProperty("java.io.tmpdir") + "/vibify/uploads";

    @Value("${r2.publicUrl}")
    private String publicUrl;

    private static final int MAX_SONGS_PER_UPLOAD = 300;

    private final ExecutorService executorService;
    private final ZipExtractor zipExtractor;
    private final MetadataExtractor metadataExtractor;
    private final HlsConverter hlsConverter;
    private final R2Uploader r2Uploader;
    private final SongRepository songRepository;

    public MusicIngestionServiceImpl(
            ExecutorService ingestionExecutor,
            ZipExtractor zipExtractor,
            MetadataExtractor metadataExtractor,
            HlsConverter hlsConverter,
            R2Uploader r2Uploader,
            SongRepository songRepository
    ) {
        this.executorService = ingestionExecutor;
        this.zipExtractor = zipExtractor;
        this.metadataExtractor = metadataExtractor;
        this.hlsConverter = hlsConverter;
        this.r2Uploader = r2Uploader;
        this.songRepository = songRepository;
    }

    @Override
    public void ingestSongs(MultipartFile zipFile) {

        logger.info("Starting music ingestion process");

        validateZipFile(zipFile);

        String jobId = UUID.randomUUID().toString();
        logger.info("Generated ingestion jobId: {}", jobId);

        Path jobDirectory = createJobDirectory(jobId);
        logger.info("Created job directory: {}", jobDirectory);

        Path zipPath = saveZipFile(zipFile, jobDirectory);
        logger.info("ZIP file stored at: {}", zipPath);

        List<Path> mp3Files = zipExtractor.extract(zipPath, jobDirectory);

        logger.info("ZIP extraction completed. MP3 files found: {}", mp3Files.size());

        if (mp3Files.size() > MAX_SONGS_PER_UPLOAD) {
            throw new IllegalArgumentException(
                    "Upload limit exceeded. Maximum allowed songs: " + MAX_SONGS_PER_UPLOAD
            );
        }

        logger.info("Dispatching {} songs for parallel processing", mp3Files.size());

        for (Path mp3 : mp3Files) {
            logger.debug("MP3 file ready for processing: {}", mp3.getFileName());

            SongProcessingWorker worker =
                    new SongProcessingWorker(
                            mp3,
                            metadataExtractor,
                            hlsConverter,
                            r2Uploader,
                            songRepository,
                            publicUrl
                    );

            executorService.submit(worker);
        }

        logger.info("Music ingestion initialization completed for job: {}", jobId);
    }



    private void validateZipFile(MultipartFile zipFile) {

        if (zipFile == null || zipFile.isEmpty()) {
            logger.error("Uploaded ZIP file is empty");
            throw new IllegalArgumentException("ZIP file is empty");
        }

        String filename = zipFile.getOriginalFilename();

        if (filename == null || !filename.endsWith(".zip")) {
            logger.error("Invalid file uploaded: {}", filename);
            throw new IllegalArgumentException("Only ZIP files are allowed");
        }

        logger.info("ZIP file validation successful: {}", filename);
    }

    private Path createJobDirectory(String jobId) {

        try {
            Path jobDir = Path.of(TEMP_UPLOAD_DIR, jobId);
            Files.createDirectories(jobDir);
            return jobDir;

        } catch (IOException e) {
            logger.error("Failed to create ingestion directory for job {}", jobId, e);
            throw new RuntimeException("Failed to create ingestion directory", e);
        }
    }

    private Path saveZipFile(MultipartFile zipFile, Path jobDirectory) {

        try {

            Path zipPath = jobDirectory.resolve("songs.zip");

            zipFile.transferTo(zipPath);

            return zipPath;

        } catch (IOException e) {
            logger.error("Failed to store uploaded ZIP file", e);
            throw new RuntimeException("Failed to store uploaded ZIP file", e);
        }
    }


}