package ui.skilltree.config;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class BeanConfiguration {

    /**
     * Sources of the skilltree.json resource ordered by priority
     */
    private final List<Resource> skillTreeResources = List.of(new FileSystemResource("./config/skilltree.json"), new ClassPathResource("META-INF/skilltree.json"));

    @Bean("skillTreeConfiguration")
    public SkillTreeConfiguration get() {
        for (Resource resource : skillTreeResources) {
            try {
                return read(resource);
            } catch (IOException ignored) {
            }
        }
        return null;
    }

    private SkillTreeConfiguration read(Resource resource) throws IOException {
        EncodedResource encodedResource = new EncodedResource(resource, StandardCharsets.UTF_8);

        JsonReader reader = new JsonReader(encodedResource.getReader());

        return new Gson().fromJson(reader, SkillTreeConfiguration.class);
    }
}