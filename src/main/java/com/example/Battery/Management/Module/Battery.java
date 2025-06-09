package com.example.Battery.Management.Module;

import org.springframework.stereotype.Component;

@Component
public class Battery {
    private double currentCharge; // Nuvarande laddning i kWh
    private double maxCapacity;   // Maxkapacitet i kWh

    public double getCurrentCharge() {
        return currentCharge;
    }

    public void setCurrentCharge(double currentCharge) {
        this.currentCharge = currentCharge;
    }

    public double getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(double maxCapacity) {
        this.maxCapacity = maxCapacity;
    }
    public double getBatteryPercentage() {
        return (currentCharge / maxCapacity) * 100;
    }

    public void setBatteryPercentage(double percentage) {
        this.currentCharge = (percentage / 100) * maxCapacity;
    }
}