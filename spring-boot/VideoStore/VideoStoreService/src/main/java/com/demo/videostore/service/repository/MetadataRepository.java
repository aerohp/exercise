package com.demo.videostore.service.repository;

import com.demo.videostore.service.model.Metadata;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MetadataRepository extends MongoRepository<Metadata, String> {

    @Query("{ 'duration' : { $gt: ?0, $lt: ?1 } }")
    List<Metadata> findByDurationBetween(int durationGT, int durationLT);

    @Query("{ 'filename' : { $regex: ?0, $options: i} }")
    List<Metadata> findByFileName(String filename);
}
