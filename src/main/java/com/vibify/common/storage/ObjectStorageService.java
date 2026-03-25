package com.vibify.common.storage;

import java.io.InputStream;

public interface ObjectStorageService {

    String uploadFile(
            InputStream inputStream,
            String key,
            String contentType,
            long contentLength
    );

    void deleteFile(String key);

}
