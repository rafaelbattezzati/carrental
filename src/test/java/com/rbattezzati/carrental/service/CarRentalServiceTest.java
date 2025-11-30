package com.rbattezzati.carrental.service;

import com.rbattezzati.carrental.model.car.Car;
import com.rbattezzati.carrental.model.car.CarType;
import com.rbattezzati.carrental.model.inventory.InventoryService;
import com.rbattezzati.carrental.model.reservation.Reservation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CarRentalServiceTest {

    private CarRentalService carRentalService;

    @BeforeEach
    void setUp() {
        InventoryService inventoryService = new InventoryService(
                Arrays.asList(
                        new Car("S1",   CarType.SEDAN),
                        new Car("S2",   CarType.SEDAN),
                        new Car("SUV1", CarType.SUV),
                        new Car("V1",   CarType.VAN)
                )
        );
        carRentalService = new CarRentalService(inventoryService);
    }

    @Test
    void canReserveSedanWhenAvailable() {
        LocalDateTime startDateTime = LocalDateTime.now().plusDays(1);

        Reservation reservation = carRentalService.reserveCar(CarType.SEDAN, startDateTime, 3);
        assertNotNull(reservation);




    }



}
