package com.example.demo.repository;


import com.example.demo.entity.Cords;
import com.example.demo.entity.ParkingSpot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParkingSpotRepository extends JpaRepository<ParkingSpot, Long> {

    Optional<ParkingSpot> findByCords_LatAndCords_Lng(double lat, double lng);

    boolean existsByCords_LatAndCords_Lng(double lat, double lng);
}

