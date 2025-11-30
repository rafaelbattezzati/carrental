package com.rbattezzati.carrental.model.reservation;

import com.rbattezzati.carrental.model.car.CarType;

import java.time.LocalDateTime;

public record ReservationRecord(
    String reservationId,
    String carId,
    CarType carType,
    LocalDateTime startDateTime,
    int days
){

    public boolean overlaps(LocalDateTime startDateTime, int requestedDays) {
        LocalDateTime requestedEnd = startDateTime.plusDays(requestedDays);
        return startDateTime.isBefore(getEndDateTime())
                && requestedEnd.isAfter(startDateTime);
    }

    public LocalDateTime getEndDateTime() {
        return startDateTime.plusDays(days);
    }
}
