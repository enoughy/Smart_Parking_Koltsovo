package com.example.demo.dto;

import com.example.demo.entity.Cords;

public class SpotDto {
    public Long id;
    public String description;
    public Cords cords;
    public String state; // "FREE", "OCCUPIED", ...
    public Long ownerId;
    public boolean canEdit;
}
