package com.rbattezzati.carrental.service.inventory;

import com.rbattezzati.carrental.factory.ReservationFactory;
import com.rbattezzati.carrental.model.car.Car;
import com.rbattezzati.carrental.model.car.CarType;
import com.rbattezzati.carrental.model.reservation.Reservation;
import com.rbattezzati.carrental.utils.ValidationUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public final class InventoryService {

    private final Map<String, Car> carsById = new ConcurrentHashMap<>();
    private final Map<String, ReentrantLock> carByIdLocks = new ConcurrentHashMap<>();
    private final List<Reservation> reservations = Collections.synchronizedList(new ArrayList<>());

    public InventoryService(Collection<Car> initialCars) {
        initializeInventory(initialCars);
    }

    private void initializeInventory(Collection<Car> initialCars) {
        Objects.requireNonNull(initialCars, "initialCars cannot be null");
        for (Car car : initialCars) {
            carsById.put(car.id(), car);
            carByIdLocks.put(car.id(), new ReentrantLock());
        }
    }

    private List<Car> getCarsByType(CarType carType) {
        return carsById.values().stream()
                .filter(c -> c.carType() == carType)
                .toList();
    }

    public Reservation reserve(CarType carType, LocalDateTime startDateTime, int totalDays) {
        List<Car> carsOfType = getCarsByType(carType);
        ValidationUtils.validateReservationRequest(carType, carsOfType, startDateTime, totalDays);

        for (Car car : carsOfType) {
            ReentrantLock lock = carByIdLocks.get(car.id());
            if (lock.tryLock()) {
                try {
                    if (isCarAvailable(car.id(), startDateTime, totalDays)) {
                        return createReservation(car, startDateTime, totalDays);
                    }
                } finally {
                    lock.unlock();
                }
            }
        }

        throw new IllegalStateException(
                "No available cars of type " + carType + " for the requested period"
        );
    }

    private boolean isCarAvailable(String carId, LocalDateTime start, int days) {
        synchronized (reservations) {
            return reservations.stream()
                    .filter(r -> r.getCar().id().equals(carId))
                    .noneMatch(r -> r.overlaps(start, days));
        }
    }

    private Reservation createReservation(Car car, LocalDateTime startDatetime, int totalDays) {
        String reservationId = UUID.randomUUID().toString();
        Reservation reservation = ReservationFactory.create(reservationId, car, startDatetime, totalDays);
        synchronized (reservations) {
            reservations.add(reservation);
            return reservation;
        }
    }

    public List<Reservation> getReservations() {
        synchronized (reservations) {
            return List.copyOf(reservations);
        }
    }

    public long countAvailable(CarType carType, LocalDateTime startDateTime, int days) {
        return getCarsByType(carType).stream()
                .filter(car -> isCarAvailable(car.id(), startDateTime, days))
                .count();
    }

    /*
    public synchronized long countAvailable(CarType carType,
                                            LocalDateTime start,
                                            int days) {
        List<Car> carsOfType = getCarsByType(carType);
        long count = 0;
        for (Car car : carsOfType) {
            boolean isAvailable = reservations.stream()
                    .filter(r -> r.getCar().id().equals(car.id()))
                    .noneMatch(r -> r.overlaps(start, days));
            if (isAvailable) count++;
        }
        return count;
    }

    public long countAvailable(CarType carType, LocalDateTime start, int days) {
        synchronized (reservationListLock) {
            List<Car> carsOfType = getCarsByType(carType);

            return carsOfType.stream()
                    .filter(car -> isCarAvailableForPeriod(car.id(), start, days))
                    .count();
        }
    }


    private boolean isCarAvailableForPeriod(String carId, LocalDateTime start, int days) {
        Object carLock = carByIdLocks.get(carId);
        synchronized (carLock) {
            synchronized (reservationListLock) {
                return reservations.stream()
                        .filter(r -> r.getCar().id().equals(carId))
                        .noneMatch(r -> r.overlaps(start, days));
            }
        }
    }*/
}
