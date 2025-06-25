package com.example.portfolioapplication;
public class Location {
    private double latitude;
    private double longitude;
    private String timestamp;

    public Location(double latitude, double longitude, String timestamp) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getTimestamp() {
        return timestamp;
    }
}