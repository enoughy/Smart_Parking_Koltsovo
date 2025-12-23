package com.example.demo.entity;

import jakarta.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class Cords {

    private double lat;
    private double lng;

    public Cords() {}

    public Cords(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public double getLat() { return lat; }
    public double getLng() { return lng; }

    public void setLat(double lat) { this.lat = lat; }
    public void setLng(double lng) { this.lng = lng; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cords)) return false;
        Cords cords = (Cords) o;
        return Double.compare(cords.lat, lat) == 0 &&
                Double.compare(cords.lng, lng) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lat, lng);
    }
}
