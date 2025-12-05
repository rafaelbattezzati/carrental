package com.rbattezzati.carrental.service.carrental;

import com.rbattezzati.carrental.model.car.CarType;
import com.rbattezzati.carrental.service.inventory.InventoryService;
import com.rbattezzati.carrental.model.reservation.Reservation;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@RequiredArgsConstructor
public class CarRentalService {

    private final InventoryService inventoryService;

    public Reservation reserveCar(CarType carType,
                                  LocalDateTime startDateTime,
                                  int days) {
        return inventoryService.reserve(carType, startDateTime, days);
    }

    public long availableCars(CarType carType,
                              LocalDateTime startDateTime,
                              int days) {
        return inventoryService.countAvailable(carType, startDateTime, days);
    }
}
