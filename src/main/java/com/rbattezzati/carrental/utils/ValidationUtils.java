package com.rbattezzati.carrental.utils;

import com.rbattezzati.carrental.model.car.Car;
import com.rbattezzati.carrental.model.car.CarType;

import java.time.LocalDateTime;
import java.util.List;

public final class ValidationUtils {

    private static final int MAX_BOOKING_DAYS = 30;

    public static void validateReservationRequest(CarType carType,
                                                  List<Car> carsOfType,
                                                  LocalDateTime startDateTime,
                                                  int days) {
        validateCarTypeAvailable(carType, carsOfType);
        validateTotalDays(days);
        validateStartDateTime(startDateTime);
    }

    public static void validateStartDateTime(LocalDateTime startDateTime) {
        if (startDateTime == null) {
            throw new IllegalArgumentException("Start date/time must not be null");
        }
        if (startDateTime.isBefore(LocalDateTime.now().minusHours(1))) {
            throw new IllegalArgumentException(
                    "Start date/time cannot be in the past. Now: " + LocalDateTime.now());
        }
    }

    public static void validateTotalDays(int days) {
        if (days < 1) {
            throw new IllegalArgumentException("Number of days must be at least 1");
        }
        if (days > MAX_BOOKING_DAYS) {
            throw new IllegalArgumentException(
                    String.format("Maximum booking period is %d days", MAX_BOOKING_DAYS));
        }
    }

    public static void validateCarTypeAvailable(CarType carType, List<Car> carsOfType) {
        if (carsOfType.isEmpty()) {
            throw new IllegalStateException(
                    String.format("No cars of type '%s' available in inventory", carType));
        }
    }
}
