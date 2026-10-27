package dev.kozhevnikov.autotests.utils.startdata.utility;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.core.io.Resource;

import java.io.IOException;

public class YamlLoader {
    private static final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory()).registerModule(new JavaTimeModule());

    public static <T> T loadResource(Resource resource, Class<T> clazz) {
        try {
            return objectMapper.readValue(resource.getInputStream(), clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
