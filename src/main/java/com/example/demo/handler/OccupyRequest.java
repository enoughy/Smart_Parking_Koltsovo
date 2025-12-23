package com.example.demo.handler;

public class OccupyRequest {

    private boolean occupied;

    public OccupyRequest() {} // нужен для Jackson

    public boolean isOccupied() {
        return occupied;
    }

    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }
}