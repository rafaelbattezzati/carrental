package com.rbattezzati.carrental.model.reservation;

import com.rbattezzati.carrental.model.car.CarType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Builder
@Getter
public final class Reservation {

    private final String reservationId;
    private final String carId;
    @Enumerated(EnumType.STRING)
    private final CarType carType;
    private final LocalDateTime startDateTime;
    private final int days;

    public boolean overlaps(LocalDateTime startDateTime, int requestedDays) {
        LocalDateTime requestedEnd = startDateTime.plusDays(requestedDays);
        return startDateTime.isBefore(getEndDateTime())
                && requestedEnd.isAfter(startDateTime);
    }

    public LocalDateTime getEndDateTime() {
        return startDateTime.plusDays(days);
    }
}
