package com.rbattezzati.carrental.test.reservation;

import com.rbattezzati.carrental.factory.CarFactory;
import com.rbattezzati.carrental.factory.ReservationFactory;
import com.rbattezzati.carrental.model.car.Car;
import com.rbattezzati.carrental.model.car.CarType;
import com.rbattezzati.carrental.model.reservation.Reservation;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class ReservationTest {

    @Test
    void createsAndValidateValidReservationWithValidDays() {
        LocalDateTime startDatetime = LocalDateTime.of(2025, 12, 4, 10, 0);
        Car vanCar = CarFactory.getInstance("CAR1", CarType.SEDAN);
        Reservation reservation = ReservationFactory.create(
                "R1",
                vanCar,
                startDatetime,
                3
        );

        //assert all attributes of Reservation
        assertEquals("R1", reservation.getReservationId());
        assertEquals("CAR1", reservation.getCar().id());
        assertEquals(CarType.SEDAN, reservation.getCar().carType());
        assertEquals(3, reservation.getDays());
        assertEquals(startDatetime.plusDays(3), reservation.getEndDateTime());
    }

    @Test
    void getEndDateTimeAddsDaysCorrectly() {
        LocalDateTime startDate = LocalDateTime.of(2025, 11, 30, 10, 0);
        Reservation reservation = Reservation.builder().startDateTime(startDate).days(3).build(); // 30th -> 02nd
        assertEquals(startDate.plusDays(3), reservation.getEndDateTime());
    }

    @Test
    void testOverlapsDatesCorrectReservationDateRanges() {
        LocalDateTime startDate = LocalDateTime.of(2025, 11, 30, 10, 0);
        Reservation reservation = Reservation.builder().startDateTime(startDate).days(3).build(); // 30th -> 02nd
        assertTrue(reservation.overlaps(startDate.plusDays(1), 2));       // inside window
        assertTrue(reservation.overlaps(startDate.minusDays(1), 2));      // starts before, ends inside
    }

    @Test
    void testOverlapsDatesIncorrectReservationDateRanges() {
        LocalDateTime startDate = LocalDateTime.of(2025, 11, 30, 10, 0);
        Reservation reservation = Reservation.builder().startDateTime(startDate).days(3).build(); // 30th -> 02nd
        assertFalse(reservation.overlaps(startDate.minusDays(3), 3));            // ends exactly at start
        assertFalse(reservation.overlaps(reservation.getEndDateTime(), 1));  // starts exactly at end
    }
}
