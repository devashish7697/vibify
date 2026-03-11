package com.vibify.songs.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SongBulkUploadRequest {

    @NotNull(message = "ZIP file is required")
    private MultipartFile zipFile;

}
