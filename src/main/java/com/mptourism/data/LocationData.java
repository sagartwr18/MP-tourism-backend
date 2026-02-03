package com.mptourism.data;

import com.mptourism.model.Location;
import com.mptourism.storage.LocationFileStorage;

import java.util.List;

public class LocationData {
    
    public static List<Location> locations;

    static {
        locations = LocationFileStorage.loadLocations();
    }
}
