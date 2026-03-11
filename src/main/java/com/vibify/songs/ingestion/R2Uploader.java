package com.vibify.songs.ingestion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class R2Uploader {

    private static final Logger logger =
            LoggerFactory.getLogger(R2Uploader.class);

    private final S3Client r2Client;

    @Value("${r2.bucket}")
    private String bucket;

    public R2Uploader(S3Client r2Client) {
        this.r2Client = r2Client;
    }

    public void uploadDirectory(Path directory, Long songId) {

        try {
            Files.list(directory).forEach(file -> {

                String key =
                        "songs/" + songId + "/" + file.getFileName();

                PutObjectRequest request =
                        PutObjectRequest.builder()
                                .bucket(bucket)
                                .key(key)
                                .build();

                r2Client.putObject(
                        request,
                        RequestBody.fromFile(file)
                );

                logger.info("Uploaded {} to R2", key);
            });

        } catch (IOException e) {
            logger.error("Failed uploading HLS directory", e);
            throw new RuntimeException("R2 upload failed", e);
        }
    }

    public void uploadFile(Path file, String key) {

        try {
            PutObjectRequest request =
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .build();

            r2Client.putObject(
                    request,
                    RequestBody.fromFile(file)
            );
            logger.info("Uploaded {} to R2", key);

        } catch (Exception e) {
            logger.error("Failed uploading file {}", key, e);
            throw new RuntimeException("R2 upload failed", e);
        }
    }

}
