package com.unloadbrain.assignement.takeaway.kpipublisher.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document
public class ExtractedData {

    @Id
    private String url;

    private Map<String, Object> extractedData;
}
