package com.mptourism.storage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mptourism.model.Category;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class CategoryFileStorage {

    private static final String FILE_PATH = "data/categories.json";

    private static final ObjectMapper mapper = new ObjectMapper();

    public static List<Category> loadCategories() {
        try {
            File file = new File(FILE_PATH);
            if (!file.exists()) {
                return new ArrayList<>();
            }

            return mapper.readValue(file, new TypeReference<List<Category>>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static void saveCategories(List<Category> categories) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(FILE_PATH), categories);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
