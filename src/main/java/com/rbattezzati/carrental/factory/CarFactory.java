package com.rbattezzati.carrental.factory;

import com.rbattezzati.carrental.model.car.*;

public final class CarFactory {

    public static Car getInstance(String id) {
        return getInstance(id, CarType.SEDAN);
    }

    // By CarType
    public static Car getInstance(String id, CarType type) {
        return switch (type) {
            case SEDAN -> new SedanCar(id);
            case SUV -> new SuvCar(id);
            case VAN -> new VanCar(id);
        };
    }

    public static Car getInstance(CarType type, String prefix, int number) {
        String id = prefix + "-" + number;
        return getInstance(id, type);
    }
}
