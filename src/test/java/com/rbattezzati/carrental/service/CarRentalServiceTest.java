package com.rbattezzati.carrental.service;

import com.rbattezzati.carrental.model.car.Car;
import com.rbattezzati.carrental.model.car.CarType;
import com.rbattezzati.carrental.model.inventory.InventoryService;
import com.rbattezzati.carrental.model.reservation.Reservation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

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
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        Reservation reservation = carRentalService.reserveCar(CarType.SEDAN, start, 3);

        assertNotNull(reservation);
        assertEquals(CarType.SEDAN, reservation.getCarType());
        assertEquals(3, reservation.getDays());
    }

    @Test
    void cannotExceedInventoryForOverlappingReservations() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);

        // There are 2 sedans; make 2 overlapping reservations
        carRentalService.reserveCar(CarType.SEDAN, start, 3);
        carRentalService.reserveCar(CarType.SEDAN, start.plusHours(1), 2);

        // Third overlapping reservation should fail
        assertThrows(IllegalStateException.class, () ->
                carRentalService.reserveCar(CarType.SEDAN, start.plusHours(2), 1)
        );
    }


    @Test
    void canReserveSameCarTypeForNonOverlappingPeriods() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);

        Reservation r1 = carRentalService.reserveCar(CarType.SUV, start, 2);
        Reservation r2 = carRentalService.reserveCar(CarType.SUV, start.plusDays(2), 2);

        assertNotEquals(r1.getReservationId(), r2.getReservationId());
    }
    
}
