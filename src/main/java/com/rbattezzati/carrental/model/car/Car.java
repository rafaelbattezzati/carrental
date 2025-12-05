package com.rbattezzati.carrental.model.car;

public sealed interface Car permits SedanCar, SuvCar, VanCar {
    String id();
    CarType carType();
    String getDescription();
    double getDailyRate();
}

