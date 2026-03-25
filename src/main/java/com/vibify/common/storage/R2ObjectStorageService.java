package com.vibify.common.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

import java.io.InputStream;
@Service
public class R2ObjectStorageService implements ObjectStorageService {

    private static final Logger logger = LoggerFactory.getLogger(R2ObjectStorageService.class);

    private final S3Client s3Client;

    @Value("${r2.bucket}")
    private String bucketName;

    @Value("${r2.publicUrl}")
    private String publicBaseUrl;

    public R2ObjectStorageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    public String uploadFile(
            InputStream inputStream,
            String key,
            String contentType,
            long contentLength
    ) {
        try {

            if (contentLength <= 0) {
                throw new IllegalArgumentException("Invalid content length");
            }

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(contentType)
                    .contentLength(contentLength)
                    .build();


            // 🔥 STREAMING upload (no full memory load)
            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromInputStream(inputStream, contentLength)
            );

            // Return final public URL
            String finalUrl = publicBaseUrl.endsWith("/")
                    ? publicBaseUrl + key
                    : publicBaseUrl + "/" + key;

            logger.info("Upload successful. Key: {}", key);

            return finalUrl;

        } catch (Exception e) {
            logger.error("R2 upload failed for key: {}", key, e);
            throw new RuntimeException("Failed to upload file to R2", e);
        }
    }

    @Override
    public void deleteFile(String key) {
        try {

            logger.info("Deleting file from R2. Key: {}", key);

            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(request);

            logger.info("File deleted successfully from R2. Key: {}", key);

        } catch (Exception e) {
            logger.error("Failed to delete file from R2. Key: {}", key, e);

            throw new RuntimeException("Failed to delete file from R2", e);
        }
    }


}