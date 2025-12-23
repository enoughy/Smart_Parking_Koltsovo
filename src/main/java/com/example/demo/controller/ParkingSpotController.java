package com.example.demo.controller;

import com.example.demo.entity.UserData;
import com.example.demo.dto.SpotAddRequest;
import com.example.demo.entity.*;
import com.example.demo.handler.ParkingSpotDto;
import com.example.demo.repository.ParkingSpotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.AbstractMap;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ParkingSpotController {

    private final ParkingSpotRepository repository;

    public ParkingSpotController(ParkingSpotRepository repository) {
        this.repository = repository;
    }

    public List<ParkingSpot> getAll() {
        return repository.findAll();
    }

    @Transactional
    public boolean addParkingSpot(SpotAddRequest req, UserData user) {
        if (repository.existsByCords_LatAndCords_Lng(req.getLat(), req.getLng())) {
            return false;
        }

        ParkingSpot spot = new ParkingSpot(
                new Cords(req.getLat(), req.getLng()),
                req.getDescription(),
                user
        );

        repository.save(spot);
        return true;
    }

    @Transactional
    public void updateStateByUser(
            double lat,
            double lng,
            ParkingSpotState state,
            UserData user
    ) {
        ParkingSpot spot = repository
                .findByCords_LatAndCords_Lng(lat, lng)
                .orElseThrow();

        spot.setState(state);

        if (state == ParkingSpotState.OCCUPIED || state == ParkingSpotState.RESERVED) {
            spot.setUserOccupied(user);
        } else {
            spot.setUserOccupied(null);
        }
    }
    @Transactional
    public ParkingSpot updateSpot(Long id, ParkingSpot updatedSpot, UserData user) {
        ParkingSpot spot = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Парковка не найдена"));

        if ( !spot.getOwner().equals(user)) { //!user.isAdmin() &&
            throw new SecurityException("Нет прав на редактирование парковки");
        }

        spot.setDescription(updatedSpot.getDescription());
        spot.setCords(updatedSpot.getCords());

        return repository.save(spot);
    }
    @Transactional
    public void updateOccupiedState(Long id, boolean occupied, UserData user) {
        ParkingSpot spot = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Парковка не найдена"));

        if (!spot.getOwner().equals(user)) { //!user.isAdmin() &&
            throw new SecurityException("Нет прав менять занятость парковки");
        }

        spot.setState(occupied ? ParkingSpotState.OCCUPIED : ParkingSpotState.FREE);
        spot.setUserOccupied(occupied ? user : null);

        repository.save(spot);
    }
    @Transactional(readOnly = true)
    public ParkingSpot getSpotById(Long id, UserData user) {
        ParkingSpot spot = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Парковка не найдена"));

        // проверка права на редактирование: владелец или адми
        boolean canEdit = user != null && (spot.getOwner().equals(user) /* || user.isAdmin() */);
        spot.setCanEdit(canEdit);

        return spot;
    }
    @Transactional(readOnly = true)
    public List<ParkingSpotDto> findNearbyFreeSpots(double lat, double lng, double radiusMeters) {
        // Получаем все парковки (можно заменить на репозиторный метод для фильтрации по state на уровне БД)
        List<ParkingSpot> all = repository.findAll();

        return all.stream()
                // Оставляем только свободные
                .filter(spot -> spot.getState() == ParkingSpotState.FREE)
                // Преобразуем в пару (spot, distance)
                .map(spot -> new AbstractMap.SimpleEntry<>(spot, distanceMeters(lat, lng,
                        spot.getCords().getLat(), spot.getCords().getLng())))
                // Оставляем те, что в нужном радиусе
                .filter(entry -> entry.getValue() <= radiusMeters)
                // Сортируем по расстоянию
                .sorted(Comparator.comparingDouble(Map.Entry::getValue))
                // Маппим в DTO
                .map(entry -> {
                    ParkingSpot s = entry.getKey();
                    return new ParkingSpotDto(
                            s.getId(),
                            s.getCords().getLat(),
                            s.getCords().getLng(),
                            s.getDescription(),
                            s.getState() != ParkingSpotState.FREE // occupied flag (false здесь)
                    );
                })
                .collect(Collectors.toList());
    }

    /** Haversine — расстояние в метрах между двумя точками latitude/longitude */
    private double distanceMeters(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6_371_000d; // радиус Земли в метрах
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
