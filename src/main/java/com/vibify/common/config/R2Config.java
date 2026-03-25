package com.vibify.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import java.time.Duration;
import software.amazon.awssdk.http.apache.ApacheHttpClient;

import java.net.URI;

@Configuration
public class R2Config {

    @Value("${r2.endpoint}")
    private String endpoint;

    @Value("${r2.accessKey}")
    private String accessKey;

    @Value("${r2.secretKey}")
    private String secretKey;

    @Bean
    public S3Client r2Client() {

        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKey, secretKey)
                        )
                )
                .region(Region.US_EAST_1)// REQUIRED FOR AWS SDK

                // ✅ Required for R2
                .serviceConfiguration(
                        S3Configuration.builder()
                                .pathStyleAccessEnabled(true)
                                .build()
                )

                // ✅ HTTP client tuning (important for large files)
                .httpClientBuilder(
                        ApacheHttpClient.builder()
                                .maxConnections(100)
                                .connectionTimeout(Duration.ofSeconds(30))
                                .socketTimeout(Duration.ofMinutes(10))
                )


                // ✅ Timeout control
                .overrideConfiguration(
                        ClientOverrideConfiguration.builder()
                                .apiCallTimeout(Duration.ofMinutes(10))
                                .apiCallAttemptTimeout(Duration.ofMinutes(10))
                                .build()
                )

                .build();
    }
}
