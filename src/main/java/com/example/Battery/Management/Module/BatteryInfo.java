package com.example.Battery.Management.Module;

public class BatteryInfo {
    private int simulationTime; // Antal sekunder för ett dygn
    private double totalConsumption; // Total energiförbrukning
    private double batteryCharge; // EV-batteriets laddning i kWh

    public int getSimulationTime() {
        return simulationTime;
    }

    public void setSimulationTime(int simulationTime) {
        this.simulationTime = simulationTime;
    }

    public double getTotalConsumption() {
        return totalConsumption;
    }

    public void setTotalConsumption(double totalConsumption) {
        this.totalConsumption = totalConsumption;
    }

    public double getBatteryCharge() {
        return batteryCharge;
    }

    public void setBatteryCharge(double batteryCharge) {
        this.batteryCharge = batteryCharge;
    }
}