package com.example.Battery.Management.Controller;
import com.example.Battery.Management.Module.EnergyConsumption;
import com.example.Battery.Management.Service.EnergyConsumptionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EnergyConsumptionController {

        private final EnergyConsumptionService energyConsumptionService;

        public EnergyConsumptionController(EnergyConsumptionService energyConsumptionService) {
            this.energyConsumptionService = energyConsumptionService;
        }

    @GetMapping("/showFormattedConsumption")
    public String showFormattedConsumptionInfo() {
        return energyConsumptionService.getFormattedConsumptionInfo();
    }
    }


