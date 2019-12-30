package com.unloadbrain.assignement.takeaway.kpipublisher.domain.repository;

import com.unloadbrain.assignement.takeaway.kpipublisher.domain.model.ExtractedData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ExtractedDataRepository extends MongoRepository<ExtractedData, String> {

    Optional<ExtractedData> findByUrl(@Param("url") String url);

    ExtractedData getByUrl(@Param("url") String url);
}