package com.demo.videostore.service.controller;

import com.demo.videostore.service.dto.FileDto;
import com.demo.videostore.service.util.VideoConverter;
import com.demo.videostore.service.model.Metadata;
import com.demo.videostore.service.service.VideoStoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping(value = "v1/files")
public class VideoStoreController {

    @Autowired
    private VideoStoreService videoStoreService;

    private Map<String, SseEmitter> sseEmitters = new ConcurrentHashMap<>();

    @GetMapping("/progress")
    public SseEmitter eventEmitter() throws IOException {
        SseEmitter sseEmitter = new SseEmitter(Long.MAX_VALUE);
        UUID guid = UUID.randomUUID();
        sseEmitters.put(guid.toString(), sseEmitter);
        sseEmitter.send(SseEmitter.event().name("GUI_ID").data(guid.toString()));
        sseEmitter.onCompletion(() -> sseEmitters.remove(guid.toString()));
        sseEmitter.onTimeout(() -> sseEmitters.remove(guid.toString()));
        return sseEmitter;
    }

    @PostMapping("/convert/{videoType}/{id}/{guid}")
    public ResponseEntity<Metadata> convertToFile(@PathVariable("videoType") VideoConverter.VideoType videoType,
                                                  @PathVariable("id") String id,
                                                  @PathVariable("guid") UUID guid) throws Exception {
        try {
            SseEmitter sseEmitter = sseEmitters.get(guid.toString());
            Metadata metadata = videoStoreService.convertToFile(videoType, id, sseEmitter);
            sseEmitter.complete();
            return ResponseEntity.ok().body(metadata);
        } catch (Exception ex) {
            throw ex;
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getFile(@PathVariable("id") String id) throws IOException {
        try {
            return videoStoreService.getFile(id);
        } catch (Exception ex) {
            throw ex;
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeFile(@PathVariable("id") String id) {
        try {
            videoStoreService.removeFile(id);
            return ResponseEntity.noContent().build();
        } catch (Exception ex) {
            throw ex;
        }
    }

    @PostMapping
    public ResponseEntity<Metadata> upload(@ModelAttribute FileDto request) {
        try {
            Metadata metadata = videoStoreService.uploadFile(request);
            return ResponseEntity.ok().body(metadata);
        } catch (Exception ex) {
            throw ex;
        }
    }

    @GetMapping
    public ResponseEntity<List<Metadata>> getFileList() {
        try {
            return ResponseEntity.ok().body(videoStoreService.getFileList());
        } catch (Exception ex) {
            throw ex;
        }
    }

    @GetMapping("/search/filename/{filename}")
    public ResponseEntity<List<Metadata>> findByFileName(@PathVariable("filename") String filename) {
        try {
            return ResponseEntity.ok().body(videoStoreService.findByFileName(filename));
        } catch (Exception ex) {
            throw ex;
        }
    }

    @GetMapping("/search/duration/{from}-{to}")
    public ResponseEntity<List<Metadata>> findByDuration(@PathVariable("from") int from, @PathVariable("to") int to) {
        try {
            return ResponseEntity.ok().body(videoStoreService.findByDuration(from, to));
        } catch (Exception ex) {
            throw ex;
        }
    }
}
