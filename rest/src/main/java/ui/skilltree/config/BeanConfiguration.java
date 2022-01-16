package ui.skilltree.config;

import com.google.gson.Gson;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ResourceUtils;

import java.io.*;

@Configuration
public class BeanConfiguration {

    private static final String PATH = "classpath:skilltree.json";

    @Bean("skillTreeConfiguration")
    public SkillTreeConfiguration get() {
        try {
            File file = ResourceUtils.getFile(PATH);
            String json = readFromFile(file);
            return parseSkillTreeConfiguration(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String readFromFile(File file) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        StringBuilder result = new StringBuilder();
        String line;
        while((line = bufferedReader.readLine()) != null) {
            result.append(line);
        }
        return result.toString();
    }

    private SkillTreeConfiguration parseSkillTreeConfiguration(String json) {
        return new Gson().fromJson(json, SkillTreeConfiguration.class);
    }
}