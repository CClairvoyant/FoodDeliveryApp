package com.trialtask.fooddeliveryapp;


import com.trialtask.fooddeliveryapp.enums.City;
import com.trialtask.fooddeliveryapp.enums.Vehicle;
import com.trialtask.fooddeliveryapp.weather.WeatherData;

import java.util.HashMap;
import java.util.Map;


@SuppressWarnings("NonAsciiCharacters")
public class DeliveryFeeCalculator {

    private static final Map<City, Map<Vehicle, Float>> REGIONAL_FEES = new HashMap<>();

    static {
        REGIONAL_FEES.put(City.TALLINN, Map.of(Vehicle.CAR, 4f, Vehicle.SCOOTER, 3.5f, Vehicle.BIKE, 3f));
        REGIONAL_FEES.put(City.TARTU, Map.of(Vehicle.CAR, 3.5f, Vehicle.SCOOTER, 3f, Vehicle.BIKE, 2.5f));
        REGIONAL_FEES.put(City.PÄRNU, Map.of(Vehicle.CAR, 3f, Vehicle.SCOOTER, 2.5f, Vehicle.BIKE, 2f));
    }

    private final Vehicle vehicle;
    private final City city;

    public DeliveryFeeCalculator(City city, Vehicle vehicle) {
        this.city = city;
        this.vehicle = vehicle;
    }

    public float calculate() {
        float regionalFee = getRegionalFee();
        float weatherFee = calculateWeatherFee();
        return weatherFee != -1 ? regionalFee + weatherFee : -1;
    }

    private float getRegionalFee() {
        return REGIONAL_FEES.get(city).get(vehicle);
    }

    private float calculateWeatherFee() {
        WeatherData latestWeatherData = getLatestWeatherData();
        float fee = 0;

        float airTemperature = latestWeatherData.getAirTemperature();
        float windSpeed = latestWeatherData.getWindSpeed();
        String phenomenon = latestWeatherData.getPhenomenon();

        if (vehicle == Vehicle.BIKE || vehicle == Vehicle.SCOOTER) {
            if (airTemperature < -10) {
                fee += 1;
            } else if (airTemperature <= 0) {
                fee += 0.5;
            }

            if (phenomenon != null) {
                if (phenomenon.contains("snow") || phenomenon.contains("sleet")) {
                    fee += 1;
                } else if (phenomenon.contains("rain") || phenomenon.contains("shower")) {
                    fee += 0.5;
                } else if (phenomenon.equals("Glaze")
                        || phenomenon.equals("Hail")
                        || phenomenon.contains("Thunder")) {
                    return -1;
                }
            }
        }

        if (vehicle == Vehicle.BIKE) {
            if (windSpeed > 20) {
                return -1;
            }

            if (windSpeed >= 10) {
                fee += 0.5;
            }
        }

        return fee;
    }

    public static WeatherData getLatestWeatherData(City city) {
        return FoodDeliveryApplication.repository.findFirstByLocationContainingIgnoreCaseOrderByTimestampDesc(city.name());
    }

    public WeatherData getLatestWeatherData() {
        return FoodDeliveryApplication.repository.findFirstByLocationContainingIgnoreCaseOrderByTimestampDesc(city.name());
    }
}
