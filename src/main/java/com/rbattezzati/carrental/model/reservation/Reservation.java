package com.rbattezzati.carrental.model.reservation;

import com.rbattezzati.carrental.model.car.Car;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Builder
@Getter
public final class Reservation {

    private final String reservationId;
    private final Car car;
    private final LocalDateTime startDateTime;
    private final int days;

    public boolean overlaps(LocalDateTime requestedStart, int requestedDays) {
        LocalDateTime requestedEnd = requestedStart.plusDays(requestedDays);
        LocalDateTime existingEnd  = getEndDateTime();
        return requestedStart.isBefore(existingEnd)
                && requestedEnd.isAfter(startDateTime);
    }

    public LocalDateTime getEndDateTime() {
        return startDateTime.plusDays(days);
    }
}
