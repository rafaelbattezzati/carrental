package com.rbattezzati.carrental.model.inventory;

import com.rbattezzati.carrental.model.car.Car;
import com.rbattezzati.carrental.model.car.CarType;
import com.rbattezzati.carrental.model.reservation.Reservation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class InventoryService {

    private final Map<String, Car> carsById = new HashMap<>();
    private final Map<String, Object> carByIdLocks = new ConcurrentHashMap<>();
    private final List<Reservation> reservations = new ArrayList<>();
    // Global lock for reservation list integrity
    private final Object reservationListLock = new Object();

    public InventoryService(Collection<Car> initialCars){
        for (Car car : initialCars){
            carsById.put(car.id(), car);
            carByIdLocks.put(car.id(), new Object());
        }
    }

    public Reservation reserve(CarType carType,
                               LocalDateTime startDatetime,
                               int totalDays) {

        List<Car> carsOfType =  carsById.values().stream()
                                    .filter(c -> c.carType() == carType)
                                    .toList();
        isCarTypeAvailable(carType, carsOfType);

        for(Car car : carsOfType) {
            Object carLock = carByIdLocks.get(car.id());
            synchronized (carLock) {
                boolean isAvailable;
                synchronized (reservationListLock) {
                    isAvailable = reservations.stream()
                            .filter(r -> r.getCarId().equals(car.id()))
                            .noneMatch(r -> r.overlaps(startDatetime, totalDays));
                    if (isAvailable){
                        String reservationId = UUID.randomUUID().toString();
                        return new Reservation(reservationId,
                                car.id(),
                                carType,
                                startDatetime,
                                totalDays);
                    }
                }
            }
        }
        throw new IllegalStateException("No available cars of type " + carType + " for the requested period");
    }

    private static void isCarTypeAvailable(CarType carType, List<Car> carsOfType) {
        if(carsOfType.isEmpty()){
            throw new IllegalStateException("No cars of type "+ carType + " available");
        }
    }

    public synchronized List<Reservation> getReservations() {
        return new ArrayList<>(reservations);
    }

    public synchronized long countAvailable(CarType type,
                                            LocalDateTime start,
                                            int days) {
        List<Car> carsOfType = carsById.values().stream()
                .filter(c -> c.carType() == type)
                .collect(Collectors.toList());

        long count = 0;
        for (Car car : carsOfType) {
            boolean isAvailable = reservations.stream()
                    .filter(r -> r.getCarId().equals(car.id()))
                    .noneMatch(r -> r.overlaps(start, days));
            if (isAvailable) {
                count++;
            }
        }
        return count;
    }
}
