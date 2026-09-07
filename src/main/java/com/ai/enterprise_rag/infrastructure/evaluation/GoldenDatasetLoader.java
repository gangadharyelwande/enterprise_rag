package com.ai.enterprise_rag.infrastructure.evaluation;

import tools.jackson.databind.json.JsonMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
public class GoldenDatasetLoader {

    private final JsonMapper jsonMapper;

    public GoldenDatasetLoader(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    public GoldenDataset load() {

        ClassPathResource resource =
                new ClassPathResource("evaluation/golden-dataset.json");

        try (InputStream inputStream = resource.getInputStream()) {

            return jsonMapper.readValue(
                    inputStream,
                    GoldenDataset.class
            );

        } catch (IOException e) {

            throw new IllegalStateException(
                    "Failed to load golden dataset",
                    e
            );
        }
    }
}