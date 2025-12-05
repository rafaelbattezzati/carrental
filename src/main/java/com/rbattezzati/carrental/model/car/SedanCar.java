package com.rbattezzati.carrental.model.car;

public record SedanCar(String id) implements Car {
    @Override
    public CarType carType() {
        return CarType.SEDAN;
    }

    @Override
    public String getDescription() {
        return "Compact Sedan - 4 seats, fuel efficient";
    }

    @Override
    public double getDailyRate() {
        return 50.0;
    }
}

