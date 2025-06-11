package com.example.Battery.Management.Controller;
import com.example.Battery.Management.Module.Battery;
import com.example.Battery.Management.Module.ChargingSession;
import com.example.Battery.Management.Module.EnergyConsumption;
import com.example.Battery.Management.Service.BatteryService;
import com.example.Battery.Management.Service.EnergyConsumptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
public class BatteryController {

    private final BatteryService batteryService;
    private final Battery battery;
    private final EnergyConsumptionService energyConsumptionService;

    public BatteryController(BatteryService batteryService, Battery battery, EnergyConsumptionService energyConsumptionService) {
        this.batteryService = batteryService;
        this.battery = battery;
        this.energyConsumptionService = energyConsumptionService;
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

        // Hämta förbrukningsdata
        EnergyConsumption energyConsumption = energyConsumptionService.getConsumptionInfo();
        List<Double> consumption = energyConsumption.getConsumption();

        // Sträng för att visa förbrukning per timme och inkludera laddningseffekt
        StringBuilder consumptionInfo = new StringBuilder("Förbrukning per timme:\n");
        if (session != null && session.getStartTime() != null) {
            LocalDateTime startTime = session.getStartTime();
            LocalDateTime endTime = session.getEndTime();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

            for (int hour = 0; hour < consumption.size(); hour++) {
                LocalDateTime currentHour = startTime.withHour(hour).withMinute(0);
                double currentConsumption = consumption.get(hour);

                if (!currentHour.isBefore(startTime) && currentHour.isBefore(endTime.minusMinutes(45))) {
                    // Lägg till laddningseffekt för hela timmar
                    currentConsumption += 7.4;
                } else if (!currentHour.isBefore(endTime.minusMinutes(45)) && currentHour.isBefore(endTime)) {
                    // Lägg till laddningseffekt för de sista 45 minuterna
                    currentConsumption += 5.55;
                }
                consumptionInfo.append(String.format("Kl %02d:00 - %.2f kWh\n", hour, currentConsumption));
            }
            String formattedStartTime = startTime.format(formatter);
            String formattedEndTime = endTime.format(formatter);

            String info = String.format(
                    "Laddning startade kl: %s och slutade kl: %s. Total energiåtgång: %.2f kWh. " +
                            "Laddstationens effekt är 7.4 kW och laddningen är optimerad för att ske när hushållets förbrukning är som lägst. " +
                            "Den totala effekten överstiger inte 11 kW under laddningen.\n\n%s",
                    formattedStartTime, formattedEndTime, session.getTotalEnergyUsed(), consumptionInfo.toString());

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
            int startHour = session.getStartTime().getHour();
            int endHour = session.getEndTime().getHour();

            // Hämta förbrukningsdata
            EnergyConsumption energyConsumption = energyConsumptionService.getConsumptionInfo();
            List<Double> consumption = energyConsumption.getConsumption();

            // Sträng för att visa förbrukning per timme och inkludera laddningseffekt
            StringBuilder consumptionInfo = new StringBuilder("Förbrukning per timme:\n");
            LocalDateTime startTime = session.getStartTime();
            LocalDateTime endTime = session.getEndTime();
            for (int hour = 0; hour < consumption.size(); hour++) {
                double currentConsumption = consumption.get(hour);

                if ((hour >= startHour && hour < 24) || (hour >= 0 && hour < endHour)) {
                    currentConsumption += 7.4;
                }
                if (hour == endHour) {
                    currentConsumption += 5.55; // Lägg till energin för de sista 45 minuterna
                }
                consumptionInfo.append(String.format("Kl %02d:00 - %.2f kWh\n", hour, currentConsumption));
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                String formattedStartTime = startTime.format(formatter);
                String formattedEndTime = endTime.format(formatter);

            String info = String.format(
                    "Laddning startade kl: %s och slutade kl: %s. Total energiåtgång: %.2f kWh. " +
                            "Laddstationens effekt är 7.4 kW och laddningen är optimerad för att ske när hushållets förbrukning är som lägst. " +
                            "Den totala effekten överstiger inte 11 kW under laddningen.\n\n%s",
                    formattedStartTime, formattedEndTime, session.getTotalEnergyUsed(), consumptionInfo.toString());

                return ResponseEntity.ok(info);
            } else {
                return ResponseEntity.ok("Laddningen kunde inte starta. Villkoren uppfylldes inte.");
            }
        }
    }

