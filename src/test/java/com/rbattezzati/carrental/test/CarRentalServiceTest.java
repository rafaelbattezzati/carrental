package com.rbattezzati.carrental.test;

import com.rbattezzati.carrental.factory.CarFactory;
import com.rbattezzati.carrental.model.car.CarType;
import com.rbattezzati.carrental.model.reservation.Reservation;
import com.rbattezzati.carrental.service.carrental.CarRentalService;
import com.rbattezzati.carrental.service.inventory.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class CarRentalServiceTest {

    private CarRentalService carRentalService;
    private LocalDateTime defaultStartDate;

    @BeforeEach
    void setUp() {
        InventoryService inventoryService = new InventoryService(
                Arrays.asList(
                        CarFactory.getInstance("S1",   CarType.SEDAN),
                        CarFactory.getInstance("S2",   CarType.SEDAN),
                        CarFactory.getInstance("SUV1", CarType.SUV),
                        CarFactory.getInstance("V1",   CarType.VAN)
                )
        );
        defaultStartDate = LocalDateTime.now();
        carRentalService = new CarRentalService(inventoryService);
    }

    @Test
    void canReserveSedanWhenAvailable() {
        LocalDateTime startDate = LocalDateTime.now().plusDays(1);
        Reservation reservation = carRentalService.reserveCar(CarType.SEDAN, startDate, 3);

        assertNotNull(reservation);
        assertEquals(CarType.SEDAN, reservation.getCar().carType());
        assertEquals(3, reservation.getDays());
    }

    @Test
    void cannotExceedInventoryForOverlappingReservations() {
        LocalDateTime startDate = LocalDateTime.now().plusDays(1);

        // There are 2 sedans; make 2 overlapping reservations
        carRentalService.reserveCar(CarType.SEDAN, startDate, 3);
        carRentalService.reserveCar(CarType.SEDAN, startDate.plusHours(1), 2);

        // Third overlapping reservation should fail
        assertThrows(IllegalStateException.class, () ->
                carRentalService.reserveCar(CarType.SEDAN, startDate.plusHours(2), 1)
        );
    }

    @Test
    void canReserveSameCarTypeForNonOverlappingPeriods() {
        LocalDateTime startDate = LocalDateTime.now().plusDays(1);
        Reservation reservation1 = carRentalService.reserveCar(CarType.SUV, startDate, 2);
        Reservation reservation2 = carRentalService.reserveCar(CarType.SUV, startDate.plusDays(2), 2);

        assertNotEquals(reservation1.getReservationId(), reservation2.getReservationId());
    }

    @Test
    void reservationFailsWhenInventoryExhausted() {
        carRentalService.reserveCar(CarType.SEDAN, defaultStartDate, 2);
        carRentalService.reserveCar(CarType.SEDAN, defaultStartDate.plusHours(1), 2);

        assertThrows(IllegalStateException.class, () ->
                carRentalService.reserveCar(CarType.SEDAN, defaultStartDate.plusHours(2), 1)
        );
    }

    @Test
    void rejectsNonPositiveDays() {
        assertThrows(IllegalArgumentException.class, () ->
                carRentalService.reserveCar(CarType.VAN, defaultStartDate, 0)
        );

        assertThrows(IllegalArgumentException.class, () ->
                carRentalService.reserveCar(CarType.VAN, defaultStartDate, -1)
        );
    }

    @Test
    void validatesAvailabilityInformation() {
        long initialAvailableSedanCars = carRentalService.availableCars(CarType.SEDAN, defaultStartDate, 2);
        assertEquals(2, initialAvailableSedanCars);

        carRentalService.reserveCar(CarType.SEDAN, defaultStartDate, 2);

        long afterAvailableSedanCars = carRentalService.availableCars(CarType.SEDAN, defaultStartDate, 2);
        assertEquals(1, afterAvailableSedanCars);
    }
}
