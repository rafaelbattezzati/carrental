package com.rbattezzati.carrental.factory;

import com.rbattezzati.carrental.model.car.Car;
import com.rbattezzati.carrental.model.reservation.Reservation;

import java.time.LocalDateTime;
import java.util.Objects;

public class ReservationFactory {

    public static Reservation create(String reservationId,
                                     Car car,
                                     LocalDateTime startDateTime,
                                     int days) {
        validateInputs(reservationId, car, startDateTime);
        return Reservation.builder()
                .reservationId(reservationId)
                .car(car)
                .startDateTime(startDateTime)
                .days(days)
                .build();
    }

    private static void validateInputs(String reservationId, Car car, LocalDateTime startDateTime) {
        Objects.requireNonNull(reservationId, "ReservationId cannot be null");
        Objects.requireNonNull(car, "Car cannot be null");
        Objects.requireNonNull(startDateTime, "Start date cannot be null");
    }
}
