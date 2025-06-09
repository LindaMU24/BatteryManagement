package com.example.Battery.Management.Module;

import java.time.LocalDateTime;

public class ChargingSession {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private double totalEnergyUsed; // i kWh
    private String optimizationReason;

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public double getTotalEnergyUsed() {
        return totalEnergyUsed;
    }

    public void setTotalEnergyUsed(double totalEnergyUsed) {
        this.totalEnergyUsed = totalEnergyUsed;
    }

    public String getOptimizationReason() {
        return optimizationReason;
    }

    public void setOptimizationReason(String optimizationReason) {
        this.optimizationReason = optimizationReason;
    }
}