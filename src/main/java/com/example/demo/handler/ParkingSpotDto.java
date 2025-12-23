package com.example.demo.handler;

public class ParkingSpotDto {

    private Long id;
    private double lat;
    private double lng;
    private String description;
    private boolean occupied;

    public ParkingSpotDto() {}

    public ParkingSpotDto(Long id, double lat, double lng, String description, boolean occupied) {
        this.id = id;
        this.lat = lat;
        this.lng = lng;
        this.description = description;
        this.occupied = occupied;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public double getLat() { return lat; }
    public void setLat(double lat) { this.lat = lat; }

    public double getLng() { return lng; }
    public void setLng(double lng) { this.lng = lng; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isOccupied() { return occupied; }
    public void setOccupied(boolean occupied) { this.occupied = occupied; }
}