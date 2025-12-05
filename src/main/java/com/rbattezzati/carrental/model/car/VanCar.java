package com.rbattezzati.carrental.model.car;

public record VanCar(String id) implements Car {
    @Override
    public CarType carType() {
        return CarType.VAN;
    }

    @Override
    public String getDescription() {
        return "Van - 7+ seats, family transport";
    }

    @Override
    public double getDailyRate() {
        return 120.0;
    }
}
