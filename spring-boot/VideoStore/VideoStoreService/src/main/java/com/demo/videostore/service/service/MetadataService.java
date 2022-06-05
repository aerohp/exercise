package com.demo.videostore.service.service;

import com.demo.videostore.service.model.Metadata;
import com.demo.videostore.service.repository.MetadataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class MetadataService {

    private static final Logger logger = LoggerFactory.getLogger(MetadataService.class);

    @Autowired
    private MetadataRepository repository;

    public List<Metadata> getFileList() {
        return repository.findAll();
    }

    public Metadata getMetadata(String id) {
        return repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found with " + id));
    }

    public Metadata createUploadedFile(Metadata request) {
        Metadata metadata = new Metadata();
        metadata.setFilename(request.getFilename());
        metadata.setSize(request.getSize());
        metadata.setUrl(request.getUrl());
        metadata.setCreatedAt(request.getCreatedAt());

        return repository.insert(metadata);
    }

    public Metadata replaceUploadedFile(Metadata metadata) {
        return repository.save(metadata);
    }

    public void removeMetadata(String id) {
        repository.deleteById(id);
    }

    public List<Metadata> findByFileName(String filename) {
        return repository.findByFileName(filename);
    }

    public List<Metadata> findByDuration(int from, int to) {
        return repository.findByDurationBetween(from, to);
    }
}
