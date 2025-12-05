package com.rbattezzati.carrental.model.car;

public record SuvCar(String id) implements Car {
    @Override
    public CarType carType() {
        return CarType.SUV;
    }

    @Override
    public String getDescription() {
        return "SUV - 5 seats, AWD, spacious";
    }

    @Override
    public double getDailyRate() {
        return 80.0;
    }
}