package com.mptourism.storage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mptourism.model.Location;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component
public class LocationFileStorage {

    private static final String FILE_PATH = "data/locations.json";
    private static final ObjectMapper mapper = new ObjectMapper();

    public static List<Location> loadLocations() {
        try {
            File file = new File(FILE_PATH);
            if (!file.exists()) {
                return new ArrayList<>();
            }
            return mapper.readValue(file, new TypeReference<List<Location>>() {
            });
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static void saveLocations(List<Location> locations) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(FILE_PATH), locations);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
