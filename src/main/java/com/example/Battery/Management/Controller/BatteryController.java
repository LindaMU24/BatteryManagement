package com.example.Battery.Management.Controller;
import com.example.Battery.Management.Module.Battery;
import com.example.Battery.Management.Module.ChargingSession;
import com.example.Battery.Management.Service.BatteryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;

@RestController
public class BatteryController {

    private final BatteryService batteryService;
    private final Battery battery;

    public BatteryController(BatteryService batteryService, Battery battery) {
        this.batteryService = batteryService;
        this.battery = battery;
    }

    @PostMapping("/startCharging")
    public String startCharging() {
        return batteryService.startCharging();
    }

    @PostMapping("/stopCharging")
    public String stopCharging() {
        return batteryService.stopCharging();
    }

    @PostMapping("/discharge")
    public String discharge() {
        return batteryService.dischargeBattery();
    }

    @GetMapping("/batteryPercentage")
    public double getBatteryPercentage() {
        return batteryService.getBatteryPercentage();
    }

    @GetMapping("/charge-when-consumption-low")
    public ResponseEntity<String> chargeWhenConsumptionLow() {
        Battery battery = new Battery();
        battery.setMaxCapacity(46.3); // Batterikapacitet i kWh
        battery.setCurrentCharge(20.0); // Nuvarande laddningsnivå i procent

        ChargingSession session = batteryService.chargeWhenConsumptionIsLow(battery);

        if (session.getStartTime() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

            String formattedStartTime = session.getStartTime().format(formatter);
            String formattedEndTime = session.getEndTime().format(formatter);

            String info = String.format(
                    "Laddning startade kl: %s och slutade kl: %s. Total energiåtgång: %.2f kWh. " +
                            "Laddstationens effekt är 7.4 kW och laddningen är optimerad för att ske när hushållets förbrukning är som lägst. " +
                            "Den totala effekten överstiger inte 11 kW under laddningen.",
                    formattedStartTime, formattedEndTime, session.getTotalEnergyUsed());

            return ResponseEntity.ok(info);
        } else {
            return ResponseEntity.ok("Laddningen kunde inte starta. Villkoren uppfylldes inte.");
        }
    }

    @GetMapping("/charge-when-price-low")
    public ResponseEntity<String> chargeWhenPriceLow() {
        Battery battery = new Battery();
        battery.setMaxCapacity(46.3); // Batterikapacitet i kWh
        battery.setCurrentCharge(20.0); // Nuvarande laddningsnivå i procent

        ChargingSession session = batteryService.chargeWhenPriceIsLow(battery);

        if (session != null && session.getStartTime() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

            String formattedStartTime = session.getStartTime().format(formatter);
            String formattedEndTime = session.getEndTime().format(formatter);

            String info = String.format(
                    "Laddning startade kl: %s och slutade kl: %s. Total energiåtgång: %.2f kWh. " +
                            "Laddningen är optimerad för att ske när elpriset är som lägst. " +
                            "Den totala effekten överstiger inte 11 kW under laddningen.",
                    formattedStartTime, formattedEndTime, session.getTotalEnergyUsed());

            return ResponseEntity.ok(info);
        } else {
            return ResponseEntity.ok("Laddningen kunde inte starta. Villkoren uppfylldes inte.");
        }
    }
}