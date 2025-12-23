package com.example.demo.handler;


import com.example.demo.dto.SpotAddRequest;
import com.example.demo.entity.*;
import com.example.demo.controller.ParkingSpotController;
import com.example.demo.security.CurrentUserProvider;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parking")
public class ParkingSpotHttpHandler {

    private final ParkingSpotController controller;
    private final CurrentUserProvider currentUserProvider;

    public ParkingSpotHttpHandler(
            ParkingSpotController controller,
            CurrentUserProvider currentUserProvider
    ) {
        this.controller = controller;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping("/all")
    public List<ParkingSpot> getAll() {
        return controller.getAll();
    }

    @PostMapping("/add")
    public void addParkingSpot( @RequestBody SpotAddRequest req, Authentication authentication) {
        System.out.println("AUTH = " + authentication);
        System.out.println("AUTH NAME = " +
                (authentication != null ? authentication.getName() : "null"));

        UserData user = currentUserProvider.getCurrentUser(authentication);
        System.out.println("USER = " + user);
        controller.addParkingSpot(req, currentUserProvider.getCurrentUser(authentication));

    }

    @PatchMapping("/{id}/occupy")
    public void toggleOccupied(
            @PathVariable Long id,
            @RequestBody OccupyRequest occupied,
            Authentication authentication
    ) {
        var user = currentUserProvider.getCurrentUser(authentication);
        controller.updateOccupiedState(id, occupied.isOccupied(), user);
    }
    @GetMapping("/{id}")
    public ParkingSpot getSpot(@PathVariable Long id, Authentication authentication) {
        var user = currentUserProvider.getCurrentUser(authentication);
        return controller.getSpotById(id, user);
    }
    @PutMapping("/{id}")
    public void updateSpot(
            @PathVariable Long id,
            @RequestBody ParkingSpot updatedSpot,
            Authentication authentication
    ) {
        var user = currentUserProvider.getCurrentUser(authentication);
        controller.updateSpot(id, updatedSpot, user);
    }
    @GetMapping("/nearby")
    public List<ParkingSpotDto> getNearby(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam double radius
    ) {
        return controller.findNearbyFreeSpots(lat, lng, radius);
    }
}

