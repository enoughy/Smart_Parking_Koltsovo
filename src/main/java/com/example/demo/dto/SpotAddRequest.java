package com.example.demo.dto;

public class SpotAddRequest {

    private double lat;
    private double lng;
    private String description;

    public double getLat() { return lat; }
    public double getLng() { return lng; }
    public String getDescription() { return description; }

    public void setLat(double lat) { this.lat = lat; }
    public void setLng(double lng) { this.lng = lng; }
    public void setDescription(String description) { this.description = description; }
}

