package com.mptourism.controller;

import com.mptourism.data.LocationData;
import com.mptourism.model.Location;
import com.mptourism.storage.LocationFileStorage;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/locations")
public class LocationController {
    
    // @GetMapping("/category/{categoryId}")
    // public List<Location> getByCategory(@PathVariable int categoryId) {
    //     return LocationData.locations.stream().filter(l -> l.getCategoryId() == categoryId).collect(Collectors.toList());
    // }

    

}
