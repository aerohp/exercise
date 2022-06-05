package com.demo.videostore.service.service;

import com.demo.videostore.service.dto.FileDto;
import com.demo.videostore.service.model.Metadata;
import com.demo.videostore.service.util.Constant;
import com.demo.videostore.service.util.VideoConverter;
import com.demo.videostore.service.util.VideoUtil;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.*;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
public class VideoStoreService {

    private static final Logger logger = LoggerFactory.getLogger(VideoStoreService.class);

    @Autowired
    private VideoFileService videoFileService;

    @Autowired
    private MetadataService metadataService;

    public List<Metadata> getFileList() {
        return metadataService.getFileList();
    }

    public List<Metadata> findByFileName(String filename) {
        return metadataService.findByFileName(filename);
    }

    public List<Metadata> findByDuration(int from, int to) {
        return metadataService.findByDuration(from, to);
    }

    public ResponseEntity<Object> getFile(String id) throws IOException {
        Metadata metadata = metadataService.getMetadata(id);
        String filename = metadata.getFilename();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + filename)
                .header("filename", filename)
                .body(IOUtils.toByteArray(videoFileService.getObject(filename)));
    }

    public void removeFile(String id) throws ResponseStatusException {
        Metadata metadata = metadataService.getMetadata(id);
        metadataService.removeMetadata(id);
        videoFileService.removeObject(metadata.getFilename());
    }

    public Metadata uploadFile(FileDto request) throws ResponseStatusException {
        String contentType = request.getFile().getContentType();
        if(!Arrays.stream(Constant.SUPPORTED_CONTENT_TYPE).anyMatch(contentType::equalsIgnoreCase)) {
            logger.info(contentType + " is not supported.");
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, contentType + " is not supported");
        }

        // check if file name exists
        MultipartFile multipartFile = request.getFile();
        String filename = multipartFile.getOriginalFilename();
        if(metadataService.getFileList().stream().filter(m -> m.getFilename().equals(filename))
                .findFirst()
                .orElse(null) != null) {
            logger.info("Current upload file name already exists.");
            throw new ResponseStatusException(HttpStatus.CONFLICT, "File name already exists.");
        }

        // get duration
        long duration = 0;
        File tempFile = null;
        try {
            tempFile = File.createTempFile("video_", ".tmp");
            multipartFile.transferTo(tempFile);
            duration = VideoUtil.getDuration(tempFile.getPath());
        } catch (IOException e) {
            logger.info("Calculate video duration failed.");
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Calculate video duration failed.");
        }

        Metadata metadata = new Metadata();
        metadata = metadataService.createUploadedFile(metadata);
        FileDto fileDto = videoFileService.uploadFile(metadata.getId(), tempFile, request);

        if(fileDto == null) {
            metadataService.removeMetadata(metadata.getId());
            logger.info("Uploading video file failed.");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uploading video file failed.");
        }

        if(tempFile != null && tempFile.exists()) {
            tempFile.delete();
        }

        metadata.setUrl(fileDto.getUrl());
        metadata.setCreatedAt(new Date());
        metadata.setSize(multipartFile.getSize());
        metadata.setDuration(duration);
        metadata.setContentType(contentType);
        metadata.setFilename(multipartFile.getOriginalFilename());
        metadataService.replaceUploadedFile(metadata);

        return metadata;
    }

    public Metadata convertToFile(VideoConverter.VideoType videoType, String id, SseEmitter sseEmitter) throws ResponseStatusException {
        try {
            Metadata metadata = metadataService.getMetadata(id);
            String filename = metadata.getFilename();

            String newExtension = ".mp4";
            String contentType = "video/mp4";
            if(videoType == VideoConverter.VideoType.WEBM) {
                newExtension = ".webm";
                contentType = "video/webm";
            }
            String newFilename = filename.substring(0, filename.lastIndexOf('.')) + newExtension;
            if(metadataService.findByFileName(newFilename).stream().filter(m -> m.getFilename().equals(filename))
                    .findFirst()
                    .orElse(null) != null) {
                logger.info("Current upload file name already exists.");
                throw new ResponseStatusException(HttpStatus.CONFLICT, "File name already exists.");
            }

            InputStream is = videoFileService.getObject(filename);
            File tempFile = File.createTempFile("video_", ".tmp");
            OutputStream os = new FileOutputStream(tempFile);
            VideoConverter.convert(videoType, is, os, sseEmitter);

            Metadata newMetadata = new Metadata();
            newMetadata = metadataService.createUploadedFile(newMetadata);

            FileDto fileDto = videoFileService.uploadFile(newMetadata.getId(), tempFile, newFilename, tempFile.length());

            if(fileDto == null) {
                metadataService.removeMetadata(newMetadata.getId());
                logger.info("Uploading video file failed.");
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uploading video file failed.");
            }

            // get duration
            long duration = 0;
            try {
                duration = VideoUtil.getDuration(tempFile.getPath()); // TODO: get duration for webm, it returns zero currently
            } catch (IOException e) {
                logger.info("Calculate video duration failed.");
                throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Calculate video duration failed.");
            }

            newMetadata.setUrl(fileDto.getUrl());
            newMetadata.setCreatedAt(new Date());
            newMetadata.setSize(tempFile.length());
            newMetadata.setDuration(duration);
            newMetadata.setContentType(contentType);
            newMetadata.setFilename(newFilename);
            metadataService.replaceUploadedFile(newMetadata);

            if(tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }

            return newMetadata;
        } catch (Exception e) {
            logger.info("Converting " + videoType.name() + " video file failed.");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Converting " + videoType.name() + " video file failed.");
        }
    }
}
