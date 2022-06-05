package com.demo.videostore.service.service;

import com.demo.videostore.service.dto.FileDto;
import com.demo.videostore.service.util.Constant;
import io.minio.*;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

@Slf4j
@Service
public class VideoFileService {

    private static final Logger logger = LoggerFactory.getLogger(VideoFileService.class);

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.video.bucket.name}")
    private String bucketName;

    private void createBucket() throws Exception {
        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }
    }

    public void removeObject(String filename) throws ResponseStatusException {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(filename).build());
        } catch (Exception e) {
            log.error("Error occurred when remove object from minio: ", e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Remove " + filename + " from minio failed.");
        }
    }

    private String getPreSignedUrl(String fileId) {
        return Constant.BASE_URL.concat(fileId);
    }

    public FileDto uploadFile(String fileId, File file, FileDto request) {
        try {
            createBucket();
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(request.getFile().getOriginalFilename())
                    .stream(new DataInputStream(new FileInputStream(file)), request.getFile().getSize(), -1)
                    .build());
        } catch (Exception e) {
            log.error("Happened error when upload file: ", e);
        }
        return FileDto.builder()
                .url(getPreSignedUrl(fileId))
                .build();
    }

    public FileDto uploadFile(String fileId, File file, String filename, long size) {
        try {
            createBucket();
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(filename)
                    .stream(new DataInputStream(new FileInputStream(file)), size, -1)
                    .build());
        } catch (Exception e) {
            log.error("Happened error when upload file: ", e);
        }
        return FileDto.builder()
                .url(getPreSignedUrl(fileId))
                .build();
    }

    public InputStream getObject(String filename) {
        InputStream stream;
        try {
            stream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(filename)
                    .build());
        } catch (Exception e) {
            log.error("Happened error when get list objects from minio: ", e);
            return null;
        }

        return stream;
    }
}
