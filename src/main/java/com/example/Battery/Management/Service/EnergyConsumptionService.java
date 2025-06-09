package com.example.Battery.Management.Service;
import com.example.Battery.Management.Module.EnergyConsumption;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

@Service
public class EnergyConsumptionService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public EnergyConsumptionService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://127.0.0.1:5000").build();
        this.objectMapper = new ObjectMapper();
    }

    public EnergyConsumption getConsumptionInfo() {
        String response = webClient.get()
                .uri("/baseload")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {
            List<Double> consumption = objectMapper.readValue(response, new TypeReference<List<Double>>() {});
            EnergyConsumption energyConsumption = new EnergyConsumption();

            energyConsumption.setConsumption(consumption);
            return energyConsumption;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON", e);
        }
    }
    public String getFormattedConsumptionInfo() {
        EnergyConsumption energyConsumption = getConsumptionInfo();
        List<Double> consumption = energyConsumption.getConsumption();

        StringBuilder formattedConsumption = new StringBuilder();
        for (int i = 0; i < consumption.size(); i++) {
            formattedConsumption.append(String.format("Kl: %02d:00 är förbrukningen %.2f kWh\n", i, consumption.get(i)));
        }
        return formattedConsumption.toString();
    }
    public int getLowestConsumptionHour() {
        EnergyConsumption energyConsumption = getConsumptionInfo();
        List<Double> consumption = energyConsumption.getConsumption();

        // Hitta index för minsta värdet
        int minIndex = 0;
        for (int i = 1; i < consumption.size(); i++) {
            if (consumption.get(i) < consumption.get(minIndex)) {
                minIndex = i;
            }
        }
        return minIndex; // Returnerar timmen med lägst förbrukning
    }
}

