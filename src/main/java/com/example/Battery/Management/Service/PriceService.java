package com.example.Battery.Management.Service;

import com.example.Battery.Management.Module.EnergyPrices;
import com.example.Battery.Management.Module.Price;

import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

@Service
public class PriceService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public PriceService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://127.0.0.1:5000").build();
        this.objectMapper = new ObjectMapper();
    }

    public EnergyPrices getPriceInfo() {
        String response = webClient.get()
                .uri("/priceperhour")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {
            List<Double> prices = objectMapper.readValue(response, new TypeReference<List<Double>>() {});
            EnergyPrices energyPrices = new EnergyPrices();
            energyPrices.setEnergy_price(prices);
            return energyPrices;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON", e);
        }
    }
    public String getFormattedPriceInfo() {
        EnergyPrices energyPrices = getPriceInfo();
        List<Double> prices = energyPrices.getEnergy_price();

        StringBuilder formattedPrices = new StringBuilder();
        for (int i = 0; i < prices.size(); i++) {
            formattedPrices.append(String.format("Kl: %02d:00 är priset %.2f öre/kWh\n", i, prices.get(i)));
        }
        return formattedPrices.toString();
    }

}




