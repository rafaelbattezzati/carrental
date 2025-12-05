package com.rbattezzati.carrental.test.inventory;

import com.rbattezzati.carrental.factory.CarFactory;
import com.rbattezzati.carrental.model.car.CarType;
import com.rbattezzati.carrental.model.reservation.Reservation;
import com.rbattezzati.carrental.service.inventory.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventoryServiceTest {

    private InventoryService inventoryService;
    private LocalDateTime startDateTime;

    @BeforeEach
    void setUp() {
        inventoryService = new InventoryService(Arrays.asList(
                CarFactory.getInstance("S1", CarType.SEDAN),
                CarFactory.getInstance("S2", CarType.SEDAN),
                CarFactory.getInstance("SUV1", CarType.SUV),
                CarFactory.getInstance("V1", CarType.VAN)
        ));
        startDateTime = LocalDateTime.now();
    }

    @Test
    void reservesCarWhenAvailable() {
        Reservation reservation = inventoryService.reserve(CarType.SEDAN, startDateTime, 2);

        assertNotNull(reservation);
        assertEquals(CarType.SEDAN, reservation.getCar().carType());
        assertEquals(2, reservation.getDays());
        assertTrue(reservation.getCar().id().equals("S1") || reservation.getCar().id().equals("S2"));
    }

    @Test
    void cannotReserveIfNoCarsOfTypeInInventory() {
        InventoryService emptySedanInventory = new InventoryService(Arrays.asList(
                CarFactory.getInstance("SUV1", CarType.SUV)
        ));

        assertThrows(IllegalStateException.class, () ->
                emptySedanInventory.reserve(CarType.SEDAN, startDateTime, 1)
        );
    }

    @Test
    void cannotExceedInventoryForOverlappingReservations() {
        // Two sedans in inventory
        inventoryService.reserve(CarType.SEDAN, startDateTime, 3);
        inventoryService.reserve(CarType.SEDAN, startDateTime.plusHours(2), 2);

        // Third overlapping reservation should fail
        assertThrows(IllegalStateException.class, () ->
                inventoryService.reserve(CarType.SEDAN, startDateTime.plusHours(4), 1)
        );
    }

    @Test
    void allowsReservationsForNonOverlappingPeriods() {
        Reservation r1 = inventoryService.reserve(CarType.SUV, startDateTime, 2);
        Reservation r2 = inventoryService.reserve(CarType.SUV, startDateTime.plusDays(2), 2);

        assertNotEquals(r1.getReservationId(), r2.getReservationId());
        assertEquals(r1.getCar().id(), r2.getCar().id()); // Same physical car, different time windows
    }

    @Test
    void countAvailableReturnsCorrectNumber() {
        // Initially both sedans free
        assertEquals(2, inventoryService.countAvailable(CarType.SEDAN, startDateTime, 2));

        // Reserve one during the window
        inventoryService.reserve(CarType.SEDAN, startDateTime, 2);

        assertEquals(1, inventoryService.countAvailable(CarType.SEDAN, startDateTime, 2));
    }
}
