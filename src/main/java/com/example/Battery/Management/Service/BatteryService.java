package com.example.Battery.Management.Service;

import com.example.Battery.Management.Module.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BatteryService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final EnergyConsumptionService energyConsumptionService;
    private final PriceService priceService;
    private static final double CHARGING_POWER = 7.4;

    public BatteryService(WebClient.Builder webClientBuilder, EnergyConsumptionService energyConsumptionService, PriceService priceService) {
        this.webClient = webClientBuilder.baseUrl("http://127.0.0.1:5000").build();
        this.objectMapper = new ObjectMapper();
        this.energyConsumptionService = energyConsumptionService;
        this.priceService = priceService;
    }

    public BatteryInfo getBatteryInfo() {
        String response = webClient.get()
                .uri("/info")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {
            return objectMapper.readValue(response, BatteryInfo.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON", e);
        }
    }

    public String startCharging() {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("charging", "on");

        String response = webClient.post()
                .uri("/charge")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return handleResponse(response);
    }


    public String stopCharging() {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("charging", "off");

        String response = webClient.post()
                .uri("/charge")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return handleResponse(response);
    }


    public String dischargeBattery() {
        return webClient.post()
                .uri("/discharge")
                .header("Content-Type", "application/json")
                .bodyValue("{\"discharging\": \"on\"}")
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public double getBatteryPercentage() {
        String response = webClient.get()
                .uri("/charge")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return Double.parseDouble(response);
    }

    public String handleResponse(String response) {
        // Logga svaret för felsökning
        System.out.println("Server response: " + response);

        // Hantera svaret beroende på innehåll
        if (response.contains("error") || response.startsWith("<html>")) {
            return "Server returned an error or HTML page";
        }
        return response;
    }


    public ChargingSession chargeWhenConsumptionIsLow(Battery battery) {
        EnergyConsumption energyConsumption = energyConsumptionService.getConsumptionInfo();
        List<Double> consumption = energyConsumption.getConsumption();
        double maxTotalPower = 11.0;
        double chargingPower = 7.4;
        double lowestConsumption = Double.MAX_VALUE;
        int bestStartHour = -1;

        for (int hour = 0; hour < consumption.size(); hour++) {
            double currentHourConsumption = consumption.get(hour);

            if (currentHourConsumption + chargingPower <= maxTotalPower && currentHourConsumption < lowestConsumption) {
                lowestConsumption = currentHourConsumption;
                bestStartHour = hour;
            }
        }

        if (bestStartHour != -1) {
            ChargingSession session = new ChargingSession();
            LocalDateTime startTime = LocalDateTime.now().withHour(bestStartHour).withMinute(0);
            LocalDateTime endTime = startTime.plusMinutes(225);  // 3 timmar och 45 minuter
            session.setStartTime(startTime);
            session.setEndTime(endTime);
            session.setTotalEnergyUsed(calculateEnergyUsed(startTime, endTime));
            session.setOptimizationReason("Lowest consumption period");

            return session;
        } else {
            System.out.println("Ingen lämplig period hittades för laddning baserat på förbrukning.");
            return null;
        }
    }

    private double calculateEnergyUsed(LocalDateTime startTime, LocalDateTime endTime) {
        // Beräkna skillnaden i timmar mellan start- och sluttid
        long durationInSeconds = Duration.between(startTime, endTime).getSeconds();
        double durationInHours = durationInSeconds / 3600.0; // Konvertera sekunder till timmar

        // Beräkna energiåtgången baserat på laddningseffekt
        return durationInHours * CHARGING_POWER;
    }

    public ChargingSession chargeWhenPriceIsLow(Battery battery) {
        EnergyConsumption energyConsumption = energyConsumptionService.getConsumptionInfo();
        List<Double> consumption = energyConsumption.getConsumption();
        EnergyPrices energyPrices = priceService.getPriceInfo();
        List<Double> prices = energyPrices.getEnergy_price();

        double maxTotalPower = 11.0;
        double chargingPower = 7.4;
        double lowestPrice = Double.MAX_VALUE;
        int bestStartHour = -1;

        for (int hour = 0; hour < prices.size(); hour++) {
            double currentHourConsumption = consumption.get(hour);
            double currentHourPrice = prices.get(hour);

            if (currentHourPrice < lowestPrice && currentHourConsumption + chargingPower <= maxTotalPower) {
                lowestPrice = currentHourPrice;
                bestStartHour = hour;
            }
        }

        if (bestStartHour != -1) {
            ChargingSession session = new ChargingSession();
            LocalDateTime startTime = LocalDateTime.now().withHour(bestStartHour).withMinute(0);
            LocalDateTime endTime = startTime.plusMinutes(225);  // 3 timmar och 45 minuter
            session.setStartTime(startTime);
            session.setEndTime(endTime);
            session.setTotalEnergyUsed(calculateEnergyUsed(startTime, endTime));
            session.setOptimizationReason("Lowest price period");

            return session;
        } else {
            System.out.println("Ingen lämplig period hittades för laddning baserat på pris.");
            return null;
        }
    }
        public List<Double> calculateHourlyConsumptionWithCharging(ChargingSession session) {
        List<Double> consumptionDuringCharging = new ArrayList<>();
        List<Double> consumption = energyConsumptionService.getConsumptionInfo().getConsumption();
        double chargingPowerPerInterval = 7.4 / (60 * 15); // Laddningseffekt per 4-sekundersintervall

        // Antal "timmar" (4-sekundersintervaller) i 3 timmar och 45 minuter
        int numberOfIntervals = (3 * 60 + 45) * 15;

        int startIndex = session.getStartTime().getHour() * 15; // Omvandla starttimmen till startindex baserat på 4-sekundersintervaller

        for (int interval = 0; interval < numberOfIntervals; interval++) {
            int index = startIndex + interval;
            if (index < consumption.size()) { // Kontrollera att vi inte går utanför listans gränser
                double totalConsumption = consumption.get(index) + chargingPowerPerInterval;
                consumptionDuringCharging.add(totalConsumption);
            }
        }

        return consumptionDuringCharging;
    }
}


